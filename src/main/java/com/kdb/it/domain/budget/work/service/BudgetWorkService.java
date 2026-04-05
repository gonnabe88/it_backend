package com.kdb.it.domain.budget.work.service;

import com.kdb.it.common.code.entity.Ccodem;
import com.kdb.it.common.code.repository.CodeRepository;
import com.kdb.it.domain.budget.cost.entity.Bcostm;
import com.kdb.it.domain.budget.project.entity.Bitemm;
import com.kdb.it.domain.budget.work.dto.BudgetWorkDto;
import com.kdb.it.domain.budget.work.entity.Bbugtm;
import com.kdb.it.domain.budget.work.repository.BbugtmRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 예산 작업 서비스
 *
 * <p>
 * 편성비목 조회, 편성률 일괄 적용, 편성 결과 조회 등
 * 예산 편성 작업(TAAABB_BBUGTM)의 비즈니스 로직을 처리합니다.
 * </p>
 *
 * <p>
 * [핵심 알고리즘]
 * 1. CCODEM에서 CTT_TP='DUP_IOE'인 편성비목 코드 조회
 * 2. 각 비목의 접두어로 결재완료 BCOSTM/BITEMM 매칭
 * 3. 요청금액 × (편성률/100) = 편성금액 계산
 * 4. BBUGTM Upsert (기존 존재하면 UPDATE, 없으면 INSERT)
 * </p>
 *
 * // Design Ref: §4.4 — BudgetWorkService 핵심 로직
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetWorkService {

    /** 예산 데이터 접근 리포지토리 (TAAABB_BBUGTM) */
    private final BbugtmRepository bbugtmRepository;

    /** 공통코드 리포지토리 (TAAABB_CCODEM): 편성비목(DUP_IOE) 조회용 */
    private final CodeRepository codeRepository;

    /**
     * 편성비목 목록 조회 (API-01)
     *
     * <p>
     * CCODEM에서 CTT_TP='DUP_IOE'인 편성비목을 조회하고,
     * 각 비목별 결재완료 요청금액 합계와 기존 편성률을 함께 반환합니다.
     * </p>
     *
     * [처리 순서]
     * 1. CCODEM에서 CTT_TP = 'DUP_IOE' 조회
     * 2. 각 비목별 접두어 추출 (DUP-IOE-237 → "237")
     * 3. 결재완료 원본 데이터에서 접두어 매칭 금액 합계 조회
     * 4. 기존 BBUGTM에서 편성률 조회
     *
     * @param bgYy 예산년도
     * @return 편성비목 목록 (코드ID, 코드명, 접두어, 기존 편성률, 요청금액 합계)
     */
    public List<BudgetWorkDto.IoeCategoryResponse> getIoeCategories(String bgYy) {
        // 1. 편성비목 코드 조회 (CTT_TP = 'DUP_IOE')
        List<Ccodem> ioeCodes = codeRepository.findByCttTpWithValidDate("DUP_IOE", null);

        // 기존 BBUGTM 데이터 조회 (편성률 확인용)
        List<Bbugtm> existingBudgets = bbugtmRepository.findByBgYyAndDelYn(bgYy, "N");

        return ioeCodes.stream().map(code -> {
            String prefix = extractPrefix(code.getCdId());

            // 2. 결재완료 요청금액 합계
            BigDecimal requestAmount = bbugtmRepository.sumApprovedAmountByPrefix(prefix, bgYy);
            if (requestAmount == null) {
                requestAmount = BigDecimal.ZERO;
            }

            // 3. 기존 편성률 조회
            Integer dupRt = existingBudgets.stream()
                    .filter(b -> b.getIoeC() != null && b.getIoeC().startsWith(prefix))
                    .map(Bbugtm::getDupRt)
                    .findFirst()
                    .orElse(null);

            return new BudgetWorkDto.IoeCategoryResponse(
                    code.getCdId(), code.getCdNm(), code.getCdva(),
                    prefix, dupRt, requestAmount);
        }).toList();
    }

    /**
     * 편성률 일괄 적용 (API-02)
     *
     * <p>
     * 각 비목별 편성률을 결재완료 원본 데이터에 적용하여 BBUGTM에 저장합니다.
     * Upsert 패턴: (BG_YY, ORC_TB, ORC_PK_VL, ORC_SNO_VL, IOE_C)로 기존 레코드 확인 후
     * 존재하면 UPDATE, 없으면 INSERT.
     * </p>
     *
     * [처리 순서]
     * 1. rates 배열 순회
     * 2. cdId에서 접두어 추출 (DUP-IOE-237 → "237")
     * 3. 결재완료 BCOSTM 조회 (IOE_C LIKE '접두어%')
     * 4. 결재완료 BITEMM 조회 (GCL_DTT LIKE '접두어%')
     * 5. 각 레코드: 편성금액 = 요청금액 × (dupRt / 100), ROUND HALF UP
     * 6. Upsert BBUGTM
     *
     * // Plan SC: SC-03 — 편성금액 = Math.round(요청금액 × 편성률 / 100)
     * // Plan SC: SC-05 — Upsert 동작 (중복 INSERT 방지)
     *
     * @param request 편성률 적용 요청 (예산년도 + 비목별 편성률 목록)
     * @return 적용 결과 (처리 메시지, 레코드 수, 요약)
     */
    @Transactional
    public BudgetWorkDto.ApplyResponse applyRates(BudgetWorkDto.ApplyRequest request) {
        String bgYy = request.bgYy();
        String bgMngNo = bbugtmRepository.generateBgMngNo(bgYy);
        int snoCounter = 0;
        int totalRecords = 0;

        for (BudgetWorkDto.RateItem rate : request.rates()) {
            String prefix = extractPrefix(rate.cdId());
            Integer dupRt = rate.dupRt();

            // 결재완료 BCOSTM 처리
            List<Bcostm> costs = bbugtmRepository.findApprovedCostsByPrefix(prefix, bgYy);
            for (Bcostm cost : costs) {
                BigDecimal dupBg = calculateDupBg(cost.getItMngcBg(), dupRt);

                Optional<Bbugtm> existing = bbugtmRepository
                        .findByBgYyAndOrcTbAndOrcPkVlAndOrcSnoVlAndIoeCAndDelYn(
                                bgYy, "BCOSTM", cost.getItMngcNo(),
                                cost.getItMngcSno(), cost.getIoeC(), "N");

                if (existing.isPresent()) {
                    // Upsert: UPDATE (JPA Dirty Checking)
                    existing.get().update(dupBg, dupRt);
                } else {
                    // Upsert: INSERT
                    snoCounter++;
                    Bbugtm bbugtm = Bbugtm.builder()
                            .bgMngNo(bgMngNo)
                            .bgSno(snoCounter)
                            .bgYy(bgYy)
                            .orcTb("BCOSTM")
                            .orcPkVl(cost.getItMngcNo())
                            .orcSnoVl(cost.getItMngcSno())
                            .ioeC(cost.getIoeC())
                            .dupBg(dupBg)
                            .dupRt(dupRt)
                            .build();
                    bbugtmRepository.save(bbugtm);
                }
                totalRecords++;
            }

            // 결재완료 BITEMM 처리
            // ORC_TB = "BITEMM": BITEMM은 자체 PK(GCL_MNG_NO + GCL_SNO)를 보유하므로
            // 개별 품목 단위로 추적 가능. Plan 설계 문서의 "BPROJM"은 결재 조회 대상을
            // 지칭한 것이며, BBUGTM에 저장 시 실제 원본은 BITEMM임.
            List<Bitemm> items = bbugtmRepository.findApprovedItemsByPrefix(prefix, bgYy);
            for (Bitemm item : items) {
                // 환율 적용: gclAmt × coalesce(xcr, 1) → 원화 금액
                BigDecimal xcrVal = item.getXcr() != null ? item.getXcr() : BigDecimal.ONE;
                BigDecimal amountKrw = item.getGclAmt() != null ? item.getGclAmt().multiply(xcrVal) : BigDecimal.ZERO;
                BigDecimal dupBg = calculateDupBg(amountKrw, dupRt);

                Optional<Bbugtm> existing = bbugtmRepository
                        .findByBgYyAndOrcTbAndOrcPkVlAndOrcSnoVlAndIoeCAndDelYn(
                                bgYy, "BITEMM", item.getGclMngNo(),
                                item.getGclSno(), item.getGclDtt(), "N");

                if (existing.isPresent()) {
                    existing.get().update(dupBg, dupRt);
                } else {
                    snoCounter++;
                    Bbugtm bbugtm = Bbugtm.builder()
                            .bgMngNo(bgMngNo)
                            .bgSno(snoCounter)
                            .bgYy(bgYy)
                            .orcTb("BITEMM")
                            .orcPkVl(item.getGclMngNo())
                            .orcSnoVl(item.getGclSno())
                            .ioeC(item.getGclDtt())
                            .dupBg(dupBg)
                            .dupRt(dupRt)
                            .build();
                    bbugtmRepository.save(bbugtm);
                }
                totalRecords++;
            }
        }

        BudgetWorkDto.SummaryResponse summary = getSummary(bgYy);
        return new BudgetWorkDto.ApplyResponse("편성률 적용 완료", totalRecords, summary);
    }

    /**
     * 편성 결과 조회 (API-03)
     *
     * <p>
     * BBUGTM에서 예산년도별 데이터를 조회하고, 비목 접두어 기준으로 그룹핑하여
     * 요청금액/편성금액 합계를 반환합니다.
     * </p>
     *
     * [처리 순서]
     * 1. BBUGTM에서 BG_YY = :bgYy AND DEL_YN = 'N' 조회
     * 2. CCODEM에서 DUP_IOE 코드 조회 (비목명 매핑용)
     * 3. 접두어 기준 GROUP BY → SUM(요청금액), SUM(편성금액) 집계
     *
     * @param bgYy 예산년도
     * @return 비목별 요약 목록 + 합계
     */
    public BudgetWorkDto.SummaryResponse getSummary(String bgYy) {
        List<Bbugtm> budgets = bbugtmRepository.findByBgYyAndDelYn(bgYy, "N");

        // 비목명 매핑용 코드 조회
        List<Ccodem> ioeCodes = codeRepository.findByCttTpWithValidDate("DUP_IOE", null);

        List<BudgetWorkDto.SummaryItem> items = new ArrayList<>();
        BigDecimal totalRequest = BigDecimal.ZERO;
        BigDecimal totalDup = BigDecimal.ZERO;

        for (Ccodem code : ioeCodes) {
            String prefix = extractPrefix(code.getCdId());

            // 편성금액 합계 (BBUGTM에서 접두어 매칭)
            BigDecimal dupAmount = budgets.stream()
                    .filter(b -> b.getIoeC() != null && b.getIoeC().startsWith(prefix))
                    .map(Bbugtm::getDupBg)
                    .filter(v -> v != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 요청금액 합계 (결재완료 원본 데이터)
            BigDecimal requestAmount = bbugtmRepository.sumApprovedAmountByPrefix(prefix, bgYy);
            if (requestAmount == null) {
                requestAmount = BigDecimal.ZERO;
            }

            // 편성률 (동일 접두어 내 동일한 값)
            Integer dupRt = budgets.stream()
                    .filter(b -> b.getIoeC() != null && b.getIoeC().startsWith(prefix))
                    .map(Bbugtm::getDupRt)
                    .findFirst()
                    .orElse(0);

            items.add(new BudgetWorkDto.SummaryItem(
                    code.getCdNm(), prefix, requestAmount, dupAmount, dupRt));

            totalRequest = totalRequest.add(requestAmount);
            totalDup = totalDup.add(dupAmount);
        }

        return new BudgetWorkDto.SummaryResponse(
                items,
                new BudgetWorkDto.SummaryTotals(totalRequest, totalDup));
    }

    /**
     * 편성금액 계산
     *
     * <p>편성금액 = 요청금액 × (편성률 / 100), HALF_UP 반올림</p>
     *
     * @param requestAmount 요청금액
     * @param dupRt         편성률 (0~100)
     * @return 편성금액 (소수점 2자리)
     */
    private BigDecimal calculateDupBg(BigDecimal requestAmount, Integer dupRt) {
        if (requestAmount == null || dupRt == null) {
            return BigDecimal.ZERO;
        }
        return requestAmount
                .multiply(BigDecimal.valueOf(dupRt))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * 편성비목 코드ID에서 접두어 추출
     *
     * <p>{@code DUP-IOE-237} → {@code "IOE-237"}</p>
     * <p>실제 IOE_C/GCL_DTT 값이 {@code IOE-237-0700} 형태이므로
     * {@code "DUP-"} 만 제거하여 {@code "IOE-237"} 접두어로 매칭합니다.</p>
     *
     * @param cdId 편성비목 코드ID (예: DUP-IOE-237)
     * @return 비목 접두어 (예: IOE-237)
     */
    private String extractPrefix(String cdId) {
        return cdId.replace("DUP-", "");
    }
}
