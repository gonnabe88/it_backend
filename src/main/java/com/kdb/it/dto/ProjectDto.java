package com.kdb.it.dto;

import com.kdb.it.domain.entity.Project;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProjectDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ProjectCreateRequest")
    public static class CreateRequest {
        @Schema(description = "프로젝트관리번호")
        private String prjMngNo;

        @Schema(description = "프로젝트명")
        private String prjNm;

        @Schema(description = "프로젝트유형")
        private String prjTp;

        @Schema(description = "주관부서")
        private String svnDpm;

        @Schema(description = "IT부서")
        private String itDpm;

        @Schema(description = "프로젝트예산")
        private BigDecimal prjBg;

        @Schema(description = "시작일자")
        private LocalDate sttDt;

        @Schema(description = "종료일자")
        private LocalDate endDt;

        @Schema(description = "주관부서담당자")
        private String svnDpmCgpr;

        @Schema(description = "IT부서담당자")
        private String itDpmCgpr;

        @Schema(description = "주관부서담당팀장")
        private String svnDpmTlr;

        @Schema(description = "IT부서담당팀장")
        private String itDpmTlr;

        @Schema(description = "주관본부/부문")
        private String svnHdq;

        @Schema(description = "전결권")
        private String edrt;

        @Schema(description = "사업설명")
        private String prjDes;

        @Schema(description = "추진사유")
        private String pulRsn;

        @Schema(description = "현황")
        private String saf;

        @Schema(description = "필요성")
        private String ncs;

        @Schema(description = "기대효과")
        private String xptEff;

        @Schema(description = "문제")
        private String plm;

        @Schema(description = "사업범위")
        private String prjRng;

        @Schema(description = "추진경과")
        private String pulPsg;

        @Schema(description = "향후계획")
        private String hrfPln;

        @Schema(description = "업무구분")
        private String bzDtt;

        @Schema(description = "기술유형")
        private String tchnTp;

        @Schema(description = "주요사용자")
        private String mnUsr;

        @Schema(description = "중복여부")
        private String dplYn;

        @Schema(description = "의무완료기한")
        private LocalDate lblFsgTlm;

        @Schema(description = "보고상태")
        private String rprSts;

        @Schema(description = "프로젝트추진가능성")
        private String prjPulPtt;

        @Schema(description = "프로젝트상태")
        private String prjSts;

        @Schema(description = "예산년도")
        private String bgYy;

        @Schema(description = "품목 목록")
        private java.util.List<BitemmDto> items;

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
    @Schema(name = "ProjectUpdateRequest")
    public static class UpdateRequest {
        @Schema(description = "프로젝트명")
        private String prjNm;

        @Schema(description = "프로젝트유형")
        private String prjTp;

        @Schema(description = "주관부서")
        private String svnDpm;

        @Schema(description = "IT부서")
        private String itDpm;

        @Schema(description = "프로젝트예산")
        private BigDecimal prjBg;

        @Schema(description = "시작일자")
        private LocalDate sttDt;

        @Schema(description = "종료일자")
        private LocalDate endDt;

        @Schema(description = "주관부서담당자")
        private String svnDpmCgpr;

        @Schema(description = "IT부서담당자")
        private String itDpmCgpr;

        @Schema(description = "주관부서담당팀장")
        private String svnDpmTlr;

        @Schema(description = "주관본부/부문")
        private String svnHdq;

        @Schema(description = "IT부서담당팀장")
        private String itDpmTlr;

        @Schema(description = "전결권")
        private String edrt;

        @Schema(description = "사업설명")
        private String prjDes;

        @Schema(description = "추진사유")
        private String pulRsn;

        @Schema(description = "현황")
        private String saf;

        @Schema(description = "필요성")
        private String ncs;

        @Schema(description = "기대효과")
        private String xptEff;

        @Schema(description = "문제")
        private String plm;

        @Schema(description = "사업범위")
        private String prjRng;

        @Schema(description = "추진경과")
        private String pulPsg;

        @Schema(description = "향후계획")
        private String hrfPln;

        @Schema(description = "업무구분")
        private String bzDtt;

        @Schema(description = "기술유형")
        private String tchnTp;

        @Schema(description = "주요사용자")
        private String mnUsr;

        @Schema(description = "중복여부")
        private String dplYn;

        @Schema(description = "의무완료기한")
        private LocalDate lblFsgTlm;

        @Schema(description = "보고상태")
        private String rprSts;

        @Schema(description = "프로젝트추진가능성")
        private String prjPulPtt;

        @Schema(description = "프로젝트상태")
        private String prjSts;

        @Schema(description = "예산년도")
        private String bgYy;

        @Schema(description = "품목 목록")
        private java.util.List<BitemmDto> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "ProjectResponse")
    public static class Response {
        @Schema(description = "프로젝트관리번호")
        private String prjMngNo;

        @Schema(description = "프로젝트순번")
        private Integer prjSno;

        @Schema(description = "프로젝트명")
        private String prjNm;

        @Schema(description = "프로젝트유형")
        private String prjTp;

        @Schema(description = "주관부서")
        private String svnDpm;

        @Schema(description = "IT부서")
        private String itDpm;

        @Schema(description = "프로젝트예산")
        private BigDecimal prjBg;

        @Schema(description = "시작일자")
        private LocalDate sttDt;

        @Schema(description = "종료일자")
        private LocalDate endDt;

        @Schema(description = "주관부서담당자")
        private String svnDpmCgpr;

        @Schema(description = "IT부서담당자")
        private String itDpmCgpr;

        @Schema(description = "주관본부/부문")
        private String svnHdq;

        @Schema(description = "주관부서담당팀장")
        private String svnDpmTlr;

        @Schema(description = "IT부서담당팀장")
        private String itDpmTlr;

        @Schema(description = "전결권")
        private String edrt;

        @Schema(description = "사업설명")
        private String prjDes;

        @Schema(description = "추진사유")
        private String pulRsn;

        @Schema(description = "현황")
        private String saf;

        @Schema(description = "필요성")
        private String ncs;

        @Schema(description = "기대효과")
        private String xptEff;

        @Schema(description = "문제")
        private String plm;

        @Schema(description = "사업범위")
        private String prjRng;

        @Schema(description = "추진경과")
        private String pulPsg;

        @Schema(description = "향후계획")
        private String hrfPln;

        @Schema(description = "업무구분")
        private String bzDtt;

        @Schema(description = "기술유형")
        private String tchnTp;

        @Schema(description = "주요사용자")
        private String mnUsr;

        @Schema(description = "중복여부")
        private String dplYn;

        @Schema(description = "의무완료기한")
        private LocalDate lblFsgTlm;

        @Schema(description = "보고상태")
        private String rprSts;

        @Schema(description = "프로젝트추진가능성")
        private String prjPulPtt;

        @Schema(description = "프로젝트상태")
        private String prjSts;

        @Schema(description = "삭제여부")
        private String delYn;

        @Schema(description = "예산년도")
        private String bgYy;

        @Schema(description = "최초생성시간")
        private LocalDateTime fstEnrDtm;

        @Schema(description = "최초생성자")
        private String fstEnrUsid;

        @Schema(description = "마지막수정시간")
        private LocalDateTime lstChgDtm;

        @Schema(description = "마지막수정자")
        private String lstChgUsid;

        @Schema(description = "신청서관리번호")
        private String apfMngNo;

        @Schema(description = "신청서상태")
        private String apfSts;

        @Schema(description = "품목 목록")
        private java.util.List<BitemmDto> items;

        public static Response fromEntity(Project project) {
            return Response.builder()
                    .prjMngNo(project.getPrjMngNo())
                    .prjSno(project.getPrjSno())
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
                    .fstEnrDtm(project.getFstEnrDtm())
                    .fstEnrUsid(project.getFstEnrUsid())
                    .lstChgDtm(project.getLstChgDtm())
                    .lstChgUsid(project.getLstChgUsid())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "BitemmDto", description = "품목 정보 DTO")
    public static class BitemmDto {
        @Schema(description = "품목관리번호")
        private String gclMngNo;

        @Schema(description = "품목일련번호")
        private Integer gclSno;

        @Schema(description = "품목구분")
        private String gclDtt;

        @Schema(description = "품목명")
        private String gclNm;

        @Schema(description = "품목수량")
        private BigDecimal gclQtt;

        @Schema(description = "통화")
        private String cur;

        @Schema(description = "환율")
        private BigDecimal xcr;

        @Schema(description = "환율기준일자")
        private LocalDate xcrBseDt;

        @Schema(description = "예산근거")
        private String bgFdtn;

        @Schema(description = "도입시기")
        private String itdDt;

        @Schema(description = "지급주기")
        private String dfrCle;

        @Schema(description = "정보보호여부")
        private String infPrtYn;

        @Schema(description = "통합인프라여부")
        private String itrInfrYn;

        @Schema(description = "최종여부")
        private String lstYn;

        public static BitemmDto fromEntity(com.kdb.it.domain.entity.Bitemm bitemm) {
            return BitemmDto.builder()
                    .gclMngNo(bitemm.getGclMngNo())
                    .gclSno(bitemm.getGclSno())
                    .gclDtt(bitemm.getGclDtt())
                    .gclNm(bitemm.getGclNm())
                    .gclQtt(bitemm.getGclQtt())
                    .cur(bitemm.getCur())
                    .xcr(bitemm.getXcr())
                    .xcrBseDt(bitemm.getXcrBseDt())
                    .bgFdtn(bitemm.getBgFdtn())
                    .itdDt(bitemm.getItdDt())
                    .dfrCle(bitemm.getDfrCle())
                    .infPrtYn(bitemm.getInfPrtYn())
                    .itrInfrYn(bitemm.getItrInfrYn())
                    .lstYn(bitemm.getLstYn())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "ProjectBulkGetRequest", description = "일괄 조회 요청")
    public static class BulkGetRequest {
        @Schema(description = "조회할 프로젝트관리번호 목록")
        private java.util.List<String> prjMngNos;
    }
}
