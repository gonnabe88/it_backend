package com.kdb.it.dto;

import com.kdb.it.domain.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class ProjectDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String prjMngNo;
        private String prjNm;
        private String prjTp;
        private String svnDpm;
        private String itDpm;
        private BigDecimal prjBg;
        private LocalDate sttDt;
        private LocalDate endDt;
        private String svnDpmCgpr;
        private String itDpmCgpr;
        private String svnDpmTlr;
        private String itDpmTlr;
        private String svnHdq;
        private String edrt;
        private String prjDes;
        private String pulRsn;
        private String saf;
        private String ncs;
        private String xptEff;
        private String plm;
        private String prjRng;
        private String pulPsg;
        private String hrfPln;
        private String bzDtt;
        private String tchnTp;
        private String mnUsr;
        private String dplYn;
        private LocalDate lblFsgTlm;
        private String rprSts;
        private String prjPulPtt;
        private String prjSts;
        private String bgYy;

        public Project toEntity() {
            return Project.builder()
                    .prjMngNo(prjMngNo)
                    .prjNm(prjNm)
                    .prjTp(prjTp)
                    .svnDpm(svnDpm)
                    .itDpm(itDpm)
                    .prjBg(prjBg)
                    .sttDt(sttDt)
                    .endDt(endDt)
                    .svnDpmCgpr(svnDpmCgpr)
                    .itDpmCgpr(itDpmCgpr)
                    .svnHdq(svnHdq)
                    .svnDpmTlr(svnDpmTlr)
                    .itDpmTlr(itDpmTlr)
                    .edrt(edrt)
                    .prjDes(prjDes)
                    .pulRsn(pulRsn)
                    .saf(saf)
                    .ncs(ncs)
                    .xptEff(xptEff)
                    .plm(plm)
                    .prjRng(prjRng)
                    .pulPsg(pulPsg)
                    .hrfPln(hrfPln)
                    .bzDtt(bzDtt)
                    .tchnTp(tchnTp)
                    .mnUsr(mnUsr)
                    .dplYn(dplYn == null ? "N" : dplYn)
                    .lblFsgTlm(lblFsgTlm)
                    .rprSts(rprSts)
                    .prjPulPtt(prjPulPtt)
                    .prjSts(prjSts)
                    .bgYy(bgYy)
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String prjNm;
        private String prjTp;
        private String svnDpm;
        private String itDpm;
        private BigDecimal prjBg;
        private LocalDate sttDt;
        private LocalDate endDt;
        private String svnDpmCgpr;
        private String itDpmCgpr;
        private String svnDpmTlr;
        private String svnHdq;
        private String itDpmTlr;
        private String edrt;
        private String prjDes;
        private String pulRsn;
        private String saf;
        private String ncs;
        private String xptEff;
        private String plm;
        private String prjRng;
        private String pulPsg;
        private String hrfPln;
        private String bzDtt;
        private String tchnTp;
        private String mnUsr;
        private String dplYn;
        private LocalDate lblFsgTlm;
        private String rprSts;
        private String prjPulPtt;
        private String prjSts;
        private String bgYy;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String prjMngNo;
        private String prjNm;
        private String prjTp;
        private String svnDpm;
        private String itDpm;
        private BigDecimal prjBg;
        private LocalDate sttDt;
        private LocalDate endDt;
        private String svnDpmCgpr;
        private String itDpmCgpr;
        private String svnHdq;
        private String svnDpmTlr;
        private String itDpmTlr;
        private String edrt;
        private String prjDes;
        private String pulRsn;
        private String saf;
        private String ncs;
        private String xptEff;
        private String plm;
        private String prjRng;
        private String pulPsg;
        private String hrfPln;
        private String bzDtt;
        private String tchnTp;
        private String mnUsr;
        private String dplYn;
        private LocalDate lblFsgTlm;
        private String rprSts;
        private String prjPulPtt;
        private String prjSts;
        private String delYn;
        private String bgYy;

        public static Response fromEntity(Project project) {
            return Response.builder()
                    .prjMngNo(project.getPrjMngNo())
                    .prjNm(project.getPrjNm())
                    .prjTp(project.getPrjTp())
                    .svnDpm(project.getSvnDpm())
                    .itDpm(project.getItDpm())
                    .prjBg(project.getPrjBg())
                    .sttDt(project.getSttDt())
                    .endDt(project.getEndDt())
                    .svnHdq(project.getSvnHdq())
                    .svnDpmCgpr(project.getSvnDpmCgpr())
                    .itDpmCgpr(project.getItDpmCgpr())
                    .svnDpmTlr(project.getSvnDpmTlr())
                    .itDpmTlr(project.getItDpmTlr())
                    .edrt(project.getEdrt())
                    .prjDes(project.getPrjDes())
                    .pulRsn(project.getPulRsn())
                    .saf(project.getSaf())
                    .ncs(project.getNcs())
                    .xptEff(project.getXptEff())
                    .plm(project.getPlm())
                    .prjRng(project.getPrjRng())
                    .pulPsg(project.getPulPsg())
                    .hrfPln(project.getHrfPln())
                    .bzDtt(project.getBzDtt())
                    .tchnTp(project.getTchnTp())
                    .mnUsr(project.getMnUsr())
                    .dplYn(project.getDplYn())
                    .lblFsgTlm(project.getLblFsgTlm())
                    .rprSts(project.getRprSts())
                    .prjPulPtt(project.getPrjPulPtt())
                    .prjSts(project.getPrjSts())
                    .delYn(project.getDelYn())
                    .bgYy(project.getBgYy())
                    .build();
        }
    }
}
