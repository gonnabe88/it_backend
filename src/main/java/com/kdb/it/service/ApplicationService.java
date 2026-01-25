package com.kdb.it.service;

import java.time.LocalDate;
import java.util.List;

import com.kdb.it.domain.entity.Capplm;
import com.kdb.it.domain.entity.Cdecim;
import com.kdb.it.dto.ApplicationDto;
import com.kdb.it.repository.CapplmRepository;
import com.kdb.it.repository.CdecimRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final CapplmRepository capplmRepository;
    private final CdecimRepository cdecimRepository;
    private final com.kdb.it.repository.CapplaRepository capplaRepository;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    // 결재
    @Transactional
    private void updateApprovalLineInDetail(Capplm capplm, String dcdEno) {
        String detailJson = capplm.getApfDtlCone();
        if (detailJson == null || detailJson.isEmpty()) {
            return;
        }

        try {
            com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(detailJson);
            com.fasterxml.jackson.databind.JsonNode approvalLineNode = rootNode.path("approvalLine");

            if (approvalLineNode.isMissingNode() || !approvalLineNode.isObject()) {
                return;
            }

            boolean updated = false;
            java.util.Iterator<String> fieldNames = approvalLineNode.fieldNames();
            while (fieldNames.hasNext()) {
                String fieldName = fieldNames.next();
                com.fasterxml.jackson.databind.JsonNode approverNode = approvalLineNode.get(fieldName);
                if (approverNode.has("id") && dcdEno.equals(approverNode.get("id").asText())) {
                    if (approverNode instanceof com.fasterxml.jackson.databind.node.ObjectNode) {
                        ((com.fasterxml.jackson.databind.node.ObjectNode) approverNode)
                                .put("date", LocalDate.now()
                                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy.MM.dd")));
                        updated = true;
                    }
                }
            }

            if (updated) {
                String updatedJson = objectMapper.writeValueAsString(rootNode);
                capplm.updateDetailContent(updatedJson);
            }

        } catch (Exception e) {
            // JSON 파싱 실패 시 로그를 남기거나 무시 (비즈니스 로직 중단을 막기 위함)
            // log.warn("Failed to update approval line json: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    // 신청서 등록
    @Transactional
    public String submit(ApplicationDto.CreateRequest request) {

        Long capplmSeq = capplmRepository.getNextVal();
        String apfMngNo = "APF_" + String.valueOf(java.time.LocalDate.now().getYear())
                + String.format("%08d", capplmSeq);

        // 1. 신청서 마스터 생성
        Capplm capplm = Capplm.builder()
                .apfMngNo(apfMngNo)
                .apfNm(request.getApfNm()) // 신청서명
                .apfDtlCone(request.getApfDtlCone()) // 신청서세부내용
                .apfSts("결재중") // 결재중
                .rqsEno(request.getRqsEno())
                .rqsDt(LocalDate.now())
                .rqsOpnn(request.getRqsOpnn())
                .build();
        capplmRepository.save(capplm);

        // 1-1. 신청서 원본 데이터 저장
        if (request.getOrcTbCd() != null) {
            Long seq = capplaRepository.getNextVal();
            String apfRelSno = "APPL_" + String.format("%028d", seq);

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
            String dcdTp = approver.getDcdTp();
            String dcdSts = approver.getDcdSts();

            if (dcdTp == null) { // 아직 결재 안 함
                if (isPreviousApproved) {
                    currentApprover = approver;
                }
                break;
            } else if ("결재".equals(dcdTp) && !"승인".equals(dcdSts)) {
                isPreviousApproved = false; // 이전 결재자가 반려함
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
        String status = request.getDcdSts();
        if (status == null || status.isEmpty()) {
            throw new IllegalArgumentException("결재 상태(승인/반려)는 필수입니다.");
        }

        currentApprover.approve(request.getDcdOpnn(), status);
        cdecimRepository.save(currentApprover);

        // 신청서 상세 내용(JSON) 내 결재 정보 업데이트
        updateApprovalLineInDetail(capplm, request.getDcdEno());

        if ("반려".equals(status)) {
            // 반려인 경우 신청서 상태도 반려로 변경
            capplm.updateStatus("반려");
        } else if ("승인".equals(status)) {
            // 승인인 경우 리스트의 마지막 요소가 방금 결재한 사람인지 확인하여 최종 완료 처리
            if ("Y".equals(currentApprover.getLstDcdYn())) {
                capplm.updateStatus("결재완료"); // 결재완료
            }
        }
    }

    // 일괄 결재 (전체를 하나의 트랜잭션으로 처리)
    @Transactional
    public ApplicationDto.BulkApproveResponse bulkApprove(ApplicationDto.BulkApproveRequest request) {
        List<ApplicationDto.ApprovalResult> results = new java.util.ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        // 모든 신청서를 순회하며 승인 처리
        for (ApplicationDto.ApprovalItem item : request.getApprovals()) {
            try {
                // 개별 승인 요청 생성
                ApplicationDto.ApproveRequest approveRequest = new ApplicationDto.ApproveRequest();
                approveRequest.setDcdEno(item.getDcdEno());
                approveRequest.setDcdOpnn(item.getDcdOpnn());
                approveRequest.setDcdSts(item.getDcdSts());

                // 승인 처리
                approve(item.getApfMngNo(), approveRequest);

                // 성공 결과 추가
                results.add(ApplicationDto.ApprovalResult.builder()
                        .apfMngNo(item.getApfMngNo())
                        .success(true)
                        .message("처리 완료")
                        .build());
                successCount++;

            } catch (Exception e) {
                // 실패 시 예외를 던져서 전체 트랜잭션 롤백
                throw new RuntimeException("신청서 " + item.getApfMngNo() + " 처리 실패: " + e.getMessage(), e);
            }
        }

        // 응답 생성
        return ApplicationDto.BulkApproveResponse.builder()
                .totalCount(request.getApprovals().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .results(results)
                .build();
    }

    public ApplicationDto.Response getApplication(String apfMngNo) {
        Capplm capplm = capplmRepository.findById(apfMngNo)
                .orElseThrow(() -> new IllegalArgumentException("신청서를 찾을 수 없습니다: " + apfMngNo));
        List<Cdecim> approvers = cdecimRepository.findByDcdMngNoOrderByDcdSqnAsc(apfMngNo);
        return ApplicationDto.Response.fromEntity(capplm, approvers);
    }

    public List<ApplicationDto.Response> getApplications() {
        return capplmRepository.findAll().stream()
                .map(capplm -> {
                    List<Cdecim> approvers = cdecimRepository.findByDcdMngNoOrderByDcdSqnAsc(capplm.getApfMngNo());
                    return ApplicationDto.Response.fromEntity(capplm, approvers);
                })
                .toList();
    }

    // 일괄 조회 (여러 신청서를 한 번에 조회)
    public List<ApplicationDto.Response> getApplicationsByIds(ApplicationDto.BulkGetRequest request) {
        return request.getApfMngNos().stream()
                .map(apfMngNo -> {
                    try {
                        return getApplication(apfMngNo);
                    } catch (IllegalArgumentException e) {
                        // 존재하지 않는 신청서는 결과에서 제외
                        return null;
                    }
                })
                .filter(response -> response != null) // null 제거
                .toList();
    }
}
