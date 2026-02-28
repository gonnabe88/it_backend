package com.kdb.it.dto;

import com.kdb.it.domain.entity.Bcostm;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 전산관리비(IT 관리비) 관련 DTO 클래스 모음
 *
 * <p>전산관리비(TAAABB_BCOSTM) 엔티티의 생성, 수정, 조회, 일괄 조회에 사용되는
 * Request/Response DTO를 정적 중첩 클래스(Static Nested Class) 형태로 관리합니다.</p>
 *
 * <p>포함된 DTO:</p>
 * <ul>
 *   <li>{@link CreateRequest}: 전산관리비 생성 요청</li>
 *   <li>{@link UpdateRequest}: 전산관리비 수정 요청</li>
 *   <li>{@link Response}: 전산관리비 조회 응답</li>
 *   <li>{@link BulkGetRequest}: 일괄 조회 요청</li>
 * </ul>
 */
public class CostDto {

    /**
     * 전산관리비 생성 요청 DTO
     *
     * <p>신규 전산관리비 항목을 등록할 때 사용합니다.</p>
     *
     * <p>{@code itMngcNo}가 null 또는 빈 문자열이면 서비스에서 Oracle 시퀀스로 자동 채번합니다.</p>
     *
     * <p>{@link #toEntity(Integer)} 메서드로 엔티티로 변환할 수 있습니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.CreateRequest", description = "전산업무비 생성 요청")
    public static class CreateRequest {
        /**
         * 전산관리비관리번호 (IT_MNGC_NO)
         * <p>null 또는 빈 문자열이면 서비스에서 자동 채번됩니다.
         * 형식: {@code COST_{yyyy}_{seq:04d}} (예: "COST_2026_0001")</p>
         */
        @Schema(description = "전산업무비코드 (IT관리비관리번호)", example = "COST_2026_0001")
        private String itMngcNo;

        /** 비목명 (예산 비목의 이름, 예: "서버 유지보수") */
        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        /** 계약명 (계약서 명칭) */
        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        /** 계약구분 (예: "유지보수", "구매", "용역") */
        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        /** 계약상대처 (계약 업체명) */
        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        /** 전산관리비예산 (금액, 소수점 포함 가능) */
        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        /** 지급주기 (예: "매월", "분기", "연") */
        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        /** 지급예정월 / 최초지급일자 */
        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        /** 통화 코드 (예: "KRW", "USD") */
        @Schema(description = "통화", example = "KRW")
        private String cur;

        /** 환율 (외화인 경우 원화 환산 기준) */
        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        /** 환율기준일자 (환율 적용 기준 날짜) */
        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        /** 정보보호여부 ("Y" 또는 "N", 기본값 "N") */
        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        /** 증감사유 (예산 증감 이유) */
        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        /** 추진담당자 (담당자명) */
        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;

