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

public class CostDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.CreateRequest", description = "전산업무비 생성 요청")
    public static class CreateRequest {
        @Schema(description = "전산업무비코드 (IT관리비관리번호)", example = "COST_2026_0001")
        private String itMngcNo;

        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        @Schema(description = "통화", example = "KRW")
        private String cur;

        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;

        public Bcostm toEntity(Integer nextSno) {
            return Bcostm.builder()
                    .itMngcNo(this.itMngcNo)
                    .itMngcSno(nextSno)
                    .ioeNm(this.ioeNm)
                    .cttNm(this.cttNm)
                    .cttTp(this.cttTp)
                    .cttOpp(this.cttOpp)
                    .itMngcBg(this.itMngcBg)
                    .dfrCle(this.dfrCle)
                    .fstDfrDt(this.fstDfrDt)
                    .cur(this.cur)
                    .xcr(this.xcr)
                    .xcrBseDt(this.xcrBseDt)
                    .infPrtYn(this.infPrtYn == null ? "N" : this.infPrtYn)
                    .indRsn(this.indRsn)
                    .pulCgpr(this.pulCgpr)
                    .lstYn("Y") // 기본값 설정
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.UpdateRequest", description = "전산업무비 수정 요청")
    public static class UpdateRequest {
        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        @Schema(description = "통화", example = "KRW")
        private String cur;

        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "CostDto.Response", description = "전산업무비 응답")
    public static class Response {
        @Schema(description = "전산업무비코드 (IT관리비관리번호)", example = "COST_2026_0001")
        private String itMngcNo;

        @Schema(description = "전산업무비일련번호 (IT관리비일련번호)", example = "1")
        private Integer itMngcSno;

        @Schema(description = "최종여부", example = "Y")
        private String lstYn;

        @Schema(description = "비목명", example = "서버 유지보수")
        private String ioeNm;

        @Schema(description = "계약명", example = "2026년 서버 유지보수 계약")
        private String cttNm;

        @Schema(description = "계약구분", example = "유지보수")
        private String cttTp;

        @Schema(description = "계약상대처", example = "(주)IT솔루션")
        private String cttOpp;

        @Schema(description = "전산업무비예산", example = "10000000")
        private BigDecimal itMngcBg;

        @Schema(description = "지급주기", example = "매월")
        private String dfrCle;

        @Schema(description = "지급예정월(최초지급일자)", example = "2026-01-25")
        private LocalDate fstDfrDt;

        @Schema(description = "통화", example = "KRW")
        private String cur;

        @Schema(description = "환율", example = "1300")
        private BigDecimal xcr;

        @Schema(description = "환율기준일자", example = "2026-01-01")
        private LocalDate xcrBseDt;

        @Schema(description = "정보보호여부", example = "N")
        private String infPrtYn;

        @Schema(description = "증감사유", example = "물가 상승 반영")
        private String indRsn;

        @Schema(description = "추진담당자", example = "홍길동")
        private String pulCgpr;

        @Schema(description = "삭제여부", example = "N")
        private String delYn;

        public static Response fromEntity(Bcostm entity) {
            return Response.builder()
                    .itMngcNo(entity.getItMngcNo())
                    .itMngcSno(entity.getItMngcSno())
                    .lstYn(entity.getLstYn())
                    .ioeNm(entity.getIoeNm())
                    .cttNm(entity.getCttNm())
                    .cttTp(entity.getCttTp())
                    .cttOpp(entity.getCttOpp())
                    .itMngcBg(entity.getItMngcBg())
                    .dfrCle(entity.getDfrCle())
                    .fstDfrDt(entity.getFstDfrDt())
                    .cur(entity.getCur())
                    .xcr(entity.getXcr())
                    .xcrBseDt(entity.getXcrBseDt())
                    .infPrtYn(entity.getInfPrtYn())
                    .indRsn(entity.getIndRsn())
                    .pulCgpr(entity.getPulCgpr())
                    .delYn(entity.getDelYn())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "CostDto.BulkGetRequest", description = "전산업무비 일괄 조회 요청")
    public static class BulkGetRequest {
        @Schema(description = "전산업무비코드 목록", example = "[\"COST_2026_0001\", \"COST_2026_0002\"]")
        private List<String> itMngcNos;
    }
}
