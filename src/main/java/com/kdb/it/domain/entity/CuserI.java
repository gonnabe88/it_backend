package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "TAAABB_CUSERI")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SuperBuilder
public class CuserI extends BaseEntity {

    @Id
    @Column(name = "ENO", nullable = false, length = 32)
    private String eno; // 행번

    @Column(name = "USR_ECY_PWD", length = 64)
    private String usrEcyPwd; // 사용자암호화패스워드

    @Column(name = "BBR_C", length = 3)
    private String bbrC; // 부서코드

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BBR_C", referencedColumnName = "PRLM_OGZ_C_CONE", insertable = false, updatable = false)
    private CorgnI organization;

    public String getBbrNm() {
        return organization != null ? organization.getBbrNm() : null;
    }

    @Column(name = "CADR_TPN", length = 20)
    private String cadrTpn; // 회사번호

    @Column(name = "DTC_BBR_C", length = 3)
    private String dtcBbrC; // 상위조직코드

    @Column(name = "DTS_DTL_CONE", length = 2000)
    private String dtsDtlCone; // 상세직무내용

    @Column(name = "ETR_MIL_ADDR_NM", length = 200)
    private String etrMilAddrNm; // 전자우편주소

    @Column(name = "INLE_NO", length = 20)
    private String inleNo; // 내선번호

    @Column(name = "PT_C", length = 5)
    private String ptC; // 직위

    @Column(name = "PT_C_NM", length = 200)
    private String ptCNm; // 직위명

    @Column(name = "TEM_C", length = 5)
    private String temC; // 팀코드

    @Column(name = "TEM_NM", length = 100)
    private String temNm; // 팀명

    @Column(name = "USR_NM", length = 100)
    private String usrNm; // 사용자명

    @Column(name = "USR_WREN_NM", length = 100)
    private String usrWrenNm; // 사용자영문명

    @Column(name = "CPN_TPN", length = 100)
    private String cpnTpn; // 휴대폰번호

    @Override
    public void prePersist() {
        super.prePersist();
        // Additional CuserI specific logic if any
    }

    public void updatePassword(String password) {
        this.usrEcyPwd = password;
    }
}
