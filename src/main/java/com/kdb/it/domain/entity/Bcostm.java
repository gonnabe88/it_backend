package com.kdb.it.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "TAAABB_BCOSTM")
@IdClass(BcostmId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Bcostm extends BaseEntity {

    @Id
    @Column(name = "IT_MNGC_NO", nullable = false, length = 32)
    private String itMngcNo; // 전산업무비코드(IT관리비관리번호)

    @Id
    @Column(name = "IT_MNGC_SNO", nullable = false)
    private Integer itMngcSno; // 전산업무비일련번호(IT관리비일련번호)

    @Column(name = "LST_YN", length = 1)
    private String lstYn; // 최종여부

    @Column(name = "IOE_NM", length = 100)
    private String ioeNm; // 비목명

    @Column(name = "CTT_NM", length = 200)
    private String cttNm; // 계약명

    @Column(name = "CTT_TP", length = 100)
    private String cttTp; // 계약구분

    @Column(name = "CTT_OPP", length = 100)
    private String cttOpp; // 계약상대처

    @Column(name = "IT_MNGC_BG", precision = 15, scale = 2)
    private BigDecimal itMngcBg; // 전산업무비예산

    @Column(name = "DFR_CLE", length = 10)
    private String dfrCle; // 지급주기

    @Column(name = "FST_DFR_DT")
    private LocalDate fstDfrDt; // 지급예정월(최초지급일자)

    @Column(name = "CUR", length = 10)
    private String cur; // 통화

    @Column(name = "XCR", precision = 9)
    private BigDecimal xcr; // 환율

    @Column(name = "XCR_BSE_DT")
    private LocalDate xcrBseDt; // 환율기준일자

    @Column(name = "INF_PRT_YN", length = 1)
    private String infPrtYn; // 정보보호여부

    @Column(name = "IND_RSN", length = 1000)
    private String indRsn; // 증감사유

    @Column(name = "PUL_CGPR", length = 32)
    private String pulCgpr; // 추진담당자

    public void update(String ioeNm, String cttNm, String cttTp, String cttOpp, BigDecimal itMngcBg,
            String dfrCle, LocalDate fstDfrDt, String cur, BigDecimal xcr, LocalDate xcrBseDt,
            String infPrtYn, String indRsn, String pulCgpr) {
        this.ioeNm = ioeNm;
        this.cttNm = cttNm;
        this.cttTp = cttTp;
        this.cttOpp = cttOpp;
        this.itMngcBg = itMngcBg;
        this.dfrCle = dfrCle;
        this.fstDfrDt = fstDfrDt;
        this.cur = cur;
        this.xcr = xcr;
        this.xcrBseDt = xcrBseDt;
        this.infPrtYn = infPrtYn;
        this.indRsn = indRsn;
        this.pulCgpr = pulCgpr;
    }
}
