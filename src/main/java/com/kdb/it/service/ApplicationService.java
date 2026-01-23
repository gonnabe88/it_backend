package com.kdb.it.service;

import com.kdb.it.domain.entity.Capplm;
import com.kdb.it.domain.entity.Cdecim;
import com.kdb.it.dto.ApplicationDto;
import com.kdb.it.repository.CapplmRepository;
import com.kdb.it.repository.CdecimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final CapplmRepository capplmRepository;
    private final CdecimRepository cdecimRepository;
    private final com.kdb.it.repository.CapplaRepository capplaRepository;

    // 신청
    @Transactional
    public String submit(ApplicationDto.CreateRequest request) {
        String apfMngNo = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

        // 1. 신청서 마스터 생성
        Capplm capplm = Capplm.builder()
                .apfMngNo(apfMngNo)
                .apfSts("200") // 결재중
                .rqsEno(request.getRqsEno())
                .rqsDt(LocalDate.now())
                .rqsOpnn(request.getRqsOpnn())
                .build();
        capplmRepository.save(capplm);

        // 1-1. 신청서 원본 데이터 저장
        if (request.getOrcTbCd() != null) {
            Long seq = capplaRepository.getNextVal();
            String apfRelSno = "APF_REL_" + seq;

            com.kdb.it.domain.entity.Cappla cappla = com.kdb.it.domain.entity.Cappla.builder()
                    .apfRelSno(apfRelSno)
                    .apfMngNo(apfMngNo)
                    .orcTbCd(request.getOrcTbCd())
                    .orcPkVl(request.getOrcPkVl())
                    .orcSnoVl(request.getOrcSnoVl() != null ? Integer.parseInt(request.getOrcSnoVl()) : null)
                    .build();
            capplaRepository.save(cappla);
        }

        // 2. 결재선 생성
        List<String> approverEnos = request.getApproverEnos();
        for (int i = 0; i < approverEnos.size(); i++) {
            Cdecim cdecim = Cdecim.builder()
                    .dcdMngNo(apfMngNo)
                    .dcdSqn(i + 1)
                    .dcdEno(approverEnos.get(i))
                    .lstDcdYn(i == approverEnos.size() - 1 ? "Y" : "N")
                    .build();
            cdecimRepository.save(cdecim);
        }

        return apfMngNo;
    }

    // 결재
    @Transactional
    public void approve(String apfMngNo, ApplicationDto.ApproveRequest request) {
        Capplm capplm = capplmRepository.findById(apfMngNo)
                .orElseThrow(() -> new IllegalArgumentException("신청서를 찾을 수 없습니다: " + apfMngNo));

        List<Cdecim> approvers = cdecimRepository.findByDcdMngNoOrderByDcdSqnAsc(apfMngNo);

        // 현재 결재 차례 찾기
        Cdecim currentApprover = null;
        boolean isPreviousApproved = true;

        for (Cdecim approver : approvers) {
            if (approver.getDcdTp() == null) { // 아직 결재 안 함
                if (isPreviousApproved) {
                    currentApprover = approver;
                }
                break;
            } else if (!"승인".equals(approver.getDcdTp())) {
                isPreviousApproved = false; // 이전 결재자가 승인하지 않음 (반려 등)
                break;
            }
        }

        if (currentApprover == null) {
            throw new IllegalStateException("결재할 차례가 아니거나 이미 모든 결재가 완료되었습니다.");
        }

        if (!currentApprover.getDcdEno().equals(request.getDcdEno())) {
            throw new IllegalArgumentException("현재 결재자가 아닙니다.");
        }

        // 결재 처리
        currentApprover.approve(request.getDcdOpnn());
        cdecimRepository.save(currentApprover);

        // 리스트의 마지막 요소가 방금 결재한 사람인지 확인
        if ("Y".equals(currentApprover.getLstDcdYn())) {
            capplm.updateStatus("900"); // 결재완료
        }
    }

    public ApplicationDto.Response getApplication(String apfMngNo) {
        Capplm capplm = capplmRepository.findById(apfMngNo)
                .orElseThrow(() -> new IllegalArgumentException("신청서를 찾을 수 없습니다: " + apfMngNo));
        List<Cdecim> approvers = cdecimRepository.findByDcdMngNoOrderByDcdSqnAsc(apfMngNo);
        return ApplicationDto.Response.fromEntity(capplm, approvers);
    }
}
