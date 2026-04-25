package com.kdb.it.domain.budget.cost.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.kdb.it.common.approval.repository.ApplicationMapRepository;
import com.kdb.it.common.approval.repository.ApplicationRepository;
import com.kdb.it.common.approval.repository.ApproverRepository;
import com.kdb.it.common.code.repository.CodeRepository;
import com.kdb.it.common.code.service.CodeService;
import com.kdb.it.common.iam.repository.OrganizationRepository;
import com.kdb.it.common.iam.repository.UserRepository;
import com.kdb.it.domain.budget.cost.dto.CostDto;
import com.kdb.it.domain.budget.cost.repository.BtermmRepository;
import com.kdb.it.domain.budget.cost.repository.CostRepository;

/**
 * CostService 단위 테스트
 *
 * <p>
 * 전산관리비 서비스의 단건 조회·수정·삭제(Soft Delete) 예외 경로와
 * 일괄 조회의 누락 항목 필터링 동작을 검증합니다.
 * 수정/삭제의 권한 검증(SecurityContextHolder)은 조회 실패 시 도달하지 않으므로
 * 이 테스트에서 별도 설정 없이 검증 가능합니다.
 * Oracle DB 없이 실행됩니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CostServiceTest {

    @Mock private CostRepository costRepository;
    @Mock private BtermmRepository btermmRepository;
    @Mock private ApplicationMapRepository capplaRepository;
    @Mock private ApplicationRepository capplmRepository;
    @Mock private OrganizationRepository corgnIRepository;
    @Mock private UserRepository cuserIRepository;
    @Mock private ApproverRepository cdecimRepository;
    @Mock private CodeRepository ccodemRepository;
    @Mock private CodeService codeService;

    @InjectMocks
    private CostService costService;

    private static final String IT_MNGC_NO = "COST_2026_0001";

    // ───────────────────────────────────────────────────────
    // getCost
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getCost: 존재하지 않는 관리번호이면 IllegalArgumentException을 던진다")
    void getCost_존재하지않는관리번호_IllegalArgumentException발생() {
        given(costRepository.findByItMngcNoAndDelYn(IT_MNGC_NO, "N")).willReturn(List.of());

        assertThatThrownBy(() -> costService.getCost(IT_MNGC_NO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(IT_MNGC_NO);
    }

    // ───────────────────────────────────────────────────────
    // updateCost
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateCost: 존재하지 않는 관리번호이면 IllegalArgumentException을 던진다")
    void updateCost_존재하지않는관리번호_IllegalArgumentException발생() {
        given(costRepository.findByItMngcNoAndDelYn(IT_MNGC_NO, "N")).willReturn(List.of());

        assertThatThrownBy(() -> costService.updateCost(IT_MNGC_NO, new CostDto.UpdateRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(IT_MNGC_NO);
    }

    // ───────────────────────────────────────────────────────
    // deleteCost
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCost: 존재하지 않는 관리번호이면 IllegalArgumentException을 던진다")
    void deleteCost_존재하지않는관리번호_IllegalArgumentException발생() {
        given(costRepository.findByItMngcNoAndDelYn(IT_MNGC_NO, "N")).willReturn(List.of());

        assertThatThrownBy(() -> costService.deleteCost(IT_MNGC_NO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(IT_MNGC_NO);
    }

    // ───────────────────────────────────────────────────────
    // getCostsByIds
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getCostsByIds: 존재하지 않는 관리번호는 결과에서 제외하고 빈 목록을 반환한다")
    void getCostsByIds_존재하지않는항목_필터링빈목록반환() {
        given(costRepository.findByItMngcNoAndDelYn(any(), eq("N"))).willReturn(List.of());

        CostDto.BulkGetRequest request = new CostDto.BulkGetRequest(
                List.of("COST_NOTEXIST1", "COST_NOTEXIST2"));

        List<CostDto.Response> result = costService.getCostsByIds(request);

        assertThat(result).isEmpty();
    }
}
