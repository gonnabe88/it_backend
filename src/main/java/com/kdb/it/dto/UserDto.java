package com.kdb.it.dto;

import com.kdb.it.domain.entity.CuserI;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class UserDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "UserListResponse")
    public static class ListResponse {
        @Schema(description = "행번")
        private String eno;

        @Schema(description = "부점명")
        private String bbrNm;

        @Schema(description = "팀명")
        private String temNm;

        @Schema(description = "사용자명")
        private String usrNm;

        @Schema(description = "직위명")
        private String ptCNm;

        // Note: bbrNm needs to be set manually as CuserI only has bbrC
        public static ListResponse fromEntity(CuserI user, String bbrNm) {
            return ListResponse.builder()
                    .eno(user.getEno())
                    .bbrNm(bbrNm)
                    .temNm(user.getTemNm())
                    .usrNm(user.getUsrNm())
                    .ptCNm(user.getPtCNm())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "UserDetailResponse")
    public static class DetailResponse {
        @Schema(description = "행번")
        private String eno;

        @Schema(description = "부점명")
        private String bbrNm;

        @Schema(description = "팀명")
        private String temNm;

        @Schema(description = "사용자명")
        private String usrNm;

        @Schema(description = "직위명")
        private String ptCNm;

        @Schema(description = "내선번호")
        private String inleNo;

        @Schema(description = "휴대폰번호")
        private String cpnTpn;

        @Schema(description = "상세직무내용")
        private String dtsDtlCone;

        public static DetailResponse fromEntity(CuserI user, String bbrNm) {
            return DetailResponse.builder()
                    .eno(user.getEno())
                    .bbrNm(bbrNm)
                    .temNm(user.getTemNm())
                    .usrNm(user.getUsrNm())
                    .ptCNm(user.getPtCNm())
                    .inleNo(user.getInleNo())
                    .cpnTpn(user.getCpnTpn())
                    .dtsDtlCone(user.getDtsDtlCone())
                    .build();
        }
    }
}
