package com.kdb.it.domain.budget.work.repository;

import com.kdb.it.domain.budget.cost.entity.Bcostm;
import com.kdb.it.domain.budget.project.entity.Bitemm;

import java.math.BigDecimal;
import java.util.List;

/**
 * 예산(BBUGTM) QueryDSL 커스텀 리포지토리 인터페이스
 *
 * <p>
 * 결재완료 필터링 + 비목 접두어 매칭 등 복잡한 동적 쿼리를 정의합니다.
 * 기존 {@code ProjectRepositoryImpl}, {@code CostRepositoryImpl}의
 * CAPPLA+CAPPLM 서브쿼리 패턴을 재사용합니다.
 * </p>
 *
 * // Design Ref: §4.6 — BbugtmRepositoryCustom (QueryDSL)
 */
public interface BbugtmRepositoryCustom {

    /**
     * 결재완료 전산업무비(BCOSTM) 중 비목코드가 접두어와 매칭되는 목록 조회
     *
     * <p>
     * [조건]
     * 1. BCOSTM.DEL_YN = 'N' AND LST_YN = 'Y'
     * 2. BCOSTM.IOE_C LIKE '접두어%'
     * 3. CAPPLA-CAPPLM JOIN으로 최신 신청서의 APF_STS = '결재완료'
     * </p>
     *
     * @param prefix 편성비목 접두어 (예: "237")
     * @return 결재완료된 전산업무비 목록
     */
    List<Bcostm> findApprovedCostsByPrefix(String prefix, String bgYy);

    /**
     * 결재완료 품목(BITEMM) 중 품목구분이 접두어와 매칭되는 목록 조회
     *
     * <p>
     * [조건]
     * 1. BITEMM.DEL_YN = 'N' AND LST_YN = 'Y'
     * 2. BITEMM.GCL_DTT LIKE '접두어%'
     * 3. BITEMM의 상위 BPROJM이 결재완료 상태
     *    (CAPPLA.ORC_TB_CD = 'BPROJM' → 최신 CAPPLM.APF_STS = '결재완료')
     * 4. BPROJM.BG_YY = :bgYy
     * </p>
     *
     * @param prefix 편성비목 접두어 (예: "237")
     * @param bgYy   예산연도 (예: 2026)
     * @return 결재완료된 품목 목록
     */
    List<Bitemm> findApprovedItemsByPrefix(String prefix, String bgYy);

    /**
     * 비목 접두어별 결재완료 요청금액 합계 조회
     *
     * <p>
     * 편성비목 조회(API-01) 시 각 비목별 결재완료 요청금액을 집계합니다.
     * BCOSTM.IT_MNGC_BG + BITEMM.GCL_AMT를 접두어별로 SUM합니다.
     * </p>
     *
     * @param prefix 편성비목 접두어 (예: "237")
     * @param bgYy   예산연도 (예: 2026)
     * @return 해당 접두어의 결재완료 요청금액 합계
     */
    BigDecimal sumApprovedAmountByPrefix(String prefix, String bgYy);
}
