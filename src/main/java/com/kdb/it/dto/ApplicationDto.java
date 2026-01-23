package com.kdb.it.dto;

import com.kdb.it.domain.entity.Capplm;
import com.kdb.it.domain.entity.Cdecim;
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
    @io.swagger.v3.oas.annotations.media.Schema(name = "ApplicationCreateRequest")
    public static class CreateRequest {
        private String orcTbCd; // 원본 테이블코드
        private String orcPkVl; // 원본 테이블의 PK값
        private String orcSnoVl; // 원본 테이블의 sno값
        private String rqsEno; // 신청 사원번호
        private String rqsOpnn; // 신청의견
        private List<String> approverEnos; // 결재자 사원번호 목록 (순서대로)
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @io.swagger.v3.oas.annotations.media.Schema(name = "ApplicationApproveRequest")
    public static class ApproveRequest {
        private String dcdEno; // 결재자 사원번호 (실제로는 세션에서 가져와야 함)
        private String dcdOpnn; // 결재의견
    }

    @Getter
    @Builder
    @io.swagger.v3.oas.annotations.media.Schema(name = "ApplicationResponse")
    public static class Response {
        private String apfMngNo;
        private String apfSts;
        private String rqsEno;
        private LocalDate rqsDt;
        private String rqsOpnn;
        private List<ApproverResponse> approvers;

        public static Response fromEntity(Capplm capplm, List<Cdecim> approvers) {
            return Response.builder()
                    .apfMngNo(capplm.getApfMngNo())
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
    public static class ApproverResponse {
        private Integer dcdSqn;
        private String dcdEno;
        private String dcdTp;
        private LocalDate dcdDt;
        private String dcdOpnn;

        public static ApproverResponse fromEntity(Cdecim cdecim) {
            return ApproverResponse.builder()
                    .dcdSqn(cdecim.getDcdSqn())
                    .dcdEno(cdecim.getDcdEno())
                    .dcdTp(cdecim.getDcdTp())
                    .dcdDt(cdecim.getDcdDt())
                    .dcdOpnn(cdecim.getDcdOpnn())
                    .build();
        }
    }
}
