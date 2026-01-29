package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "TAAABB_CAPPLA")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// 신청서-원본 데이터 관계 관리 테이블
public class Cappla extends BaseEntity {

    @Id
    @Column(name = "APF_REL_SNO", nullable = false, length = 36)
    // 신청서관계일련번호
    private String apfRelSno;

    @Column(name = "APF_MNG_NO", length = 32, nullable = false)
    // 신청서관리번호(외래키)
    private String apfMngNo;

    @Column(name = "ORC_TB_CD", length = 10)
    // 원본테이블코드
    private String orcTbCd;

    @Column(name = "ORC_PK_VL", length = 32)
    // 원본PK값(GCL_MNG_NO)
    private String orcPkVl;

    @Column(name = "ORC_SNO_VL")
    // 원본일련번호값(GCL_SNO)
    private Integer orcSnoVl;
}
