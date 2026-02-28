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

/**
 * 정보화사업(IT 프로젝트) 마스터 엔티티
 *
 * <p>DB 테이블: {@code TAAABB_BPRJTM}</p>
 *
 * <p>IT 부문의 정보화사업(신규 시스템 도입, 인프라 구축 등) 계획 및 현황을 관리합니다.
 * 품목 정보({@link Bitemm})와 신청서({@link Capplm})와 연관됩니다.</p>
 *
 * <p>관리번호 형식: {@code PRJ-{예산연도}-{4자리 시퀀스}} (예: {@code PRJ-2026-0001})</p>
 *
 * <p>주의: {@code update()} 메서드가 두 개 존재합니다 (오버로딩).
 * 하나는 {@code prjSno}를 포함하고, 다른 하나는 포함하지 않습니다.</p>
 */
@Entity                                              // JPA 엔티티로 등록
@Table(name = "TAAABB_BPRJTM")                       // 매핑할 DB 테이블명
@Getter                                              // 모든 필드의 getter 자동 생성 (Lombok)
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // protected 기본 생성자 (JPA 요구사항)
@AllArgsConstructor                                  // 전체 필드 생성자 자동 생성
@SuperBuilder                                        // 상속 구조에서 Builder 패턴 지원
public class Project extends BaseEntity {

    /** 프로젝트관리번호: 기본키 (예: PRJ-2026-0001) */
    @Id
    @Column(name = "PRJ_MNG_NO", nullable = false, length = 32)
    private String prjMngNo;

    /** 프로젝트순번: 동일 관리번호 내 버전 구분 순번 (1부터 시작) */
    @Column(name = "PRJ_SNO", nullable = false)
    private Integer prjSno;

    /** 프로젝트명: 사업의 공식 명칭 (최대 200자) */
    @Column(name = "PRJ_NM", nullable = false, length = 200)
    private String prjNm;

    /** 프로젝트유형: 사업의 성격 분류 (최대 100자, 예: 신규개발, 고도화, 유지보수) */
    @Column(name = "PRJ_TP", nullable = false, length = 100)
    private String prjTp;

    /** 주관부서: 사업을 주관하는 업무 부서 코드/명 (최대 100자) */
    @Column(name = "SVN_DPM", nullable = false, length = 100)
    private String svnDpm;

    /** IT부서: 사업을 담당하는 IT 부서 코드/명 (최대 100자) */
    @Column(name = "IT_DPM", nullable = false, length = 100)
    private String itDpm;

    /** 프로젝트예산: 사업 총 예산 금액 (최대 15자리, 소수점 2자리) */
    @Column(name = "PRJ_BG", nullable = false, precision = 15, scale = 2)
    private BigDecimal prjBg;

    /** 시작일자: 사업 개시 예정일 */
    @Column(name = "STT_DT", nullable = false)
    private LocalDate sttDt;

    /** 종료일자: 사업 완료 예정일 */
    @Column(name = "END_DT", nullable = false)
    private LocalDate endDt;

    /** 주관부서담당자: 주관부서 담당자 사번 또는 이름 (최대 32자) */
    @Column(name = "SVN_DPM_CGPR", nullable = false, length = 32)
    private String svnDpmCgpr;

    /** IT부서담당자: IT부서 담당자 사번 또는 이름 (최대 32자) */
    @Column(name = "IT_DPM_CGPR", nullable = false, length = 32)
    private String itDpmCgpr;

    /** 주관부서담당팀장: 주관부서 담당 팀장 사번 또는 이름 (최대 32자) */
    @Column(name = "SVN_DPM_TLR", nullable = false, length = 32)
    private String svnDpmTlr;

    /** IT부서담당팀장: IT부서 담당 팀장 사번 또는 이름 (최대 32자) */
    @Column(name = "IT_DPM_TLR", nullable = false, length = 32)
    private String itDpmTlr;

    /** 전결권: 결재 전결 권한자 구분 (최대 32자) */
    @Column(name = "EDRT", nullable = false, length = 32)
    private String edrt;

    /** 사업설명: 사업의 전반적인 설명 (최대 1000자) */
    @Column(name = "PRJ_DES", nullable = false, length = 1000)
    private String prjDes;

