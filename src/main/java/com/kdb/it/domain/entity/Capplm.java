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

import java.time.LocalDate;

@Entity
@Table(name = "TAAABB_CAPPLM")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
// 신청서 마스터 테이블
public class Capplm extends BaseEntity {

    @Id
    @Column(name = "APF_MNG_NO", length = 32, nullable = false)
    // 신청서관리번호
    private String apfMngNo;

    @Column(name = "APF_STS", length = 32)
    // 신청서상태
    private String apfSts;

    @Column(name = "RQS_ENO", length = 32)
    // 신청 사원번호
    private String rqsEno;

    @Column(name = "RQS_DT")
    // 신청일자
    private LocalDate rqsDt;

    @Column(name = "RQS_OPNN", length = 1000)
    // 신청의견
    private String rqsOpnn;

    public void updateStatus(String status) {
        this.apfSts = status;
    }
}
