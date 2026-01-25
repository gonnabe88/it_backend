package com.kdb.it.dto;

import com.kdb.it.domain.entity.Capplm;
import com.kdb.it.domain.entity.Cdecim;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ApplicationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationCreateRequest")
    public static class CreateRequest {
        @Schema(description = "원본 테이블코드")
        private String orcTbCd;

        @Schema(description = "원본 테이블의 PK값")
        private String orcPkVl;

        @Schema(description = "원본 테이블의 sno값")
        private String orcSnoVl;

        @Schema(description = "신청서명")
        private String apfNm;

        @Schema(description = "신청서세부내용")
        private String apfDtlCone;

        @Schema(description = "신청 사원번호")
        private String rqsEno;

        @Schema(description = "신청의견")
        private String rqsOpnn;

        @Schema(description = "결재자 사원번호 목록 (순서대로)")
        private List<String> approverEnos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationApproveRequest")
    public static class ApproveRequest {
        @Schema(description = "결재자 사원번호 (실제로는 세션에서 가져와야 함)")
        private String dcdEno;

        @Schema(description = "결재의견")
        private String dcdOpnn;

        @Schema(description = "결재상태 (승인/반려)")
        private String dcdSts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationBulkApproveRequest", description = "일괄 승인 요청")
    public static class BulkApproveRequest {
        @Schema(description = "승인할 신청서 목록")
        private List<ApprovalItem> approvals;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationApprovalItem", description = "개별 신청서 승인 정보")
    public static class ApprovalItem {
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        @Schema(description = "결재자 사원번호")
        private String dcdEno;

        @Schema(description = "결재의견")
        private String dcdOpnn;

        @Schema(description = "결재상태 (승인/반려)")
        private String dcdSts;
    }

    @Getter
    @Builder
    @Schema(name = "ApplicationBulkApproveResponse", description = "일괄 승인 응답")
    public static class BulkApproveResponse {
        @Schema(description = "전체 처리 건수")
        private int totalCount;

        @Schema(description = "성공 건수")
        private int successCount;

        @Schema(description = "실패 건수")
        private int failureCount;

        @Schema(description = "개별 승인 결과 목록")
        private List<ApprovalResult> results;
    }

    @Getter
    @Builder
    @Schema(name = "ApplicationApprovalResult", description = "개별 승인 결과")
    public static class ApprovalResult {
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        @Schema(description = "성공 여부")
        private boolean success;

        @Schema(description = "결과 메시지")
        private String message;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ApplicationBulkGetRequest", description = "일괄 조회 요청")
    public static class BulkGetRequest {
        @Schema(description = "조회할 신청관리번호 목록")
        private List<String> apfMngNos;
    }

    @Getter
    @Builder
    @Schema(name = "ApplicationResponse")
    public static class Response {
        @Schema(description = "신청관리번호")
        private String apfMngNo;

        @Schema(description = "신청서명")
        private String apfNm;

        @Schema(description = "신청서세부내용")
        private String apfDtlCone;

        @Schema(description = "신청상태")
        private String apfSts;

        @Schema(description = "신청자 사원번호")
        private String rqsEno;

        @Schema(description = "신청일자")
        private LocalDate rqsDt;

        @Schema(description = "신청의견")
        private String rqsOpnn;

        @Schema(description = "결재자 목록")
        private List<ApproverResponse> approvers;

        public static Response fromEntity(Capplm capplm, List<Cdecim> approvers) {
            return Response.builder()
                    .apfMngNo(capplm.getApfMngNo())
                    .apfNm(capplm.getApfNm())
                    .apfDtlCone(capplm.getApfDtlCone())
                    .apfSts(capplm.getApfSts())
                    .rqsEno(capplm.getRqsEno())
                    .rqsDt(capplm.getRqsDt())
                    .rqsOpnn(capplm.getRqsOpnn())
                    .approvers(approvers.stream()
                            .map(ApproverResponse::fromEntity)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    @Schema(name = "ApplicationApproverResponse")
    public static class ApproverResponse {
        @Schema(description = "결재순번")
        private Integer dcdSqn;

        @Schema(description = "결재자 사원번호")
        private String dcdEno;

        @Schema(description = "결재유형")
        private String dcdTp;

        @Schema(description = "결재일자")
        private LocalDate dcdDt;

        @Schema(description = "결재의견")
        private String dcdOpnn;

        @Schema(description = "결재상태")
        private String dcdSts;

        public static ApproverResponse fromEntity(Cdecim cdecim) {
            return ApproverResponse.builder()
                    .dcdSqn(cdecim.getDcdSqn())
                    .dcdEno(cdecim.getDcdEno())
                    .dcdTp(cdecim.getDcdTp())
                    .dcdDt(cdecim.getDcdDt())
                    .dcdOpnn(cdecim.getDcdOpnn())
                    .dcdSts(cdecim.getDcdSts())
                    .build();
        }
    }
}