        /**
         * 요청 DTO를 {@link Bcostm} 엔티티로 변환합니다.
         *
         * @param nextSno 설정할 전산관리비일련번호 (서비스에서 계산된 다음 SNO)
         * @return 변환된 Bcostm 엔티티 (LST_YN='Y' 기본값 설정)
         */
        public Bcostm toEntity(Integer nextSno) {
            return Bcostm.builder()
                    .itMngcNo(this.itMngcNo)               // 전산관리비관리번호
                    .itMngcSno(nextSno)                    // 전산관리비일련번호
                    .ioeNm(this.ioeNm)                     // 비목명
                    .cttNm(this.cttNm)                     // 계약명
                    .cttTp(this.cttTp)                     // 계약구분
                    .cttOpp(this.cttOpp)                   // 계약상대처
                    .itMngcBg(this.itMngcBg)               // 전산관리비예산
                    .dfrCle(this.dfrCle)                   // 지급주기
                    .fstDfrDt(this.fstDfrDt)               // 최초지급일자
                    .cur(this.cur)                         // 통화
                    .xcr(this.xcr)                         // 환율
                    .xcrBseDt(this.xcrBseDt)               // 환율기준일자
                    .infPrtYn(this.infPrtYn == null ? "N" : this.infPrtYn) // 정보보호여부 (기본값 "N")
                    .indRsn(this.indRsn)                   // 증감사유
                    .pulCgpr(this.pulCgpr)                 // 추진담당자
                    .lstYn("Y")                            // 최종여부: 신규는 항상 최신
                    .build();
        }
    }

    /**
     * 전산관리비 수정 요청 DTO
     *
     * <p>기존 전산관리비 항목의 내용을 수정할 때 사용합니다.
     * {@code IT_MNGC_NO}는 URL PathVariable로 받으므로 이 DTO에는 포함하지 않습니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.UpdateRequest", description = "전산업무비 수정 요청")
    public static class UpdateRequest {
        /** 비목명 */
        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        /** 계약명 */
        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        /** 계약구분 */
        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        /** 계약상대처 */
        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        /** 전산관리비예산 */
        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        /** 지급주기 */
        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        /** 지급예정월 / 최초지급일자 */
        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        /** 통화 코드 */
        @Schema(description = "통화", example = "KRW")
        private String cur;

        /** 환율 */
        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        /** 환율기준일자 */
        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        /** 정보보호여부 ("Y" 또는 "N") */
        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        /** 증감사유 */
        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        /** 추진담당자 */
        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;
    }

    /**
     * 전산관리비 조회 응답 DTO
     *
     * <p>{@link Bcostm} 엔티티의 모든 필드를 포함합니다.
     * {@link #fromEntity(Bcostm)} 정적 팩토리 메서드로 엔티티에서 변환합니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.Response", description = "전산업무비 응답")
    public static class Response {
        /** 전산관리비관리번호 (IT_MNGC_NO) */
        @Schema(description = "전산업무비코드 (IT관리비관리번호)", example = "COST_2026_0001")
        private String itMngcNo;

        /** 전산관리비일련번호 (IT_MNGC_SNO, 이력 순번) */
        @Schema(description = "전산업무비일련번호 (IT관리비일련번호)", example = "1")
        private Integer itMngcSno;

        /** 최종여부 ("Y": 최신 이력, "N": 과거 이력) */
        @Schema(description = "최종여부", example = "Y")
        private String lstYn;

        /** 비목명 */
        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        /** 계약명 */
        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        /** 계약구분 */
        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        /** 계약상대처 */
        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        /** 전산관리비예산 */
        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        /** 지급주기 */
        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        /** 지급예정월 / 최초지급일자 */
        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        /** 통화 코드 */
        @Schema(description = "통화", example = "KRW")
        private String cur;

        /** 환율 */
        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        /** 환율기준일자 */
        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        /** 정보보호여부 ("Y" 또는 "N") */
        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        /** 증감사유 */
        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        /** 추진담당자 */
        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;

        /** 삭제여부 (Soft Delete 상태, "Y": 삭제됨, "N": 정상) */
        @Schema(description = "삭제여부", example = "N")
        private String delYn;

        /**
         * {@link Bcostm} 엔티티를 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * @param entity 변환할 Bcostm 엔티티
         * @return 변환된 응답 DTO
         */
        public static Response fromEntity(Bcostm entity) {
            return Response.builder()
                    .itMngcNo(entity.getItMngcNo())     // 전산관리비관리번호
                    .itMngcSno(entity.getItMngcSno())   // 전산관리비일련번호
                    .lstYn(entity.getLstYn())           // 최종여부
                    .ioeNm(entity.getIoeNm())           // 비목명
                    .cttNm(entity.getCttNm())           // 계약명
                    .cttTp(entity.getCttTp())           // 계약구분
                    .cttOpp(entity.getCttOpp())         // 계약상대처
                    .itMngcBg(entity.getItMngcBg())     // 전산관리비예산
                    .dfrCle(entity.getDfrCle())         // 지급주기
                    .fstDfrDt(entity.getFstDfrDt())     // 최초지급일자
                    .cur(entity.getCur())               // 통화
                    .xcr(entity.getXcr())               // 환율
                    .xcrBseDt(entity.getXcrBseDt())     // 환율기준일자
                    .infPrtYn(entity.getInfPrtYn())     // 정보보호여부
                    .indRsn(entity.getIndRsn())         // 증감사유
                    .pulCgpr(entity.getPulCgpr())       // 추진담당자
                    .delYn(entity.getDelYn())           // 삭제여부
                    .build();
        }
    }

    /**
     * 전산관리비 일괄 조회 요청 DTO
     *
     * <p>여러 전산관리비관리번호를 한 번에 조회할 때 사용합니다.
     * 존재하지 않는 항목은 결과에서 자동 제외됩니다.</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CostDto.BulkGetRequest", description = "전산업무비 일괄 조회 요청")
    public static class BulkGetRequest {
        /** 조회할 전산관리비관리번호 목록 */
        @Schema(description = "전산업무비코드 목록", example = "[\"COST_2026_0001\", \"COST_2026_0002\"]")
        private List<String> itMngcNos;
    }
}
