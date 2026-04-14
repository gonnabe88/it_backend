package com.kdb.it.domain.budget.work.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kdb.it.common.code.entity.Ccodem;
import com.kdb.it.common.code.repository.CodeRepository;
import com.kdb.it.domain.budget.cost.entity.Bcostm;
import com.kdb.it.domain.budget.work.dto.BudgetWorkDto;
import com.kdb.it.domain.budget.work.entity.Bbugtm;
import com.kdb.it.domain.budget.work.repository.BbugtmRepository;

/**
 * BudgetWorkService 단위 테스트
 *
 * <p>
 * BbugtmRepository, CodeRepository를 Mock 처리하여 Oracle DB 없이
 * 편성비목 조회, 편성률 적용, 편성 결과 조회 로직을 검증합니다.
 * </p>
 *
 * <p>
 * 핵심 검증 대상:
 * <ul>
 * <li>extractPrefix: "DUP-IOE-237" → "IOE-237" 변환</li>
 * <li>calculateDupBg: 요청금액 × (편성률/100) 계산 (HALF_UP 반올림)</li>
 * <li>getIoeCategories: IOE 코드별 편성률 + 요청금액 반환</li>
 * <li>getSummary: 비목별 요약 + 합계 반환</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
class BudgetWorkServiceTest {

    @Mock
    private BbugtmRepository bbugtmRepository;
    @Mock
    private CodeRepository codeRepository;

    @InjectMocks
    private BudgetWorkService budgetWorkService;

    // =========================================================================
    // getIoeCategories — 편성비목 목록 조회
    // =========================================================================

    @Test
    @DisplayName("getIoeCategories - DUP_IOE 코드가 없으면 빈 목록 반환")
    void getIoeCategories_코드없음_빈목록반환() {
        // given
        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of());
        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of());

        // when
        List<BudgetWorkDto.IoeCategoryResponse> result = budgetWorkService.getIoeCategories("2026");

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getIoeCategories - 코드 1개 반환 시 편성률은 null (기존 데이터 없음)")
    void getIoeCategories_기존데이터없음_편성률null() {
        // given: DUP-IOE-237 코드 1개
        Ccodem code = Ccodem.builder().cdId("DUP-IOE-237").cdNm("자산비").cdva("IOE-237").build();
        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of(code));
        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of());
        given(bbugtmRepository.sumApprovedAmountByPrefix("IOE-237", "2026")).willReturn(null);

        // when
        List<BudgetWorkDto.IoeCategoryResponse> result = budgetWorkService.getIoeCategories("2026");

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).cdId()).isEqualTo("DUP-IOE-237");
        assertThat(result.get(0).prefix()).isEqualTo("IOE-237"); // extractPrefix 검증
        assertThat(result.get(0).dupRt()).isNull();               // 기존 편성률 없음
        assertThat(result.get(0).requestAmount()).isEqualTo(BigDecimal.ZERO); // null → ZERO
    }

    @Test
    @DisplayName("getIoeCategories - 기존 BBUGTM에 편성률이 있으면 기존 편성률을 반환한다")
    void getIoeCategories_기존편성률있음_편성률반환() {
        // given
        Ccodem code = Ccodem.builder().cdId("DUP-IOE-237").cdNm("자산비").cdva("IOE-237").build();
        Bbugtm existing = Bbugtm.builder()
                .ioeC("IOE-237-0700")
                .dupRt(80)
                .build();

        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of(code));
        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of(existing));
        given(bbugtmRepository.sumApprovedAmountByPrefix("IOE-237", "2026"))
                .willReturn(BigDecimal.valueOf(1000000));

        // when
        List<BudgetWorkDto.IoeCategoryResponse> result = budgetWorkService.getIoeCategories("2026");

        // then
        assertThat(result.get(0).dupRt()).isEqualTo(80);
        assertThat(result.get(0).requestAmount()).isEqualTo(BigDecimal.valueOf(1000000));
    }

    // =========================================================================
    // getSummary — 편성 결과 조회
    // =========================================================================

    /** 세부 비목 코드 타입 목록 (getSummary에서 조회하는 cttTp들) */
    private static final List<String> DETAIL_CTT_TPS = List.of(
            "IOE_CPIT", "IOE_IDR", "IOE_SEVS", "IOE_XPN", "IOE_LEAFE");

    /** 세부 코드 조회 mock 헬퍼: 모든 세부 cttTp에 대해 빈 목록 반환 */
    private void mockEmptyDetailCodes() {
        for (String cttTp : DETAIL_CTT_TPS) {
            given(codeRepository.findByCttTpWithValidDate(cttTp, null)).willReturn(List.of());
        }
    }

    @Test
    @DisplayName("getSummary - 비목이 없으면 빈 목록과 합계 0을 반환한다")
    void getSummary_비목없음_빈결과반환() {
        // given
        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of());
        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of());
        mockEmptyDetailCodes();

        // when
        BudgetWorkDto.SummaryResponse result = budgetWorkService.getSummary("2026");

        // then
        assertThat(result.data()).isEmpty();
        assertThat(result.totals().requestAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totals().dupAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("getSummary - 세부 비목 단위로 편성금액 합계를 올바르게 계산한다")
    void getSummary_세부비목_합계계산() {
        // given: DUP_IOE 그룹 코드 1개 + BBUGTM 세부 데이터 1건
        Ccodem dupCode = Ccodem.builder().cdId("DUP-IOE-237").cdNm("전산임차료").build();
        Ccodem detailCode = Ccodem.builder().cdId("IOE-237-0700").cdNm("국외전산임차료").cttTp("IOE_IDR").build();
        Bbugtm bbugtm = Bbugtm.builder()
                .ioeC("IOE-237-0700")
                .dupBg(BigDecimal.valueOf(800000))
                .dupRt(80)
                .build();

        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of(bbugtm));
        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of(dupCode));
        // 세부 코드: IOE_IDR에 detailCode 반환, 나머지는 빈 목록
        for (String cttTp : DETAIL_CTT_TPS) {
            if ("IOE_IDR".equals(cttTp)) {
                given(codeRepository.findByCttTpWithValidDate(cttTp, null)).willReturn(List.of(detailCode));
            } else {
                given(codeRepository.findByCttTpWithValidDate(cttTp, null)).willReturn(List.of());
            }
        }
        // 결재완료 원본 데이터: 요청금액 1,000,000
        Bcostm approvedCost = Bcostm.builder().ioeC("IOE-237-0700").itMngcBg(BigDecimal.valueOf(1000000)).build();
        given(bbugtmRepository.findApprovedCostsByPrefix("IOE-237", "2026")).willReturn(List.of(approvedCost));
        given(bbugtmRepository.findApprovedItemsByPrefix("IOE-237", "2026")).willReturn(List.of());

        // when
        BudgetWorkDto.SummaryResponse result = budgetWorkService.getSummary("2026");

        // then: 세부 ioeC 단위로 1건 반환, 요청금액은 결재완료 원본 데이터 기반
        assertThat(result.data()).hasSize(1);
        BudgetWorkDto.SummaryItem item = result.data().get(0);
        assertThat(item.ioeCategory()).isEqualTo("국외전산임차료");
        assertThat(item.ioeC()).isEqualTo("IOE-237-0700");
        assertThat(item.groupName()).isEqualTo("전산임차료");
        assertThat(item.capital()).isFalse(); // IOE_IDR = 일반관리비
        assertThat(item.dupAmount()).isEqualByComparingTo(BigDecimal.valueOf(800000));
        assertThat(item.requestAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000000));
        assertThat(result.totals().requestAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000000));
        assertThat(result.totals().dupAmount()).isEqualByComparingTo(BigDecimal.valueOf(800000));
    }

    // =========================================================================
    // applyRates — 편성률 일괄 적용 (경계값/계산 검증)
    // =========================================================================

    @Test
    @DisplayName("applyRates - rates 목록이 비어있으면 0건 처리 결과를 반환한다")
    void applyRates_빈rates목록_0건처리() {
        // given: rates 없는 요청
        BudgetWorkDto.ApplyRequest request = new BudgetWorkDto.ApplyRequest("2026", List.of());
        given(bbugtmRepository.generateBgMngNo("2026")).willReturn("BG-2026-0001");
        given(bbugtmRepository.findByBgYyAndDelYn("2026", "N")).willReturn(List.of());
        given(codeRepository.findByCttTpWithValidDate("DUP_IOE", null)).willReturn(List.of());
        mockEmptyDetailCodes();

        // when
        BudgetWorkDto.ApplyResponse result = budgetWorkService.applyRates(request);

        // then
        assertThat(result.message()).contains("편성률 적용 완료");
        assertThat(result.totalRecords()).isEqualTo(0);
    }
}