    /** 추진사유: 사업을 추진하게 된 배경 및 이유 (최대 1000자) */
    @Column(name = "PUL_RSN", nullable = false, length = 1000)
    private String pulRsn;

    /** 현황: 현재 업무/시스템의 현황 분석 내용 (최대 1000자) */
    @Column(name = "SAF", nullable = false, length = 1000)
    private String saf;

    /** 필요성: 사업의 필요성 및 당위성 (최대 1000자) */
    @Column(name = "NCS", nullable = false, length = 1000)
    private String ncs;

    /** 기대효과: 사업 완료 후 기대되는 효과 (최대 1000자) */
    @Column(name = "XPT_EFF", nullable = false, length = 1000)
    private String xptEff;

    /** 문제: 현재 문제점 또는 개선이 필요한 사항 (최대 1000자) */
    @Column(name = "PLM", nullable = false, length = 1000)
    private String plm;

    /** 사업범위: 사업의 대상 범위 및 경계 (최대 1000자) */
    @Column(name = "PRJ_RNG", nullable = false, length = 1000)
    private String prjRng;

    /** 추진경과: 사업 추진 진행 상황 및 경과 내용 (최대 1000자) */
    @Column(name = "PUL_PSG", nullable = false, length = 1000)
    private String pulPsg;

    /** 향후계획: 앞으로의 추진 계획 (최대 1000자) */
    @Column(name = "HRF_PLN", nullable = false, length = 1000)
    private String hrfPln;

    /** 업무구분: 사업이 속하는 업무 영역 구분 (최대 32자, 예: 리테일, 기업금융) */
    @Column(name = "BZ_DTT", nullable = false, length = 32)
    private String bzDtt;

    /** 기술유형: 사업에 적용되는 기술 분류 (최대 32자, 예: 웹, 앱, AI, 빅데이터) */
    @Column(name = "TCHN_TP", nullable = false, length = 32)
    private String tchnTp;

    /** 주요사용자: 시스템의 주요 사용자 그룹 (최대 32자, 예: 내부직원, 고객, 전체) */
    @Column(name = "MN_USR", nullable = false, length = 32)
    private String mnUsr;

    /** 중복여부: 기존 유사 사업과의 중복 여부 ('Y'=중복, 'N'=미중복) */
    @Column(name = "DPL_YN", nullable = false, length = 1)
    private String dplYn;

    /** 의무완료기한: 법적/규정상 반드시 완료해야 하는 기한 */
    @Column(name = "LBL_FSG_TLM", nullable = false)
    private LocalDate lblFsgTlm;

    /** 보고상태: 상위 보고 단계의 상태 (최대 32자) */
    @Column(name = "RPR_STS", nullable = false, length = 32)
    private String rprSts;

    /** 프로젝트추진가능성: 사업 추진 가능성 평가 결과 (최대 2자, 예: '상', '중', '하' 또는 코드값) */
    @Column(name = "PRJ_PUL_PTT", nullable = false, length = 2)
    private String prjPulPtt;

    /** 프로젝트상태: 사업의 현재 진행 상태 (최대 32자, 예: 계획, 진행중, 완료, 취소) */
    @Column(name = "PRJ_STS", nullable = false, length = 32)
    private String prjSts;

    /** 예산년도: 사업 예산이 편성된 연도 (4자리 숫자, 예: "2026") */
    @Column(name = "BG_YY", nullable = false, precision = 4)
    private String bgYy;

    /** 주관본부/부문: 사업을 총괄하는 본부 또는 부문 명칭 (최대 32자) */
    @Column(name = "SVN_HDQ", nullable = false, length = 32)
    private String svnHdq;

