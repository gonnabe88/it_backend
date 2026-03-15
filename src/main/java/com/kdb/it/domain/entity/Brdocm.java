package com.kdb.it.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 요구사항 정의서 엔티티
 *
 * <p>
 * DB 테이블: {@code TAAABB_BRDOCM}
 * </p>
 *
 * <p>
 * IT 프로젝트의 요구사항 정의서를 관리합니다.
 * </p>
 *
 * <p>
 * 관리번호 형식: {@code DOC-{연도}-{4자리 시퀀스}} (예: {@code DOC-2026-0001})
 * </p>
 */
@Entity
@Table(name = "TAAABB_BRDOCM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Brdocm extends BaseEntity {

    /** 문서관리번호: 기본키 (예: DOC-2026-0001) */
    @Id
    @Column(name = "DOC_MNG_NO", nullable = false, length = 32)
    private String docMngNo;

    /** 요구사항명: 요구사항의 제목 (최대 200자) */
    @Column(name = "REQ_NM", length = 200)
    private String reqNm;

    /** 요구사항내용: 요구사항 상세 내용 (BLOB, HTML 포함 가능) */
    @Lob
    @Column(name = "REQ_CONE")
    private byte[] reqCone;

    /** 요구사항구분: 요구사항 분류 코드 (최대 32자) */
    @Column(name = "REQ_DTT", length = 32)
    private String reqDtt;

    /** 업무구분: 업무 영역 분류 코드 (최대 32자) */
    @Column(name = "BZ_DTT", length = 32)
    private String bzDtt;

    /** 완료기한: 요구사항 처리 완료 기한 */
    @Column(name = "FSG_TLM")
    private LocalDate fsgTlm;

    /**
     * 요구사항 정의서 정보 업데이트 메서드
     *
     * <p>
     * JPA Dirty Checking을 활용하여 트랜잭션 내에서 필드를 변경합니다.
     * </p>
     *
     * @param reqNm  요구사항명
     * @param reqCone 요구사항내용 (BLOB)
     * @param reqDtt 요구사항구분
     * @param bzDtt  업무구분
     * @param fsgTlm 완료기한
     */
    public void update(String reqNm, byte[] reqCone, String reqDtt, String bzDtt, LocalDate fsgTlm) {
        this.reqNm = reqNm;
        this.reqCone = reqCone;
        this.reqDtt = reqDtt;
        this.bzDtt = bzDtt;
        this.fsgTlm = fsgTlm;
    }
}
