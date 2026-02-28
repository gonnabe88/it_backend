package com.kdb.it.service;

import com.kdb.it.domain.entity.Project;
import com.kdb.it.dto.ProjectDto;
import com.kdb.it.repository.ProjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 정보화사업(IT 프로젝트) 서비스
 *
 * <p>정보화사업(TAAABB_BPRJTM) 엔티티의 CRUD 및 품목({@link com.kdb.it.domain.entity.Bitemm}) 동기화
 * 비즈니스 로직을 처리합니다.</p>
 *
 * <p>결재 연동:</p>
 * <ul>
 *   <li>수정/삭제 시 해당 프로젝트에 연결된 신청서(CAPPLA)의 결재 상태를 확인합니다</li>
 *   <li>"결재중" 또는 "결재완료" 상태인 경우 수정/삭제가 불가합니다</li>
 *   <li>원본 테이블 코드: {@code "BPRJTM"}</li>
 * </ul>
 *
 * <p>품목(Bitemm) 동기화 로직 (수정 시):</p>
 * <ol>
 *   <li>요청의 {@code gclMngNo}가 있으면 기존 항목 수정</li>
 *   <li>요청의 {@code gclMngNo}가 없으면 신규 항목 추가</li>
 *   <li>요청에 없는 기존 항목은 Soft Delete</li>
 * </ol>
 *
 * <p>Soft Delete 패턴: {@code DEL_YN='Y'}로 논리 삭제합니다.</p>
 *
 * <p>{@code @Transactional(readOnly = true)}: 조회 메서드의 기본값.
 * 쓰기 메서드는 {@code @Transactional}로 오버라이드합니다.</p>
 */
@Service                              // Spring 서비스 빈으로 등록
@RequiredArgsConstructor              // final 필드 생성자 자동 주입 (Lombok)
@Transactional(readOnly = true)       // 기본 읽기 전용 트랜잭션
public class ProjectService {

    /** 정보화사업 데이터 접근 리포지토리 (TAAABB_BPRJTM) */
    private final ProjectRepository projectRepository;

    /** 신청서-원본 데이터 연결 리포지토리 (TAAABB_CAPPLA): 결재 상태 확인용 */
    private final com.kdb.it.repository.CapplaRepository capplaRepository;

    /** 신청서 마스터 리포지토리 (TAAABB_CAPPLM): 결재 상태 조회용 */
    private final com.kdb.it.repository.CapplmRepository capplmRepository;

    /** 품목 데이터 접근 리포지토리 (TAAABB_BITEMM) */
    private final com.kdb.it.repository.BitemmRepository bitemmRepository;

    /**
     * 전체 정보화사업 목록 조회
     *
     * <p>삭제되지 않은({@code DEL_YN='N'}) 모든 프로젝트를 조회합니다.
     * 각 프로젝트에 연결된 최신 신청서 정보(신청관리번호, 결재상태)를 포함합니다.</p>
     *
     * <p>목록 조회에서는 품목(Bitemm) 정보를 포함하지 않습니다 (성능 최적화).</p>
     *
     * @return 전체 정보화사업 응답 DTO 목록 (신청서 정보 포함, 품목 제외)
     */
    public List<ProjectDto.Response> getProjectList() {
        return projectRepository.findAllByDelYn("N").stream()
                .map(project -> {
                    ProjectDto.Response response = ProjectDto.Response.fromEntity(project);
                    // 각 프로젝트의 최신 신청서 정보(신청관리번호, 결재상태) 조회 및 설정
                    setApplicationInfo(response, project.getPrjMngNo(), project.getPrjSno());
                    return response;
                })
                .toList();
    }