    /**
     * 정보화사업 정보 업데이트 메서드 (prjSno 포함)
     *
     * <p>JPA Dirty Checking을 활용하여 트랜잭션 내에서 모든 필드를 변경합니다.
     * 프로젝트 순번(prjSno)도 함께 변경합니다.</p>
     *
     * @param prjNm     프로젝트명
     * @param prjTp     프로젝트유형
     * @param svnDpm    주관부서
     * @param itDpm     IT부서
     * @param prjBg     프로젝트예산
     * @param sttDt     시작일자
     * @param endDt     종료일자
     * @param svnDpmCgpr 주관부서담당자
     * @param itDpmCgpr IT부서담당자
     * @param svnDpmTlr 주관부서담당팀장
     * @param itDpmTlr  IT부서담당팀장
     * @param edrt      전결권
     * @param prjDes    사업설명
     * @param pulRsn    추진사유
     * @param saf       현황
     * @param ncs       필요성
     * @param xptEff    기대효과
     * @param plm       문제
     * @param prjRng    사업범위
     * @param pulPsg    추진경과
     * @param hrfPln    향후계획
     * @param bzDtt     업무구분
     * @param tchnTp    기술유형
     * @param mnUsr     주요사용자
     * @param dplYn     중복여부
     * @param lblFsgTlm 의무완료기한
     * @param rprSts    보고상태
     * @param prjPulPtt 프로젝트추진가능성
     * @param prjSts    프로젝트상태
     * @param bgYy      예산년도
     * @param svnHdq    주관본부/부문
     * @param prjSno    프로젝트순번
     */
    public void update(String prjNm, String prjTp, String svnDpm, String itDpm, BigDecimal prjBg,
            LocalDate sttDt, LocalDate endDt, String svnDpmCgpr, String itDpmCgpr,
            String svnDpmTlr, String itDpmTlr, String edrt, String prjDes, String pulRsn,
            String saf, String ncs, String xptEff, String plm, String prjRng, String pulPsg,
            String hrfPln, String bzDtt, String tchnTp, String mnUsr, String dplYn,
            LocalDate lblFsgTlm, String rprSts, String prjPulPtt, String prjSts, String bgYy, String svnHdq, Integer prjSno) {
        this.prjSno = prjSno;
        this.prjNm = prjNm;
        // ... (나머지 필드 업데이트)
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
        this.bgYy = bgYy;
        this.svnHdq = svnHdq;
    }

    /**
     * 정보화사업 정보 업데이트 메서드 (prjSno 제외)
     *
     * <p>프로젝트 순번(prjSno)은 변경하지 않고 나머지 필드만 업데이트합니다.
     * 일반적인 수정 API에서 사용됩니다.</p>
     *
     * @param prjNm     프로젝트명
     * @param prjTp     프로젝트유형
     * @param svnDpm    주관부서
     * @param itDpm     IT부서
     * @param prjBg     프로젝트예산
     * @param sttDt     시작일자
     * @param endDt     종료일자
     * @param svnDpmCgpr 주관부서담당자
     * @param itDpmCgpr IT부서담당자
     * @param svnDpmTlr 주관부서담당팀장
     * @param itDpmTlr  IT부서담당팀장
     * @param edrt      전결권
     * @param prjDes    사업설명
     * @param pulRsn    추진사유
     * @param saf       현황
     * @param ncs       필요성
     * @param xptEff    기대효과
     * @param plm       문제
     * @param prjRng    사업범위
     * @param pulPsg    추진경과
     * @param hrfPln    향후계획
     * @param bzDtt     업무구분
     * @param tchnTp    기술유형
     * @param mnUsr     주요사용자
     * @param dplYn     중복여부
     * @param lblFsgTlm 의무완료기한
     * @param rprSts    보고상태
     * @param prjPulPtt 프로젝트추진가능성
     * @param prjSts    프로젝트상태
     * @param bgYy      예산년도
     * @param svnHdq    주관본부/부문
     */
    public void update(String prjNm, String prjTp, String svnDpm, String itDpm, BigDecimal prjBg,
            LocalDate sttDt, LocalDate endDt, String svnDpmCgpr, String itDpmCgpr,
            String svnDpmTlr, String itDpmTlr, String edrt, String prjDes, String pulRsn,
            String saf, String ncs, String xptEff, String plm, String prjRng, String pulPsg,
            String hrfPln, String bzDtt, String tchnTp, String mnUsr, String dplYn,
            LocalDate lblFsgTlm, String rprSts, String prjPulPtt, String prjSts, String bgYy, String svnHdq) {
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
        this.bgYy = bgYy;
        this.svnHdq = svnHdq;
    }
}
