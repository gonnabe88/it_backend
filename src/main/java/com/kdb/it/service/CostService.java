package com.kdb.it.service;

import com.kdb.it.domain.entity.Bcostm;
import com.kdb.it.dto.CostDto;
import com.kdb.it.repository.BcostmRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전산관리비(IT 관리비) 서비스
 *
 * <p>전산관리비(TAAABB_BCOSTM) 엔티티의 생성, 조회, 수정, 삭제(Soft Delete)
 * 비즈니스 로직을 처리합니다.</p>
 *
 * <p>복합키 구조:</p>
 * <ul>
 *   <li>{@code IT_MNGC_NO} (전산관리비관리번호): 논리적 식별자</li>
 *   <li>{@code IT_MNGC_SNO} (전산관리비일련번호): 동일 관리번호 내의 이력 순번</li>
 * </ul>
 *
 * <p>조회/수정/삭제는 {@code IT_MNGC_NO}를 기준으로 하며,
 * {@code LST_YN='Y'}인 항목(최신 이력)을 대상으로 처리합니다.</p>
 *
 * <p>Soft Delete 패턴: {@code DEL_YN='Y'}로 변경하여 논리 삭제합니다.</p>
 *
 * <p>{@code @Transactional(readOnly = true)}: 조회 메서드의 기본값.
 * 쓰기 메서드는 {@code @Transactional}로 오버라이드합니다.</p>
 */
@Service                              // Spring 서비스 빈으로 등록
@RequiredArgsConstructor              // final 필드 생성자 자동 주입 (Lombok)
@Transactional(readOnly = true)       // 기본 읽기 전용 트랜잭션
public class CostService {

    /** 전산관리비 데이터 접근 리포지토리 (TAAABB_BCOSTM) */
    private final BcostmRepository bcostmRepository;

