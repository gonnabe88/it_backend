package com.kdb.it.common.code.entity;

import com.kdb.it.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

/**
 * 공통코드마스터 엔티티
 *
 * <p>
 * DB 테이블: {@code TAAABB_CCODEM}
 * </p>
 * <p>
 * 시스템에서 사용하는 공통코드를 관리합니다.
 * </p>
 */
@Entity
@Table(name = "TAAABB_CCODEM")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class Ccodem extends BaseEntity {

    /** 코드ID: 기본키 */
    @Id
    @Column(name = "C_ID", nullable = false, length = 32)
    private String cdId;

    /** 코드명 */
    @Column(name = "C_NM", length = 100)
    private String cdNm;

    /** 코드값 */
    @Column(name = "CDVA", length = 100)
    private String cdva;

    /** 코드설명 */
    @Column(name = "C_DES", length = 500)
    private String cdDes;

    /** 코드값구분 */
    @Column(name = "CTT_TP", length = 100)
    private String cttTp;

    /** 코드값구분설명 */
    @Column(name = "CTT_TP_DES", length = 500)
    private String cttTpDes;

    /** 코드순서 */
    @Column(name = "C_SQN")
    private Integer cdSqn;

    /** 시작일자 */
    @Column(name = "STT_DT")
    private LocalDate sttDt;

    /** 종료일자 */
    @Column(name = "END_DT")
    private LocalDate endDt;

    /**
     * 공통코드 정보 업데이트 메서드
     *
     * @param cdNm     코드명
     * @param cdva     코드값
     * @param cdDes    코드설명
     * @param cttTp    코드값구분
     * @param cttTpDes 코드값구분설명
     * @param cdSqn    코드순서
     * @param sttDt    시작일자
     * @param endDt    종료일자
     */
    public void update(String cdNm, String cdva, String cdDes, String cttTp, String cttTpDes, Integer cdSqn,
            LocalDate sttDt,
            LocalDate endDt) {
        this.cdNm = cdNm;
        this.cdva = cdva;
        this.cdDes = cdDes;
        this.cttTp = cttTp;
        this.cttTpDes = cttTpDes;
        this.cdSqn = cdSqn;
        this.sttDt = sttDt;
        this.endDt = endDt;
    }
}
