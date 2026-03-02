package com.kdb.it.dto;

import com.kdb.it.domain.entity.Ccodem;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 공통코드(Ccodem) 관련 DTO
 */
public class CcodemDto {

    /**
     * 공통코드 생성 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class CreateRequest {
        private String cdId; // 코드ID
        private String cdNm; // 코드명
        private String cdva; // 코드값
        private String cdDes; // 코드설명
        private String cttTp; // 코드값구분
        private String cttTpDes; // 코드값구분설명
        private Integer cdSqn; // 코드순서
        private LocalDate sttDt; // 시작일자
        private LocalDate endDt; // 종료일자

        /**
         * DTO를 Ccodem 엔티티로 변환
         */
        public Ccodem toEntity() {
            return Ccodem.builder()
                    .cdId(this.cdId)
                    .cdNm(this.cdNm)
                    .cdva(this.cdva)
                    .cdDes(this.cdDes)
                    .cttTp(this.cttTp)
                    .cttTpDes(this.cttTpDes)
                    .cdSqn(this.cdSqn)
                    .sttDt(this.sttDt)
                    .endDt(this.endDt)
                    .build();
        }
    }

    /**
     * 공통코드 수정 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UpdateRequest {
        private String cdNm; // 코드명
        private String cdva; // 코드값
        private String cdDes; // 코드설명
        private String cttTp; // 코드값구분
        private String cttTpDes; // 코드값구분설명
        private Integer cdSqn; // 코드순서
        private LocalDate sttDt; // 시작일자
        private LocalDate endDt; // 종료일자
    }

    /**
     * 공통코드 조회 응답 DTO
     */
    @Getter
    @Setter
    @Builder
    public static class Response {
        private String cdId; // 코드ID
        private String cdNm; // 코드명
        private String cdva; // 코드값
        private String cdDes; // 코드설명
        private String cttTp; // 코드값구분
        private String cttTpDes; // 코드값구분설명
        private Integer cdSqn; // 코드순서
        private LocalDate sttDt; // 시작일자
        private LocalDate endDt; // 종료일자

        // BaseEntity 필드
        private String delYn;
        private LocalDateTime fstEnrDtm;
        private String fstEnrUsid;
        private LocalDateTime lstChgDtm;
        private String lstChgUsid;

        /**
         * Ccodem 엔티티를 Response DTO로 변환
         */
        public static Response fromEntity(Ccodem ccodem) {
            if (ccodem == null)
                return null;
            return Response.builder()
                    .cdId(ccodem.getCdId())
                    .cdNm(ccodem.getCdNm())
                    .cdva(ccodem.getCdva())
                    .cdDes(ccodem.getCdDes())
                    .cttTp(ccodem.getCttTp())
                    .cttTpDes(ccodem.getCttTpDes())
                    .cdSqn(ccodem.getCdSqn())
                    .sttDt(ccodem.getSttDt())
                    .endDt(ccodem.getEndDt())
                    .delYn(ccodem.getDelYn())
                    .fstEnrDtm(ccodem.getFstEnrDtm())
                    .fstEnrUsid(ccodem.getFstEnrUsid())
                    .lstChgDtm(ccodem.getLstChgDtm())
                    .lstChgUsid(ccodem.getLstChgUsid())
                    .build();
        }
    }
}
