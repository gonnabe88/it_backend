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
@Table(name = "TAAABB_BITEMM")
@IdClass(BitemmId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Bitemm extends BaseEntity {

    @Id
    @Column(name = "GCL_MNG_NO", nullable = false, length = 32)
    private String gclMngNo; // 품목관리번호

    @Id
    @Column(name = "GCL_SNO", nullable = false)
    private Integer gclSno; // 품목일련번호

    @Column(name = "PRJ_MNG_NO", length = 32)
    private String prjMngNo; // 사업관리번호

    @Column(name = "PRJ_SNO")
    private Integer prjSno; // 사업일련번호

    @Column(name = "GCL_DTT", length = 32)
    private String gclDtt; // 품목구분

    @Column(name = "GCL_NM", length = 100)
    private String gclNm; // 품목명

    @Column(name = "GCL_QTT", precision = 9)
    private BigDecimal gclQtt; // 품목수량

    @Column(name = "CUR", length = 10)
    private String cur; // 통화

    @Column(name = "XCR", precision = 9)
    private BigDecimal xcr; // 환율

    @Column(name = "XCR_BSE_DT")
    private LocalDate xcrBseDt; // 환율기준일자

    @Column(name = "BG_FDTN", length = 100)
    private String bgFdtn; // 예산근거

    @Column(name = "ITD_DT", length = 32)
    private String itdDt; // 도입시기

    @Column(name = "DFR_CLE", length = 10)
    private String dfrCle; // 지급주기

    @Column(name = "INF_PRT_YN", length = 1)
    private String infPrtYn; // 정보보호여부

    @Column(name = "ITR_INFR_YN", length = 1)
    private String itrInfrYn; // 통합인프라여부

    @Column(name = "LST_YN", length = 1)
    private String lstYn; // 최종여부

    @Column(name = "GCL_AMT", precision = 15)
    private BigDecimal gclAmt; // 품목금액
}