    /**
     * 특정 전산관리비 단건 조회
     *
     * <p>{@code IT_MNGC_NO}로 삭제되지 않은({@code DEL_YN='N'}) 항목을 조회합니다.
     * 동일 관리번호에 여러 이력(SNO)이 있을 수 있으므로, 첫 번째 항목을 반환합니다.</p>
     *
     * <p>비즈니스 규칙상 {@code IT_MNGC_NO}가 유니크하게 관리된다면 목록 크기는 1입니다.</p>
     *
     * @param itMngcNo 조회할 전산관리비관리번호
     * @return 전산관리비 응답 DTO
     * @throws IllegalArgumentException 해당 관리번호의 항목이 없는 경우
     */
    public CostDto.Response getCost(String itMngcNo) {
        // IT_MNGC_NO로 삭제되지 않은 항목 목록 조회
        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }
        // 편의상 첫 번째 항목 반환 (IT_MNGC_NO가 유니크하다고 가정)
        return CostDto.Response.fromEntity(costs.get(0));
    }

    /**
     * 전체 전산관리비 목록 조회
     *
     * <p>삭제되지 않은({@code DEL_YN='N'}) 모든 전산관리비를 조회하여
     * DTO 목록으로 변환하여 반환합니다.</p>
     *
     * @return 전체 전산관리비 응답 DTO 목록
     */
    public List<CostDto.Response> getCostList() {
        return bcostmRepository.findAllByDelYn("N").stream()
                .map(CostDto.Response::fromEntity) // 각 엔티티를 DTO로 변환
                .toList();
    }

    /**
     * 신규 전산관리비 생성
     *
     * <p>전산관리비관리번호({@code IT_MNGC_NO})가 없으면 Oracle 시퀀스로 자동 채번합니다.
     * 일련번호({@code IT_MNGC_SNO})는 기존 데이터 기준 MAX+1로 설정합니다.</p>
     *
     * <p>관리번호 자동 생성 형식: {@code COST_{yyyy}_{seq:04d}}</p>
     * <p>예: {@code COST_2026_0001}</p>
     *
     * <p>일련번호(SNO) 채번:</p>
     * <ul>
     *   <li>기존 데이터가 없으면 1</li>
     *   <li>기존 데이터가 있으면 MAX(IT_MNGC_SNO) + 1</li>
     * </ul>
     *
     * @param request 전산관리비 생성 요청 DTO (관리번호, 비목명, 계약 정보 등)
     * @return 생성된 전산관리비관리번호
     */
    @Transactional
    public String createCost(CostDto.CreateRequest request) {
        String itMngcNo = request.getItMngcNo();

        // IT_MNGC_NO가 없으면 Oracle 시퀀스로 자동 생성
        if (itMngcNo == null || itMngcNo.isEmpty()) {
            Long seq = bcostmRepository.getNextSequenceValue(); // Oracle 시퀀스 채번
            String year = String.valueOf(java.time.LocalDate.now().getYear()); // 현재 연도
            // 형식: COST_{yyyy}_{seq:04d}
            itMngcNo = String.format("COST_%s_%04d", year, seq);
            request.setItMngcNo(itMngcNo); // 요청 객체에 생성된 번호 설정
        }

        // IT_MNGC_SNO 채번: 기존 데이터의 MAX + 1 (없으면 1)
        Integer nextSno = bcostmRepository.getNextSnoValue(itMngcNo);
        if (nextSno == null) {
            nextSno = 1; // 첫 번째 이력
        }

        // 엔티티 생성 및 저장 (LST_YN='Y': 최신 이력)
        Bcostm bcostm = request.toEntity(nextSno);
        bcostmRepository.save(bcostm);

        return bcostm.getItMngcNo(); // 저장된 관리번호 반환
    }

    /**
     * 전산관리비 수정
     *
     * <p>{@code IT_MNGC_NO}로 조회된 항목 중 {@code LST_YN='Y'}인 최신 이력을 수정합니다.
     * 최신 이력이 없으면 첫 번째 항목을 수정 대상으로 사용합니다.</p>
     *
     * <p>JPA Dirty Checking: 조회된 엔티티의 필드를 변경하면 트랜잭션 종료 시
     * 자동으로 UPDATE 쿼리가 실행됩니다.</p>
     *
     * @param itMngcNo 수정할 전산관리비관리번호
     * @param request  수정 요청 DTO (비목명, 계약 정보, 예산 등)
     * @return 수정된 전산관리비관리번호
     * @throws IllegalArgumentException 해당 관리번호의 항목이 없는 경우
     */
    @Transactional
    public String updateCost(String itMngcNo, CostDto.UpdateRequest request) {
        // IT_MNGC_NO로 삭제되지 않은 항목 목록 조회
        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }

        // LST_YN='Y'인 최신 이력 항목 찾기 (없으면 첫 번째 항목 사용)
        Bcostm target = costs.stream()
                .filter(c -> "Y".equals(c.getLstYn())) // 최신 이력 필터
                .findFirst()
                .orElse(costs.get(0)); // 최신 이력이 없으면 첫 번째 항목

        // 엔티티 필드 업데이트 (JPA Dirty Checking으로 자동 반영)
        target.update(
                request.getIoeNm(),      // 비목명
                request.getCttNm(),      // 계약명
                request.getCttTp(),      // 계약구분
                request.getCttOpp(),     // 계약상대처
                request.getItMngcBg(),   // 전산관리비예산
                request.getDfrCle(),     // 지급주기
                request.getFstDfrDt(),   // 지급예정월 (최초지급일자)
                request.getCur(),        // 통화
                request.getXcr(),        // 환율
                request.getXcrBseDt(),   // 환율기준일자
                request.getInfPrtYn(),   // 정보보호여부
                request.getIndRsn(),     // 증감사유
                request.getPulCgpr());   // 추진담당자

        return target.getItMngcNo(); // 수정된 관리번호 반환
    }

    /**
     * 전산관리비 삭제 (Soft Delete)
     *
     * <p>{@code IT_MNGC_NO}에 해당하는 모든 이력(SNO)에 대해 {@code DEL_YN='Y'}로 설정합니다.</p>
     *
     * <p>물리 삭제(DELETE)를 수행하지 않으며, 논리적으로 삭제 처리합니다.</p>
     *
     * @param itMngcNo 삭제할 전산관리비관리번호
     * @throws IllegalArgumentException 해당 관리번호의 항목이 없는 경우
     */
    @Transactional
    public void deleteCost(String itMngcNo) {
        // 삭제되지 않은 항목 목록 조회
        List<Bcostm> costs = bcostmRepository.findByItMngcNoAndDelYn(itMngcNo, "N");
        if (costs.isEmpty()) {
            throw new IllegalArgumentException("Cost not found with id: " + itMngcNo);
        }

        // 모든 이력에 대해 Soft Delete 처리 (DEL_YN='Y')
        for (Bcostm cost : costs) {
            cost.delete(); // BaseEntity.delete() 호출 → DEL_YN='Y'
        }
    }

    /**
     * 전산관리비 일괄 조회
     *
     * <p>여러 관리번호를 한 번에 조회합니다. 존재하지 않는 항목은 결과에서 제외합니다.</p>
     *
     * @param request 일괄 조회 요청 DTO (전산관리비관리번호 목록)
     * @return 존재하는 항목의 응답 DTO 목록 (없는 항목 제외)
     */
    public List<CostDto.Response> getCostsByIds(CostDto.BulkGetRequest request) {
        return request.getItMngcNos().stream()
                .map(itMngcNo -> {
                    try {
                        return getCost(itMngcNo); // 개별 조회
                    } catch (IllegalArgumentException e) {
                        return null; // 존재하지 않는 항목은 null로 처리
                    }
                })
                .filter(response -> response != null) // null 제거 (존재하지 않는 항목 제외)
                .toList();
    }
}
