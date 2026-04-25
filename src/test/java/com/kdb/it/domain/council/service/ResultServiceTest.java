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
import com.kdb.it.domain.council.entity.Basctm;
import com.kdb.it.domain.council.entity.Brsltm;
import com.kdb.it.domain.council.repository.ResultRepository;

/**
 * ResultService 단위 테스트
 *
 * <p>
 * 협의회 결과서 서비스의 조회·저장·확정 메서드를 검증합니다.
 * Basctm·Brsltm 엔티티는 protected 생성자를 우회하기 위해 Mockito.mock()으로 생성합니다.
 * CouncilService·EvaluationService는 @Mock으로 교체합니다.
 * Oracle DB 없이 실행됩니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResultServiceTest {

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private CouncilService councilService;

    @Mock
    private EvaluationService evaluationService;

    @InjectMocks
    private ResultService resultService;

    // ───────────────────────────────────────────────────────
    // getResult
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getResult: 결과서가 아직 없으면 avgScores만 채워진 빈 DTO를 반환한다")
    void getResult_결과서없음_빈DTO반환() {
        // given
        Basctm council = mock(Basctm.class);
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N")).willReturn(Optional.empty());
        given(evaluationService.buildAvgScores("ASCT-2026-0001")).willReturn(List.of());

        // when
        CouncilDto.ResultResponse result = resultService.getResult("ASCT-2026-0001");

        // then
        assertThat(result.synOpnn()).isNull();
        assertThat(result.ckgOpnn()).isNull();
        assertThat(result.flMngNo()).isNull();
        assertThat(result.avgScores()).isEmpty();
    }

    @Test
    @DisplayName("getResult: 결과서가 있으면 내용을 DTO로 반환한다")
    void getResult_결과서있음_DTO반환() {
        // given
        Basctm council = mock(Basctm.class);
        Brsltm resultEntity = mock(Brsltm.class);
        given(resultEntity.getSynOpnn()).willReturn("종합의견");
        given(resultEntity.getCkgOpnn()).willReturn("타당성검토의견");
        given(resultEntity.getFlMngNo()).willReturn("FL_00000001");

        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N"))
                .willReturn(Optional.of(resultEntity));
        given(evaluationService.buildAvgScores("ASCT-2026-0001")).willReturn(List.of());

        // when
        CouncilDto.ResultResponse result = resultService.getResult("ASCT-2026-0001");

        // then
        assertThat(result.synOpnn()).isEqualTo("종합의견");
        assertThat(result.ckgOpnn()).isEqualTo("타당성검토의견");
        assertThat(result.flMngNo()).isEqualTo("FL_00000001");
    }

    // ───────────────────────────────────────────────────────
    // saveResult
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("saveResult: 기존 결과서가 있으면 update()를 호출한다")
    void saveResult_기존결과서있음_update호출() {
        // given
        Basctm council = mock(Basctm.class);
        Brsltm existingResult = mock(Brsltm.class);
        given(council.getAsctSts()).willReturn("RESULT_WRITING");
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N"))
                .willReturn(Optional.of(existingResult));

        // when
        resultService.saveResult("ASCT-2026-0001",
                new CouncilDto.ResultRequest("종합의견", "타당성의견", null));

        // then
        verify(existingResult).update("종합의견", "타당성의견", null);
        verify(resultRepository, never()).save(any());
    }

    @Test
    @DisplayName("saveResult: 결과서가 없으면 신규 저장한다")
    void saveResult_결과서없음_신규저장() {
        // given
        Basctm council = mock(Basctm.class);
        given(council.getAsctSts()).willReturn("RESULT_WRITING");
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N")).willReturn(Optional.empty());

        // when
        resultService.saveResult("ASCT-2026-0001",
                new CouncilDto.ResultRequest("종합의견", "타당성의견", null));

        // then
        verify(resultRepository).save(any(Brsltm.class));
    }

    @Test
    @DisplayName("saveResult: 협의회 상태가 EVALUATING이면 RESULT_WRITING으로 상태를 전이한다")
    void saveResult_EVALUATING상태_RESULT_WRITING전이() {
        // given
        Basctm council = mock(Basctm.class);
        given(council.getAsctSts()).willReturn("EVALUATING");
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N")).willReturn(Optional.empty());

        // when
        resultService.saveResult("ASCT-2026-0001",
                new CouncilDto.ResultRequest("종합의견", "타당성의견", null));

        // then
        verify(councilService).changeStatus("ASCT-2026-0001", "RESULT_WRITING");
    }

    // ───────────────────────────────────────────────────────
    // confirmResult
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("confirmResult: 결과서가 없으면 IllegalStateException을 던진다")
    void confirmResult_결과서없음_IllegalStateException발생() {
        // given
        Basctm council = mock(Basctm.class);
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> resultService.confirmResult("ASCT-2026-0001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("결과서");
    }

    @Test
    @DisplayName("confirmResult: 결과서가 있으면 RESULT_REVIEW로 상태를 전이한다")
    void confirmResult_결과서있음_RESULT_REVIEW전이() {
        // given
        Basctm council = mock(Basctm.class);
        Brsltm resultEntity = mock(Brsltm.class);
        given(councilService.findActiveCouncil("ASCT-2026-0001")).willReturn(council);
        given(resultRepository.findByAsctIdAndDelYn("ASCT-2026-0001", "N"))
                .willReturn(Optional.of(resultEntity));

        // when
        resultService.confirmResult("ASCT-2026-0001");

        // then
        verify(councilService).changeStatus("ASCT-2026-0001", "RESULT_REVIEW");
    }
}
