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

@Entity
@Table(name = "TAAABB_CORGNI")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class CorgnI extends BaseEntity {

    @Id
    @Column(name = "PRLM_OGZ_C_CONE", nullable = false, length = 100)
    private String prlmOgzCCone; // 조직코드

    @Column(name = "ITM_SQN_SNO", length = 9)
    private String itmSqnSno; // 순서

    @Column(name = "PRLM_HRK_OGZ_C_CONE", length = 100)
    private String prlmHrkOgzCCone; // 상위조직코드

    @Column(name = "BBR_WREN_NM", length = 100)
    private String bbrWrenNm; // 부점영문명

    @Column(name = "BBR_NM", length = 100)
    private String bbrNm; // 부점명
}
