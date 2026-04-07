package com.kdb.it.domain.council.service;

import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.common.system.security.CustomUserDetails;
import com.kdb.it.domain.council.dto.CouncilDto;
import com.kdb.it.domain.council.entity.Basctm;
import com.kdb.it.domain.council.entity.Bpovwm;
import com.kdb.it.domain.council.repository.CouncilRepository;
import com.kdb.it.domain.council.repository.ProjectOverviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 정보화실무협의회 기본 서비스
 *
 * <p>협의회 목록 조회, 신규 생성, 단건 조회, 상태 전이를 담당합니다.</p>
 *
 * <p>권한별 조회 범위:</p>
 * <ul>
 *   <li>일반사용자(ITPZZ001): 소속 부서(BBR_C) 기준 사업의 협의회만 조회</li>
 *   <li>관리자(ITPAD001): 전체 협의회 조회</li>
 *   <li>평가위원: BCMMTM에 ENO가 있는 협의회만 조회</li>
 * </ul>
 *
 * <p>협의회 ID 채번 형식: {@code ASCT-{연도}-{4자리순번}} (예: ASCT-2026-0001)</p>
 *
 * <p>Design Ref: §2.1 Architecture Decision — Clean Architecture, 서비스 분리</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouncilService {

    /** 협의회 기본정보 리포지토리 (TAAABB_BASCTM) */
    private final CouncilRepository councilRepository;

    /** 사업개요 리포지토리 (TAAABB_BPOVWM) — 사업명 조회용 */
    private final ProjectOverviewRepository projectOverviewRepository;

    /** 사용자 정보 리포지토리 — 평가위원 역할 확인용 */
    private final UserRepository userRepository;

    // =========================================================================
    // 조회
    // =========================================================================

    /**
     * 권한별 협의회 목록 조회
     *
     * <p>Plan SC: Step 1~3 전 과정 온라인 처리 기반 목록 제공</p>
     *
     * @param userDetails 현재 로그인한 사용자 정보
     * @return 권한에 맞는 협의회 목록
     */
    public List<CouncilDto.ListResponse> getCouncilList(CustomUserDetails userDetails) {
        if (userDetails.isAdmin()) {
            // 관리자: 전체 부서 대상으로 결재완료 사업(미신청 포함) + 기신청 협의회 통합 조회
            return councilRepository.findProjectsForCouncilAll().stream()
                    .map(row -> toListResponseFromRow(row))
                    .collect(Collectors.toList());
        }

        if (isCommitteeMember(userDetails)) {
            // 평가위원: 배정된 협의회만 조회
            return councilRepository.findByCommitteeMember(userDetails.getEno(), "N").stream()
                    .map(c -> toListResponseFromEntity(c))
                    .collect(Collectors.toList());
        }

        // 일반사용자: SVN_DPM = 사용자 BBR_C 조건으로 결재완료 사업 + 기신청 협의회 통합 조회
        return councilRepository.findProjectsForCouncilByDepartment(userDetails.getBbrC()).stream()
                .map(row -> toListResponseFromRow(row))
                .collect(Collectors.toList());
    }

    /**
     * 협의회 단건 상세 조회
     *
     * @param asctId 협의회ID
     * @return 협의회 상세 정보
     * @throws IllegalArgumentException 존재하지 않는 협의회
     */
    public CouncilDto.DetailResponse getCouncil(String asctId) {
        Basctm council = findActiveCouncil(asctId);
        return toDetailResponse(council);
    }

    // =========================================================================
    // 생성
    // =========================================================================

    /**
     * 협의회 신규 신청
     *
     * <p>소관부서 담당자(ITPZZ001)가 타당성검토표 작성 전 협의회를 신청합니다.
     * 초기 상태는 DRAFT(작성중)로 설정됩니다.</p>
     *
     * @param request     협의회 신청 요청 (프로젝트 정보, 심의유형)
     * @param userDetails 신청자 정보
     * @return 생성된 협의회ID
     */
    @Transactional
    public String createCouncil(CouncilDto.CreateRequest request, CustomUserDetails userDetails) {
        // 협의회ID 채번: ASCT-{연도}-{4자리순번}
        String asctId = generateAsctId();

        // 협의회 기본정보 생성 (초기 상태: DRAFT)
        Basctm council = Basctm.builder()
                .asctId(asctId)
                .prjMngNo(request.prjMngNo())
                .prjSno(request.prjSno())
                .asctSts("DRAFT")
                .dbrTp(request.dbrTp())
                .build();

        councilRepository.save(council);
        return asctId;
    }

    // =========================================================================
    // 상태 전이
    // =========================================================================

    /**
     * 협의회 상태 변경
     *
     * <p>각 서비스(FeasibilityService, CommitteeService 등)에서 비즈니스 이벤트 완료 시 호출합니다.</p>
     *
     * @param asctId    협의회ID
     * @param targetSts 변경할 상태 코드 (CCODEM ASCT_STS 기준)
     */
    @Transactional
    public void changeStatus(String asctId, String targetSts) {
        Basctm council = findActiveCouncil(asctId);
        council.changeStatus(targetSts);
        // JPA Dirty Checking으로 자동 반영
    }

    // =========================================================================
    // 내부 헬퍼
    // =========================================================================

    /**
     * 활성 협의회 조회 (삭제되지 않은 항목)
     *
     * @param asctId 협의회ID
     * @return Basctm 엔티티
     * @throws IllegalArgumentException 존재하지 않는 경우
     */
    public Basctm findActiveCouncil(String asctId) {
        return councilRepository.findByAsctIdAndDelYn(asctId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 협의회입니다. asctId=" + asctId));
    }

    /**
     * 협의회ID 채번
     *
     * <p>형식: ASCT-{연도}-{4자리순번} (예: ASCT-2026-0001)</p>
     *
     * @return 생성된 협의회ID
     */
    private String generateAsctId() {
        int year = java.time.LocalDate.now().getYear();
        Long seq = councilRepository.getNextSequenceValue();
        return String.format("ASCT-%d-%04d", year, seq);
    }

    /**
     * 현재 사용자가 평가위원인지 확인
     *
     * <p>ITPZZ001이지만 특정 협의회에 배정된 경우 평가위원으로 동작합니다.
     * 목록 조회 시 권한 분기 판단에 사용합니다.</p>
     *
     * @param userDetails 현재 사용자 정보
     * @return 평가위원이면 true (일반사용자이면서 BCMMTM에 ENO가 있는 경우)
     */
    private boolean isCommitteeMember(CustomUserDetails userDetails) {
        // 일반사용자(ITPZZ001)인 경우 BCMMTM 배정 여부 확인
        // 관리자는 이미 위에서 분기 처리되므로 이 시점은 비관리자임
        if (!userDetails.hasAthId(CustomUserDetails.ATH_USER)) {
            return false;
        }
        // BCMMTM에 ENO가 있는 협의회 수 > 0 이면 평가위원
        List<Basctm> memberCouncils = councilRepository.findByCommitteeMember(userDetails.getEno(), "N");
        return !memberCouncils.isEmpty();
    }

    /**
     * Basctm 엔티티 → ListResponse 변환 (평가위원용)
     *
     * <p>사업명은 BPOVWM에서 조회합니다. 타당성검토표가 없으면 null을 반환합니다.
     * 사업 상세 필드(prjYy 등)는 평가위원 뷰에서 불필요하므로 null로 처리합니다.</p>
     */
    private CouncilDto.ListResponse toListResponseFromEntity(Basctm council) {
        // 사업명 조회 (BPOVWM 선택적 존재)
        String prjNm = projectOverviewRepository
                .findByAsctIdAndDelYn(council.getAsctId(), "N")
                .map(Bpovwm::getPrjNm)
                .orElse(null);

        return new CouncilDto.ListResponse(
                council.getAsctId(),
                council.getPrjMngNo(),
                council.getPrjSno(),
                prjNm,
                council.getAsctSts(),
                council.getDbrTp(),
                council.getCnrcDt(),
                true,  // 이미 신청된 건
                null, null, null, null, null, null, null, null  // 사업 상세 (평가위원 뷰 미사용)
        );
    }

    /**
     * Native Query Object[] 행 → ListResponse 변환 (관리자/일반사용자용)
     *
     * <p>컬럼 순서: prjMngNo(0), prjSno(1), prjNm(2), asctId(3), asctSts(4),
     * dbrTp(5), cnrcDt(6), applied(7), prjYy(8), prjTp(9), svnDpm(10),
     * prjBg(11), sttDt(12), endDt(13), itDpm(14), prjDes(15)</p>
     */
    private CouncilDto.ListResponse toListResponseFromRow(Object[] row) {
        String asctId  = (String) row[3];
        String asctSts = (String) row[4];
        String dbrTp   = (String) row[5];
        // Oracle JDBC 버전에 따라 DATE → java.sql.Date 또는 java.time.LocalDateTime으로 반환
        java.time.LocalDate cnrcDt = toLocalDate(row[6]);
        java.time.LocalDate sttDt  = toLocalDate(row[12]);
        java.time.LocalDate endDt  = toLocalDate(row[13]);
        // Oracle NUMBER(1) → BigDecimal 등으로 반환되므로 intValue() 처리
        boolean applied = row[7] != null && ((Number) row[7]).intValue() == 1;
        // Oracle NUMBER(15,2) → BigDecimal
        java.math.BigDecimal prjBg = row[11] != null ? new java.math.BigDecimal(row[11].toString()) : null;

        return new CouncilDto.ListResponse(
                asctId,
                (String) row[0],
                row[1] != null ? ((Number) row[1]).intValue() : null,
                (String) row[2],
                asctSts,
                dbrTp,
                cnrcDt,
                applied,
                (String) row[8],   // prjYy
                (String) row[9],   // prjTp
                (String) row[10],  // svnDpm
                prjBg,             // prjBg
                sttDt,             // sttDt
                endDt,             // endDt
                (String) row[14],  // itDpm
                (String) row[15]   // prjDes
        );
    }

    /**
     * Oracle Native Query DATE 컬럼 → LocalDate 변환
     *
     * <p>Oracle JDBC 드라이버 버전에 따라 DATE 컬럼이 java.sql.Date,
     * java.time.LocalDateTime, java.time.LocalDate 등 다양한 타입으로 반환될 수 있어
     * 방어적으로 처리합니다.</p>
     *
     * @param val Native Query 결과의 날짜 컬럼 값
     * @return LocalDate (null이면 null 반환)
     */
    private java.time.LocalDate toLocalDate(Object val) {
        if (val == null) return null;
        if (val instanceof java.time.LocalDate) return (java.time.LocalDate) val;
        if (val instanceof java.time.LocalDateTime) return ((java.time.LocalDateTime) val).toLocalDate();
        if (val instanceof java.sql.Date) return ((java.sql.Date) val).toLocalDate();
        if (val instanceof java.sql.Timestamp) return ((java.sql.Timestamp) val).toLocalDateTime().toLocalDate();
        return null;
    }

    /**
     * Basctm → DetailResponse 변환
     */
    private CouncilDto.DetailResponse toDetailResponse(Basctm council) {
        return new CouncilDto.DetailResponse(
                council.getAsctId(),
                council.getPrjMngNo(),
                council.getPrjSno(),
                council.getAsctSts(),
                council.getDbrTp(),
                council.getCnrcDt(),
                council.getCnrcTm(),
                council.getCnrcPlc()
        );
    }
}
