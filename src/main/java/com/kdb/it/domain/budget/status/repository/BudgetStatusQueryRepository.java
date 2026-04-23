package com.kdb.it.domain.budget.status.repository;

import com.kdb.it.domain.budget.status.dto.BudgetStatusDto;

import java.util.List;

/**
 * 예산 현황 QueryDSL 쿼리 인터페이스
 *
 * <p>
 * 3개 탭(정보화사업/전산업무비/경상사업)별 전용 쿼리를 정의합니다.
 * 각 쿼리는 DB 레벨에서 조인+피벗을 처리하여 단일 호출로 정제된 데이터를 반환합니다.
 * </p>
 *
 * // Design Ref: §3.4 — BudgetStatusQueryRepository 설계
 */
public interface BudgetStatusQueryRepository {

    /**
     * 정보화사업 예산 현황 조회
     *
     * <p>BPROJM(ORN_YN!='Y', LST_YN='Y') LEFT JOIN BITEMM(품목구분별 피벗) LEFT JOIN BBUGTM(비목별 피벗)</p>
     *
     * @param bgYy 예산년도
     * @return 정보화사업별 편성요청/조정 금액 목록
     */
    List<BudgetStatusDto.ProjectResponse> findProjectStatus(String bgYy);

    /**
     * 전산업무비 예산 현황 조회
     *
     * <p>BCOSTM(LST_YN='Y') LEFT JOIN BBUGTM(비목별 매핑)</p>
     *
     * @param bgYy 예산년도
     * @return 전산업무비별 편성요청/조정 금액 목록
     */
    List<BudgetStatusDto.CostResponse> findCostStatus(String bgYy);

    /**
     * 경상사업 예산 현황 조회
     *
     * <p>BPROJM(ORN_YN='Y', LST_YN='Y') LEFT JOIN BITEMM(기계장치/기타무형자산 분리)</p>
     *
     * @param bgYy 예산년도
     * @return 경상사업별 기계장치/기타무형자산 상세 목록
     */
    List<BudgetStatusDto.OrdinaryResponse> findOrdinaryStatus(String bgYy);
}
