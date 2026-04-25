package com.kdb.it.domain.council.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.kdb.it.domain.council.dto.CouncilDto;
import com.kdb.it.domain.council.entity.Bpovwm;
import com.kdb.it.domain.council.repository.FeasibilityCheckRepository;
import com.kdb.it.domain.council.repository.PerformanceRepository;
import com.kdb.it.domain.council.repository.ProjectOverviewRepository;

/**
 * FeasibilityService 단위 테스트
 *
 * <p>
 * 타당성검토표 서비스의 조회·저장(임시/완료) 메서드를 검증합니다.
 * Bpovwm 엔티티는 protected 생성자를 우회하기 위해 Mockito.mock()으로 생성합니다.
 * EntityManager(@PersistenceContext)는 성과지표 교체 경로(replacePerformances)에만
 * 사용되므로, 해당 경로를 포함하지 않는 테스트에서는 주입하지 않습니다.
 * Oracle DB 없이 실행됩니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FeasibilityServiceTest {

    @Mock private ProjectOverviewRepository projectOverviewRepository;
    @Mock private FeasibilityCheckRepository feasibilityCheckRepository;
    @Mock private PerformanceRepository performanceRepository;
    @Mock private CouncilService councilService;

    @InjectMocks
    private FeasibilityService feasibilityService;

    private static final String ASCT_ID = "ASCT-2026-0001";

    // ───────────────────────────────────────────────────────
    // getFeasibility
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getFeasibility: 사업개요 미작성이면 null을 반환한다")
    void getFeasibility_미작성상태_null반환() {
        given(projectOverviewRepository.findByAsctIdAndDelYn(ASCT_ID, "N")).willReturn(Optional.empty());

        CouncilDto.FeasibilityResponse result = feasibilityService.getFeasibility(ASCT_ID);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getFeasibility: 사업개요·자체점검·성과지표를 통합한 응답을 반환하고 점검항목은 6개 고정이다")
    void getFeasibility_데이터있음_통합응답반환() {
        Bpovwm overview = mock(Bpovwm.class);
        given(overview.getPrjNm()).willReturn("테스트사업");
        given(overview.getPrjTrm()).willReturn("2026");
        given(overview.getLglRglYn()).willReturn("N");
        given(overview.getKpnTp()).willReturn("COMPLETE");
        given(projectOverviewRepository.findByAsctIdAndDelYn(ASCT_ID, "N")).willReturn(Optional.of(overview));
        given(feasibilityCheckRepository.findByAsctIdAndDelYn(ASCT_ID, "N")).willReturn(List.of());
        given(performanceRepository.findByAsctIdAndDelYnOrderByDtpSnoAsc(ASCT_ID, "N")).willReturn(List.of());

        CouncilDto.FeasibilityResponse result = feasibilityService.getFeasibility(ASCT_ID);

        assertThat(result).isNotNull();
        assertThat(result.prjNm()).isEqualTo("테스트사업");
        assertThat(result.checkItems()).hasSize(6);
        assertThat(result.performances()).isEmpty();
    }

    // ───────────────────────────────────────────────────────
    // saveFeasibility — 유효성 검증
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("saveFeasibility: COMPLETE 타입에 첨부파일이 없으면 IllegalArgumentException을 던진다")
    void saveFeasibility_COMPLETE_첨부파일없음_IllegalArgumentException발생() {
        CouncilDto.FeasibilityRequest request = new CouncilDto.FeasibilityRequest(
                "테스트사업", "2026", null, null, null, null, "N", null, null,
                "COMPLETE", null, null, null);

        assertThatThrownBy(() -> feasibilityService.saveFeasibility(ASCT_ID, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("필수");
    }

    // ───────────────────────────────────────────────────────
    // saveFeasibility — 상태 전이
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("saveFeasibility: COMPLETE 타입 정상 요청이면 SUBMITTED 상태로 전이한다")
    void saveFeasibility_COMPLETE_정상요청_SUBMITTED전이() {
        CouncilDto.FeasibilityRequest request = new CouncilDto.FeasibilityRequest(
                "테스트사업", "2026", null, null, null, null, "N", null, null,
                "COMPLETE", null, null, "FL_00000001");
        given(projectOverviewRepository.findByAsctIdAndDelYn(ASCT_ID, "N")).willReturn(Optional.empty());

        feasibilityService.saveFeasibility(ASCT_ID, request);

        verify(councilService).changeStatus(ASCT_ID, "SUBMITTED");
    }

    @Test
    @DisplayName("saveFeasibility: TEMP 타입이면 상태 전이를 수행하지 않는다")
    void saveFeasibility_TEMP_상태전이없음() {
        CouncilDto.FeasibilityRequest request = new CouncilDto.FeasibilityRequest(
                "테스트사업", "2026", null, null, null, null, "N", null, null,
                "TEMP", null, null, null);
        given(projectOverviewRepository.findByAsctIdAndDelYn(ASCT_ID, "N")).willReturn(Optional.empty());

        feasibilityService.saveFeasibility(ASCT_ID, request);

        verify(councilService, never()).changeStatus(any(), any());
    }
}
