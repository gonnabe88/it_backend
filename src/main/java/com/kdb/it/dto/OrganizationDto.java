package com.kdb.it.dto;

import com.kdb.it.domain.entity.CorgnI;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class OrganizationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "OrganizationResponse")
    public static class Response {
        @Schema(description = "조직코드")
        private String prlmOgzCCone;

        @Schema(description = "상위조직코드")
        private String prlmHrkOgzCCone;

        @Schema(description = "부점명")
        private String bbrNm;

        public static Response fromEntity(CorgnI corgnI) {
            return Response.builder()
                    .prlmOgzCCone(corgnI.getPrlmOgzCCone())
                    .prlmHrkOgzCCone(corgnI.getPrlmHrkOgzCCone())
                    .bbrNm(corgnI.getBbrNm())
                    .build();
        }
    }
}
