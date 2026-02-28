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

/**
 * 정보화사업 품목 엔티티
 *
 * <p>DB 테이블: {@code TAAABB_BITEMM}</p>
 *
 * <p>정보화사업({@link Project})에 속하는 개별 도입 품목(소프트웨어, 하드웨어, 서비스 등)을
 * 관리합니다. 하나의 사업에 여러 품목이 존재할 수 있습니다.</p>
 *
 * <p>복합키 구조: ({@code GCL_MNG_NO}, {@code GCL_SNO})</p>
 *
 * <p>연관 관계: {@code PRJ_MNG_NO} + {@code PRJ_SNO}로 {@link Project}와 연결됩니다.</p>
 */
@Entity                                              // JPA 엔티티로 등록
@Table(name = "TAAABB_BITEMM")                       // 매핑할 DB 테이블명
@IdClass(BitemmId.class)                             // 복합키 클래스 지정
@Getter                                              // 모든 필드의 getter 자동 생성 (Lombok)
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // protected 기본 생성자 (JPA 요구사항)
@AllArgsConstructor                                  // 전체 필드 생성자 자동 생성
@SuperBuilder                                        // 상속 구조에서 Builder 패턴 지원
public class Bitemm extends BaseEntity {

    /** 품목관리번호: 복합 기본키의 첫 번째 컬럼 (형식: GCL-{연도}-{4자리 시퀀스}, 예: GCL-2026-0001) */
    @Id
    @Column(name = "GCL_MNG_NO", nullable = false, length = 32)
    private String gclMngNo;

    /** 품목일련번호: 복합 기본키의 두 번째 컬럼 (같은 관리번호 내 순번) */
    @Id
    @Column(name = "GCL_SNO", nullable = false)
    private Integer gclSno;

    /** 사업관리번호: 이 품목이 속한 정보화사업의 관리번호 (Project.prjMngNo 참조) */
    @Column(name = "PRJ_MNG_NO", length = 32)
    private String prjMngNo;

    /** 사업일련번호: 이 품목이 속한 정보화사업의 순번 (Project.prjSno 참조) */
    @Column(name = "PRJ_SNO")
    private Integer prjSno;

    /** 품목구분: 품목의 카테고리 (예: 소프트웨어, 하드웨어, 서비스, 컨설팅) */
    @Column(name = "GCL_DTT", length = 32)
    private String gclDtt;

    /** 품목명: 도입할 품목의 명칭 (예: Oracle DB 라이선스, 서버 장비) */
    @Column(name = "GCL_NM", length = 100)
    private String gclNm;

    /** 품목수량: 도입 수량 (최대 9자리 숫자) */
    @Column(name = "GCL_QTT", precision = 9)
    private BigDecimal gclQtt;

    /** 통화: 가격 통화 코드 (예: KRW, USD, EUR) */
    @Column(name = "CUR", length = 10)
    private String cur;

    /** 환율: 외화 품목의 적용 환율 (최대 9자리) */
    @Column(name = "XCR", precision = 9)
    private BigDecimal xcr;

    /** 환율기준일자: 환율 적용 기준일 */
    @Column(name = "XCR_BSE_DT")
    private LocalDate xcrBseDt;

    /** 예산근거: 이 품목의 예산 산정 근거 또는 참고 자료 */
    @Column(name = "BG_FDTN", length = 100)
    private String bgFdtn;

    /** 도입시기: 품목 도입 예정 시기 (예: 2026년 1분기) */
    @Column(name = "ITD_DT", length = 32)
    private String itdDt;

    /** 지급주기: 비용 지급 주기 (예: 일시, 매월, 분기) */
    @Column(name = "DFR_CLE", length = 10)
    private String dfrCle;

    /** 정보보호여부: 이 품목이 정보보호 관련 항목인지 여부 (Y/N) */
    @Column(name = "INF_PRT_YN", length = 1)
    private String infPrtYn;

    /** 통합인프라여부: 통합인프라(공동 인프라) 관련 항목인지 여부 (Y/N) */
    @Column(name = "ITR_INFR_YN", length = 1)
    private String itrInfrYn;

    /** 최종여부: 'Y'=현재 유효한 레코드, 'N'=이전 버전 레코드 */
    @Column(name = "LST_YN", length = 1)
    private String lstYn;

    /** 품목금액: 이 품목의 총 금액 (수량 × 단가, 최대 15자리) */
    @Column(name = "GCL_AMT", precision = 15)
    private BigDecimal gclAmt;
}
