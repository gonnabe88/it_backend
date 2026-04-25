package com.kdb.it.common.approval.service;

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
import org.springframework.context.ApplicationEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdb.it.common.approval.dto.ApplicationDto;
import com.kdb.it.common.approval.entity.Capplm;
import com.kdb.it.common.approval.entity.Cdecim;
import com.kdb.it.common.approval.event.ApprovalCompletedEvent;
import com.kdb.it.common.approval.repository.ApplicationMapRepository;
import com.kdb.it.common.approval.repository.ApplicationRepository;
import com.kdb.it.common.approval.repository.ApproverRepository;
import com.kdb.it.domain.budget.cost.entity.Bcostm;
import com.kdb.it.domain.budget.cost.repository.CostRepository;
import com.kdb.it.domain.budget.project.entity.Bprojm;
import com.kdb.it.domain.budget.project.repository.ProjectRepository;

/**
 * ApplicationService 단위 테스트
 *
 * <p>
 * 결재 처리(approve), 신청서 조회(getApplication), 미상신 건수(getPendingCount)를 검증합니다.
 * Capplm 엔티티는 protected 생성자를 우회하기 위해 Mockito.mock()으로 생성합니다.
 * Cdecim 엔티티는 @SuperBuilder로 직접 구성합니다. Oracle DB 없이 실행됩니다.
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApplicationServiceTest {

    @Mock private ApplicationRepository applicationRepository;
    @Mock private ApproverRepository approverRepository;
    @Mock private ApplicationMapRepository applicationMapRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private ProjectRepository projectRepository;
    @Mock private CostRepository costRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ApplicationService applicationService;

    private static final String APF_MNG_NO = "APF_202600000001";

    /** Capplm Mock — getApfDtlCone() null로 updateApprovalLineInDetail 즉시 리턴 */
    private Capplm mockCapplm() {
        Capplm capplm = mock(Capplm.class);
        given(capplm.getApfDtlCone()).willReturn(null);
        return capplm;
    }

    /** 미결재(dcdTp=null) 상태의 Cdecim 생성 */
    private Cdecim pendingApprover(String eno, int sqn, String lstDcdYn) {
        return Cdecim.builder()
                .dcdMngNo(APF_MNG_NO)
                .dcdSqn(sqn)
                .dcdEno(eno)
                .lstDcdYn(lstDcdYn)
                .build();
    }

    /** 결재 요청 DTO 생성 헬퍼 */
    private ApplicationDto.ApproveRequest approveRequest(String eno, String sts) {
        ApplicationDto.ApproveRequest req = new ApplicationDto.ApproveRequest();
        req.setDcdEno(eno);
        req.setDcdOpnn("테스트의견");
        req.setDcdSts(sts);
        return req;
    }

    // ───────────────────────────────────────────────────────
    // approve — 예외 케이스
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("approve: 신청서가 없으면 IllegalArgumentException을 던진다")
    void approve_신청서없음_IllegalArgumentException발생() {
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.approve(APF_MNG_NO, approveRequest("E10001", "승인")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(APF_MNG_NO);
    }

    @Test
    @DisplayName("approve: 모든 결재가 완료된 경우 IllegalStateException을 던진다")
    void approve_모든결재완료_IllegalStateException발생() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));

        Cdecim completed = Cdecim.builder()
                .dcdMngNo(APF_MNG_NO).dcdSqn(1).dcdEno("E10001")
                .lstDcdYn("Y").dcdTp("결재").dcdSts("승인").build();
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO)).willReturn(List.of(completed));

        assertThatThrownBy(() -> applicationService.approve(APF_MNG_NO, approveRequest("E10001", "승인")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("approve: 현재 결재자가 아닌 사번으로 요청하면 IllegalArgumentException을 던진다")
    void approve_잘못된결재자_IllegalArgumentException발생() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO))
                .willReturn(List.of(pendingApprover("E10001", 1, "Y")));

        assertThatThrownBy(() -> applicationService.approve(APF_MNG_NO, approveRequest("E99999", "승인")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("현재 결재자가 아닙니다");
    }

    @Test
    @DisplayName("approve: 결재 상태가 null이면 IllegalArgumentException을 던진다")
    void approve_결재상태미지정_IllegalArgumentException발생() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO))
                .willReturn(List.of(pendingApprover("E10001", 1, "Y")));

        assertThatThrownBy(() -> applicationService.approve(APF_MNG_NO, approveRequest("E10001", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("결재 상태");
    }

    // ───────────────────────────────────────────────────────
    // approve — 정상 케이스
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("approve: 마지막 결재자 승인 시 신청서 상태가 결재완료로 변경되고 이벤트가 발행된다")
    void approve_마지막결재자승인_결재완료이벤트발행() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO))
                .willReturn(List.of(pendingApprover("E10001", 1, "Y")));

        applicationService.approve(APF_MNG_NO, approveRequest("E10001", "승인"));

        verify(capplm).updateStatus("결재완료");
        verify(eventPublisher).publishEvent(any(ApprovalCompletedEvent.class));
    }

    @Test
    @DisplayName("approve: 반려 처리 시 신청서 상태가 반려로 변경되고 이벤트가 발행된다")
    void approve_반려처리_반려이벤트발행() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO))
                .willReturn(List.of(pendingApprover("E10001", 1, "Y")));

        applicationService.approve(APF_MNG_NO, approveRequest("E10001", "반려"));

        verify(capplm).updateStatus("반려");
        verify(eventPublisher).publishEvent(any(ApprovalCompletedEvent.class));
    }

    @Test
    @DisplayName("approve: 중간 결재자 승인 시 신청서 상태 변경 없이 이벤트도 발행되지 않는다")
    void approve_중간결재자승인_상태변경없음() {
        Capplm capplm = mockCapplm();
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.of(capplm));

        Cdecim mid = pendingApprover("E10001", 1, "N");
        Cdecim last = pendingApprover("E10002", 2, "Y");
        given(approverRepository.findByDcdMngNoOrderByDcdSqnAsc(APF_MNG_NO)).willReturn(List.of(mid, last));

        applicationService.approve(APF_MNG_NO, approveRequest("E10001", "승인"));

        verify(capplm, never()).updateStatus(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    // ───────────────────────────────────────────────────────
    // getApplication
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getApplication: 존재하지 않는 신청서 번호이면 IllegalArgumentException을 던진다")
    void getApplication_신청서없음_IllegalArgumentException발생() {
        given(applicationRepository.findById(APF_MNG_NO)).willReturn(Optional.empty());

        assertThatThrownBy(() -> applicationService.getApplication(APF_MNG_NO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(APF_MNG_NO);
    }

    // ───────────────────────────────────────────────────────
    // getPendingCount
    // ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getPendingCount: 프로젝트 3건 전산업무비 2건이면 총 5건을 반환한다")
    void getPendingCount_프로젝트3전산업무비2_총5반환() {
        given(projectRepository.searchByCondition(any())).willReturn(List.of(
                mock(Bprojm.class), mock(Bprojm.class), mock(Bprojm.class)));
        given(costRepository.searchByCondition(any())).willReturn(List.of(
                mock(Bcostm.class), mock(Bcostm.class)));

        ApplicationDto.PendingCountResponse result = applicationService.getPendingCount();

        assertThat(result.getProjectCount()).isEqualTo(3L);
        assertThat(result.getCostCount()).isEqualTo(2L);
        assertThat(result.getTotalCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getPendingCount: 미상신 건수가 없으면 총합 0을 반환한다")
    void getPendingCount_미상신없음_총합0반환() {
        given(projectRepository.searchByCondition(any())).willReturn(List.of());
        given(costRepository.searchByCondition(any())).willReturn(List.of());

        ApplicationDto.PendingCountResponse result = applicationService.getPendingCount();

        assertThat(result.getTotalCount()).isEqualTo(0L);
    }
}
