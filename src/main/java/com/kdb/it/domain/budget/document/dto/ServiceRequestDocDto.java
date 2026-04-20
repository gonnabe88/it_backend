package com.kdb.it.domain.budget.document.dto;

import com.kdb.it.domain.budget.document.entity.Brdocm;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 요구사항 정의서(TAAABB_BRDOCM) 관련 DTO 클래스 모음
 *
 * <p>
 * 요구사항 정의서 엔티티의 생성, 수정, 조회에 사용되는 DTO를
 * 정적 중첩 클래스(Static Nested Class) 형태로 관리합니다.
 * </p>
 */
public class ServiceRequestDocDto {

    /**
     * 요구사항 정의서 생성 요청 DTO
     *
     * <p>
     * {@code docMngNo}가 null 또는 빈 문자열이면 서비스에서 자동 채번합니다.
     * 형식: {@code DOC-{연도}-{seq:04d}} (예: DOC-2026-0001)
     * </p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ServiceRequestDocCreateRequest", description = "요구사항 정의서 생성 요청")
    public static class CreateRequest {

        /** 문서관리번호: null이면 자동 채번 */
        @Schema(description = "문서관리번호 (미입력 시 자동 채번)")
        private String docMngNo;

        /** 요구사항명 */
        @Schema(description = "요구사항명")
        private String reqNm;

        /** 요구사항내용 (HTML 포함 가능) */
        @Schema(description = "요구사항내용")
        private String reqCone;

        /** 요구사항구분 코드 */
        @Schema(description = "요구사항구분")
        private String reqDtt;

        /** 업무구분 코드 */
        @Schema(description = "업무구분")
        private String bzDtt;

        /** 완료기한 */
        @Schema(description = "완료기한 (yyyy-MM-dd)")
        private LocalDate fsgTlm;

        /**
         * CreateRequest를 {@link Brdocm} 엔티티로 변환합니다.
         *
         * @return 변환된 Brdocm 엔티티
         */
        public Brdocm toEntity() {
            return Brdocm.builder()
                    .docMngNo(this.docMngNo)
                    .reqNm(this.reqNm)
                    .reqCone(this.reqCone != null ? this.reqCone.getBytes(StandardCharsets.UTF_8) : null)
                    .reqDtt(this.reqDtt)
                    .bzDtt(this.bzDtt)
                    .fsgTlm(this.fsgTlm)
                    .build();
        }
    }

    /**
     * 요구사항 정의서 수정 요청 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ServiceRequestDocUpdateRequest", description = "요구사항 정의서 수정 요청")
    public static class UpdateRequest {

        /** 요구사항명 */
        @Schema(description = "요구사항명")
        private String reqNm;

        /** 요구사항내용 (HTML 포함 가능) */
        @Schema(description = "요구사항내용")
        private String reqCone;

        /** 요구사항구분 코드 */
        @Schema(description = "요구사항구분")
        private String reqDtt;

        /** 업무구분 코드 */
        @Schema(description = "업무구분")
        private String bzDtt;

        /** 완료기한 */
        @Schema(description = "완료기한 (yyyy-MM-dd)")
        private LocalDate fsgTlm;
    }

    /**
     * 요구사항 정의서 조회 응답 DTO
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ServiceRequestDocResponse", description = "요구사항 정의서 조회 응답")
    public static class Response {

        /** 문서관리번호 */
        @Schema(description = "문서관리번호")
        private String docMngNo;

        /** 요구사항명 */
        @Schema(description = "요구사항명")
        private String reqNm;

        /** 요구사항내용 (BLOB → UTF-8 문자열 변환) */
        @Schema(description = "요구사항내용")
        private String reqCone;

        /** 요구사항구분 */
        @Schema(description = "요구사항구분")
        private String reqDtt;

        /** 업무구분 */
        @Schema(description = "업무구분")
        private String bzDtt;

        /** 완료기한 */
        @Schema(description = "완료기한")
        private LocalDate fsgTlm;

        /** 삭제여부 */
        @Schema(description = "삭제여부")
        private String delYn;

        /** 최초생성시간 */
        @Schema(description = "최초생성시간")
        private LocalDateTime fstEnrDtm;

        /** 최초생성자 사번 */
        @Schema(description = "최초생성자")
        private String fstEnrUsid;

        /** 최초생성자 이름 (TAAABB_CUSERI JOIN) */
        @Schema(description = "최초생성자 이름")
        private String fstEnrUsNm;

        /** 마지막수정시간 */
        @Schema(description = "마지막수정시간")
        private LocalDateTime lstChgDtm;

        /** 마지막수정자 사번 */
        @Schema(description = "마지막수정자")
        private String lstChgUsid;

        /**
         * {@link Brdocm} 엔티티를 Response DTO로 변환합니다.
         *
         * @param entity 변환할 Brdocm 엔티티
         * @return 변환된 Response DTO
         */
        public static Response fromEntity(Brdocm entity) {
            // BLOB → UTF-8 문자열 변환
            String reqConeStr = null;
            if (entity.getReqCone() != null) {
                reqConeStr = new String(entity.getReqCone(), StandardCharsets.UTF_8);
            }

            return Response.builder()
                    .docMngNo(entity.getDocMngNo())
                    .reqNm(entity.getReqNm())
                    .reqCone(reqConeStr)
                    .reqDtt(entity.getReqDtt())
                    .bzDtt(entity.getBzDtt())
                    .fsgTlm(entity.getFsgTlm())
                    .delYn(entity.getDelYn())
                    .fstEnrDtm(entity.getFstEnrDtm())
                    .fstEnrUsid(entity.getFstEnrUsid())
                    .lstChgDtm(entity.getLstChgDtm())
                    .lstChgUsid(entity.getLstChgUsid())
                    .build();
        }
    }
}
