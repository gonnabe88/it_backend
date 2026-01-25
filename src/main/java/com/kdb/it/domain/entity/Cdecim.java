package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "TAAABB_CDECIM")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@IdClass(CdecimId.class)
// 결재 정보 관리 테이블
public class Cdecim extends BaseEntity {

    @Id
    @Column(name = "DCD_MNG_NO", length = 32, nullable = false)
    // 결재관리번호
    private String dcdMngNo;

    @Id
    @Column(name = "DCD_SQN", nullable = false)
    // 결재순서
    private Integer dcdSqn;

    @Column(name = "DCD_ENO", length = 10)
    // 결재직원번호
    private String dcdEno;

    @Column(name = "DCD_TP", length = 32)
    // 결재유형
    private String dcdTp;

    @Column(name = "DCD_DT")
    // 결재일자
    private LocalDate dcdDt;

    @Column(name = "DCD_OPNN", length = 1000)
    // 결재의견
    private String dcdOpnn;

    @Column(name = "DCD_STS", length = 32)
    // 결재상태 (승인/반려)
    private String dcdSts;

    @Column(name = "LST_DCD_YN", length = 1)
    // 최종결재자여부
    private String lstDcdYn;

    public void approve(String opinion, String status) {
        this.dcdTp = "결재"; // 결재 행위 자체는 완료됨
        this.dcdSts = status; // 승인 or 반려
        this.dcdDt = LocalDate.now();
        this.dcdOpnn = opinion;
    }
}