    /**
     * 단건 정보화사업 상세 조회
     *
     * <p>프로젝트관리번호로 삭제되지 않은 프로젝트를 조회합니다.
     * 신청서 정보와 품목({@link com.kdb.it.domain.entity.Bitemm}) 목록을 포함합니다.</p>
     *
     * @param prjMngNo 조회할 프로젝트관리번호
     * @return 정보화사업 상세 응답 DTO (신청서 정보, 품목 목록 포함)
     * @throws IllegalArgumentException 해당 관리번호의 프로젝트가 없는 경우
     */
    public ProjectDto.Response getProject(String prjMngNo) {
        // 프로젝트 조회 (삭제되지 않은 항목만)
        Project project = projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N")
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));

        ProjectDto.Response response = ProjectDto.Response.fromEntity(project);
        // 최신 신청서 정보 조회 및 설정
        setApplicationInfo(response, prjMngNo, project.getPrjSno());

        // 품목 정보 조회 및 설정 (삭제되지 않은 항목만)
        // PRJ_MNG_NO(프로젝트관리번호), PRJ_SNO(프로젝트일련번호) 기준, DEL_YN='N'인 품목 조회
        List<com.kdb.it.domain.entity.Bitemm> bitemms = bitemmRepository.findByPrjMngNoAndPrjSnoAndDelYn(prjMngNo,
                project.getPrjSno(), "N");

        // 품목 엔티티를 DTO로 변환하여 응답 객체에 설정
        List<ProjectDto.BitemmDto> itemDtos = bitemms.stream()
                .map(ProjectDto.BitemmDto::fromEntity)
                .toList();
        response.setItems(itemDtos);

        return response;
    }

    /**
     * 신규 정보화사업 생성
     *
     * <p>프로젝트관리번호({@code PRJ_MNG_NO})가 없으면 Oracle 시퀀스로 자동 채번합니다.
     * 제공된 경우 중복 여부를 확인합니다.</p>
     *
     * <p>관리번호 자동 생성 형식: {@code PRJ-{bgYy}-{seq:04d}}</p>
     * <p>예: {@code PRJ-2026-0001}</p>
     *
     * @param request 정보화사업 생성 요청 DTO (프로젝트명, 예산, 기간, 담당자 등)
     * @return 생성된 프로젝트관리번호
     * @throws IllegalArgumentException 제공된 관리번호가 이미 존재하는 경우
     */
    @Transactional
    public String createProject(ProjectDto.CreateRequest request) {
        String prjMngNo = request.getPrjMngNo();

        // 프로젝트관리번호가 없으면 자동 채번
        if (prjMngNo == null || prjMngNo.isEmpty()) {
            Long nextVal = projectRepository.getNextSequenceValue(); // Oracle 시퀀스 채번

            // 예산년도 결정 (요청값 없으면 현재 연도 사용)
            String year = request.getBgYy();
            if (year == null || year.isEmpty()) {
                year = String.valueOf(java.time.LocalDate.now().getYear());
                request.setBgYy(year);
            }

            // 형식: PRJ-{bgYy}-{seq:04d}
            prjMngNo = String.format("PRJ-%s-%04d", year, nextVal);
            request.setPrjMngNo(prjMngNo);

        } else {
            // 제공된 관리번호 중복 확인
            if (projectRepository.existsById(prjMngNo)) {
                throw new IllegalArgumentException("Project already exists with id: " + prjMngNo);
            }
        }

        // 엔티티 생성 및 저장
        Project project = request.toEntity();
        projectRepository.save(project);
        return project.getPrjMngNo(); // 저장된 관리번호 반환
    }

    /**
     * 정보화사업 수정
     *
     * <p>프로젝트 기본 정보를 수정하고, 품목(Bitemm) 목록을 동기화합니다.</p>
     *
     * <p>결재 상태 확인: "결재중" 또는 "결재완료" 상태인 경우 수정이 불가합니다.</p>
     *
     * <p>품목 동기화 로직:</p>
     * <ol>
     *   <li>기존 품목 목록 조회 (DEL_YN='N')</li>
     *   <li>요청 품목 처리:
     *     <ul>
     *       <li>{@code gclMngNo}가 있는 경우: 기존 항목 찾아 수정 (새 빌더로 생성 후 save → JPA merge)</li>
     *       <li>{@code gclMngNo}가 없는 경우: 신규 항목으로 시퀀스 채번 후 추가</li>
     *     </ul>
     *   </li>
     *   <li>요청에 없는 기존 항목: Soft Delete ({@code DEL_YN='Y'})</li>
     * </ol>
     *
     * @param prjMngNo 수정할 프로젝트관리번호
     * @param request  수정 요청 DTO (변경할 필드들, 품목 목록)
     * @return 수정된 프로젝트관리번호
     * @throws IllegalArgumentException 해당 관리번호의 프로젝트가 없는 경우
     * @throws IllegalStateException    결재중/결재완료 상태여서 수정 불가한 경우
     */
    @Transactional
    public String updateProject(String prjMngNo, ProjectDto.UpdateRequest request) {
        // 프로젝트 조회 (삭제 여부 무관)
        Project project = projectRepository.findById(prjMngNo)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));

        // 결재 상태 확인 (BPRJTM 테이블 코드로 신청서 연결 여부 조회)
        // 결재중 또는 결재완료 상태인 경우 수정 불가
        boolean isProcessingOrApproved = capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                "BPRJTM", prjMngNo, project.getPrjSno(), java.util.List.of("결재중", "결재완료"));

        if (isProcessingOrApproved) {
            throw new IllegalStateException("결재중이거나 결재완료된 프로젝트는 수정할 수 없습니다.");
        }

        // 프로젝트 기본 정보 수정 (JPA Dirty Checking으로 자동 반영)
        project.update(
                request.getPrjNm(),      // 프로젝트명
                request.getPrjTp(),      // 프로젝트유형
                request.getSvnDpm(),     // 주관부서
                request.getItDpm(),      // IT부서
                request.getPrjBg(),      // 프로젝트예산
                request.getSttDt(),      // 시작일자
                request.getEndDt(),      // 종료일자
                request.getSvnDpmCgpr(), // 주관부서담당자
                request.getItDpmCgpr(),  // IT부서담당자
                request.getSvnDpmTlr(),  // 주관부서담당팀장
                request.getItDpmTlr(),   // IT부서담당팀장
                request.getEdrt(),       // 전결권
                request.getPrjDes(),     // 사업설명
                request.getPulRsn(),     // 추진사유
                request.getSaf(),        // 현황
                request.getNcs(),        // 필요성
                request.getXptEff(),     // 기대효과
                request.getPlm(),        // 문제
                request.getPrjRng(),     // 사업범위
                request.getPulPsg(),     // 추진경과
                request.getHrfPln(),     // 향후계획
                request.getBzDtt(),      // 업무구분
                request.getTchnTp(),     // 기술유형
                request.getMnUsr(),      // 주요사용자
                request.getDplYn(),      // 중복여부
                request.getLblFsgTlm(),  // 의무완료기한
                request.getRprSts(),     // 보고상태
                request.getPrjPulPtt(),  // 프로젝트추진가능성
                request.getPrjSts(),     // 프로젝트상태
                request.getBgYy(),       // 예산년도
                request.getSvnHdq());    // 주관본부/부문

        // ===== 품목 정보 동기화 (CUD) =====
        if (request.getItems() != null) {
            // 1. 기존 품목 조회 (DEL_YN='N')
            List<com.kdb.it.domain.entity.Bitemm> existingItems = bitemmRepository
                    .findByPrjMngNoAndPrjSnoAndDelYn(prjMngNo, project.getPrjSno(), "N");

            // 처리된 품목 관리번호 추적 (삭제 대상 식별용)
            java.util.Set<String> processedGclMngNos = new java.util.HashSet<>();
            // 현재 최대 SNO 계산 (신규 추가 시 MAX+1로 설정)
            int maxGclSno = existingItems.stream()
                    .mapToInt(com.kdb.it.domain.entity.Bitemm::getGclSno)
                    .max().orElse(0);

            // 2. 요청 품목 처리 (수정 또는 신규 추가)
            for (ProjectDto.BitemmDto itemDto : request.getItems()) {
                if (itemDto.getGclMngNo() != null && !itemDto.getGclMngNo().isEmpty()) {
                    // === 기존 항목 수정 ===
                    // gclMngNo와 gclSno로 기존 항목 찾기
                    com.kdb.it.domain.entity.Bitemm existingItem = existingItems.stream()
                            .filter(item -> item.getGclMngNo().equals(itemDto.getGclMngNo())
                                    && item.getGclSno().equals(itemDto.getGclSno()))
                            .findFirst()
                            .orElse(null);

                    if (existingItem != null) {
                        // Bitemm 엔티티에 update 메서드가 없으므로,
                        // 동일 PK(gclMngNo, gclSno)로 새 Builder 생성 후 save → JPA merge 수행
                        com.kdb.it.domain.entity.Bitemm updatedItem = com.kdb.it.domain.entity.Bitemm.builder()
                                .gclMngNo(existingItem.getGclMngNo())   // PK 유지
                                .gclSno(existingItem.getGclSno())       // PK 유지
                                .prjMngNo(existingItem.getPrjMngNo())   // 프로젝트관리번호 유지
                                .prjSno(existingItem.getPrjSno())       // 프로젝트순번 유지
                                .gclDtt(itemDto.getGclDtt())            // 품목구분
                                .gclNm(itemDto.getGclNm())              // 품목명
                                .gclQtt(itemDto.getGclQtt())            // 품목수량
                                .cur(itemDto.getCur())                  // 통화
                                .xcr(itemDto.getXcr())                  // 환율
                                .xcrBseDt(itemDto.getXcrBseDt())        // 환율기준일자
                                .bgFdtn(itemDto.getBgFdtn())            // 예산근거
                                .itdDt(itemDto.getItdDt())              // 도입시기
                                .dfrCle(itemDto.getDfrCle())            // 지급주기
                                .infPrtYn(itemDto.getInfPrtYn() == null ? "N" : itemDto.getInfPrtYn()) // 정보보호여부
                                .itrInfrYn(itemDto.getItrInfrYn() == null ? "N" : itemDto.getItrInfrYn()) // 통합인프라여부
                                .lstYn("Y")                             // 최종여부
                                .gclAmt(itemDto.getGclAmt())            // 품목금액
                                .build();
                        bitemmRepository.save(updatedItem); // JPA save → ID 있으므로 merge
                        processedGclMngNos.add(existingItem.getGclMngNo()); // 처리 완료 표시
                    }
                } else {
                    // === 신규 품목 추가 ===
                    // Oracle 시퀀스로 품목관리번호 채번
                    Long gclSeq = bitemmRepository.getNextSequenceValue();
                    String gclMngNo = String.format("GCL-%s-%04d", java.time.LocalDate.now().getYear(), gclSeq);

                    com.kdb.it.domain.entity.Bitemm newItem = com.kdb.it.domain.entity.Bitemm.builder()
                            .gclMngNo(gclMngNo)                         // 품목관리번호 (신규 채번)
                            .gclSno(++maxGclSno)                        // 품목일련번호 (MAX+1)
                            .prjMngNo(prjMngNo)                         // 프로젝트관리번호
                            .prjSno(project.getPrjSno())                // 프로젝트순번
                            .gclDtt(itemDto.getGclDtt())                // 품목구분
                            .gclNm(itemDto.getGclNm())                  // 품목명
                            .gclQtt(itemDto.getGclQtt())                // 품목수량
                            .cur(itemDto.getCur())                      // 통화
                            .xcr(itemDto.getXcr())                      // 환율
                            .xcrBseDt(itemDto.getXcrBseDt())            // 환율기준일자
                            .bgFdtn(itemDto.getBgFdtn())                // 예산근거
                            .itdDt(itemDto.getItdDt())                  // 도입시기
                            .dfrCle(itemDto.getDfrCle())                // 지급주기
                            .infPrtYn(itemDto.getInfPrtYn() == null ? "N" : itemDto.getInfPrtYn()) // 정보보호여부
                            .itrInfrYn(itemDto.getItrInfrYn() == null ? "N" : itemDto.getItrInfrYn()) // 통합인프라여부
                            .lstYn("Y")                                 // 최종여부
                            .gclAmt(itemDto.getGclAmt())                // 품목금액
                            .build();
                    bitemmRepository.save(newItem);
                }
            }

            // 3. 요청에 없는 기존 품목 Soft Delete 처리
            // processedGclMngNos에 포함되지 않은 기존 항목은 삭제 대상
            for (com.kdb.it.domain.entity.Bitemm existingItem : existingItems) {
                if (!processedGclMngNos.contains(existingItem.getGclMngNo())) {
                    existingItem.delete(); // BaseEntity.delete() → DEL_YN='Y'
                }
            }
        }

        return project.getPrjMngNo(); // 수정된 관리번호 반환
    }

    /**
     * 정보화사업 삭제 (Soft Delete)
     *
     * <p>프로젝트와 연결된 품목({@link com.kdb.it.domain.entity.Bitemm}) 모두를
     * {@code DEL_YN='Y'}로 논리 삭제합니다.</p>
     *
     * <p>결재 상태 확인: "결재중" 또는 "결재완료" 상태인 경우 삭제가 불가합니다.</p>
     *
     * @param prjMngNo 삭제할 프로젝트관리번호
     * @throws IllegalArgumentException 해당 관리번호의 프로젝트가 없는 경우
     * @throws IllegalStateException    결재중/결재완료 상태여서 삭제 불가한 경우
     */
    @Transactional
    public void deleteProject(String prjMngNo) {
        // 프로젝트 조회
        Project project = projectRepository.findById(prjMngNo)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + prjMngNo));

        // 결재 상태 확인 (결재중/결재완료이면 삭제 불가)
        boolean isProcessingOrApproved = capplaRepository.existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
                "BPRJTM", prjMngNo, project.getPrjSno(), java.util.List.of("결재중", "결재완료"));

        if (isProcessingOrApproved) {
            throw new IllegalStateException("결재중이거나 결재완료된 프로젝트는 삭제할 수 없습니다.");
        }

        // 1. 프로젝트 Soft Delete (DEL_YN='Y')
        project.delete();

        // 2. 관련 품목 전체 Soft Delete (DEL_YN 무관하게 모든 품목 조회 후 삭제)
        List<com.kdb.it.domain.entity.Bitemm> bitemms = bitemmRepository.findByPrjMngNoAndPrjSno(prjMngNo,
                project.getPrjSno());
        for (com.kdb.it.domain.entity.Bitemm bitemm : bitemms) {
            bitemm.delete(); // BaseEntity.delete() 호출 (DEL_YN='Y')
        }
    }

    /**
     * 정보화사업 일괄 조회
     *
     * <p>여러 프로젝트관리번호를 한 번에 조회합니다.
     * 존재하지 않는 항목은 결과에서 제외합니다 (null 필터링).</p>
     *
     * @param request 일괄 조회 요청 DTO (프로젝트관리번호 목록)
     * @return 존재하는 프로젝트의 응답 DTO 목록 (품목 정보 포함, 없는 항목 제외)
     */
    public List<ProjectDto.Response> getProjectsByIds(ProjectDto.BulkGetRequest request) {
        return request.getPrjMngNos().stream()
                .map(prjMngNo -> {
                    try {
                        return getProject(prjMngNo); // 개별 상세 조회 (품목 포함)
                    } catch (IllegalArgumentException e) {
                        return null; // 존재하지 않는 항목은 null로 처리
                    }
                })
                .filter(response -> response != null) // null 제거 (존재하지 않는 항목 제외)
                .toList();
    }

    /**
     * 프로젝트 응답 DTO에 신청서 정보 설정 (내부 헬퍼 메서드)
     *
     * <p>프로젝트관리번호와 순번으로 연결된 신청서(CAPPLA) 중 가장 최신 신청서를 조회하여
     * 응답 DTO에 신청관리번호({@code apfMngNo})와 결재상태({@code apfSts})를 설정합니다.</p>
     *
     * <p>조회 기준:</p>
     * <ul>
     *   <li>{@code ORC_TB_CD = 'BPRJTM'}: 프로젝트 원본 테이블 코드</li>
     *   <li>{@code ORC_PK_VL = prjMngNo}: 프로젝트관리번호</li>
     *   <li>{@code ORC_SNO_VL = prjSno}: 프로젝트순번</li>
     *   <li>최신순 정렬 ({@code APF_REL_SNO DESC})</li>
     * </ul>
     *
     * @param response 신청서 정보를 설정할 응답 DTO
     * @param prjMngNo 프로젝트관리번호
     * @param prjSno   프로젝트순번
     */
    private void setApplicationInfo(ProjectDto.Response response, String prjMngNo, Integer prjSno) {
        // BPRJTM 테이블 코드와 프로젝트 관리번호/순번으로 연결된 신청서 목록 조회 (최신순)
        List<com.kdb.it.domain.entity.Cappla> capplas = capplaRepository
                .findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc("BPRJTM", prjMngNo, prjSno);

        if (!capplas.isEmpty()) {
            com.kdb.it.domain.entity.Cappla cappla = capplas.get(0); // 가장 최신 신청서
            response.setApfMngNo(cappla.getApfMngNo()); // 신청관리번호 설정

            // 신청서 마스터에서 결재상태 조회 및 설정
            capplmRepository.findById(cappla.getApfMngNo())
                    .ifPresent(capplm -> response.setApfSts(capplm.getApfSts()));
        }
    }
}
