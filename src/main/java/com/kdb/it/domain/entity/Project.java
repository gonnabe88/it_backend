package com.kdb.it.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "TAAABB_CPRJTM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Project extends BaseEntity {

    @Id
    @Column(name = "PRJ_MNG_NO", nullable = false, length = 32)
    private String prjMngNo; // 프로젝트관리번호

    @Column(name = "PRJ_NM", nullable = false, length = 200)
    private String prjNm; // 프로젝트명

    @Column(name = "PRJ_TP", nullable = false, length = 100)
    private String prjTp; // 프로젝트유형

    @Column(name = "SVN_DPM", nullable = false, length = 100)
    private String svnDpm; // 주관부서

    @Column(name = "IT_DPM", nullable = false, length = 100)
    private String itDpm; // IT부서

    @Column(name = "PRJ_BG", nullable = false, precision = 15, scale = 2)
    private BigDecimal prjBg; // 프로젝트예산

    @Column(name = "STT_DT", nullable = false)
    private LocalDate sttDt; // 시작일자

    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt; // 종료일자

    @Column(name = "SVN_DPM_CGPR", nullable = false, length = 32)
    private String svnDpmCgpr; // 주관부서담당자

    @Column(name = "IT_DPM_CGPR", nullable = false, length = 32)
    private String itDpmCgpr; // IT부서담당자

    @Column(name = "SVN_DPM_TLR", nullable = false, length = 32)
    private String svnDpmTlr; // 주관부서담당팀장

    @Column(name = "IT_DPM_TLR", nullable = false, length = 32)
    private String itDpmTlr; // IT부서담당팀장

    @Column(name = "EDRT", nullable = false, length = 32)
    private String edrt; // 전결권

    @Column(name = "PRJ_DES", nullable = false, length = 1000)
    private String prjDes; // 사업설명

    @Column(name = "PUL_RSN", nullable = false, length = 1000)
    private String pulRsn; // 추진사유

    @Column(name = "SAF", nullable = false, length = 1000)
    private String saf; // 현황

    @Column(name = "NCS", nullable = false, length = 1000)
    private String ncs; // 필요성

    @Column(name = "XPT_EFF", nullable = false, length = 1000)
    private String xptEff; // 기대효과

    @Column(name = "PLM", nullable = false, length = 1000)
    private String plm; // 문제

    @Column(name = "PRJ_RNG", nullable = false, length = 1000)
    private String prjRng; // 사업범위

    @Column(name = "PUL_PSG", nullable = false, length = 1000)
    private String pulPsg; // 추진경과

    @Column(name = "HRF_PLN", nullable = false, length = 1000)
    private String hrfPln; // 향후계획

    @Column(name = "BZ_DTT", nullable = false, length = 32)
    private String bzDtt; // 업무구분

    @Column(name = "TCHN_TP", nullable = false, length = 32)
    private String tchnTp; // 기술유형

    @Column(name = "MN_USR", nullable = false, length = 32)
    private String mnUsr; // 주요사용자

    @Column(name = "DPL_YN", nullable = false, length = 1)
    private String dplYn; // 중복여부

    @Column(name = "LBL_FSG_TLM", nullable = false)
    private LocalDate lblFsgTlm; // 의무완료기한

    @Column(name = "RPR_STS", nullable = false, length = 32)
    private String rprSts; // 보고상태

    @Column(name = "PRJ_PUL_PTT", nullable = false, length = 2)
    private String prjPulPtt; // 프로젝트추진가능성

    @Column(name = "PRJ_STS", nullable = false, length = 32)
    private String prjSts; // 프로젝트상태

    public void update(String prjNm, String prjTp, String svnDpm, String itDpm, BigDecimal prjBg,
                       LocalDate sttDt, LocalDate endDt, String svnDpmCgpr, String itDpmCgpr,
                       String svnDpmTlr, String itDpmTlr, String edrt, String prjDes, String pulRsn,
                       String saf, String ncs, String xptEff, String plm, String prjRng, String pulPsg,
                       String hrfPln, String bzDtt, String tchnTp, String mnUsr, String dplYn,
                       LocalDate lblFsgTlm, String rprSts, String prjPulPtt, String prjSts) {
        this.prjNm = prjNm;
        this.prjTp = prjTp;
        this.svnDpm = svnDpm;
        this.itDpm = itDpm;
        this.prjBg = prjBg;
        this.sttDt = sttDt;
        this.endDt = endDt;
        this.svnDpmCgpr = svnDpmCgpr;
        this.itDpmCgpr = itDpmCgpr;
        this.svnDpmTlr = svnDpmTlr;
        this.itDpmTlr = itDpmTlr;
        this.edrt = edrt;
        this.prjDes = prjDes;
        this.pulRsn = pulRsn;
        this.saf = saf;
        this.ncs = ncs;
        this.xptEff = xptEff;
        this.plm = plm;
        this.prjRng = prjRng;
        this.pulPsg = pulPsg;
        this.hrfPln = hrfPln;
        this.bzDtt = bzDtt;
        this.tchnTp = tchnTp;
        this.mnUsr = mnUsr;
        this.dplYn = dplYn;
        this.lblFsgTlm = lblFsgTlm;
        this.rprSts = rprSts;
        this.prjPulPtt = prjPulPtt;
        this.prjSts = prjSts;
    }
}
