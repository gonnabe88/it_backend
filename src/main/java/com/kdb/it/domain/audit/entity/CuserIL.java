package com.kdb.it.domain.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 사용자 정보(TAAABB_CUSERI) 변경 로그 엔티티.
 */
@Entity
@Table(name = "TAAABB_CUSERIL", comment = "사용자 정보 변경 로그")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class CuserIL extends BaseLogEntity {

    @Column(name = "ENO", length = 32, comment = "사번")
    private String eno;

    @Column(name = "USR_ECY_PWD", length = 64, comment = "암호화비밀번호")
    private String usrEcyPwd;

    @Column(name = "BBR_C", length = 3, comment = "부서코드")
    private String bbrC;

    @Column(name = "CADR_TPN", length = 20, comment = "휴대폰번호")
    private String cadrTpn;

    @Column(name = "DTC_BBR_C", length = 3, comment = "파견부서코드")
    private String dtcBbrC;

    @Column(name = "DTS_DTL_CONE", length = 2000, comment = "파견상세내용")
    private String dtsDtlCone;

    @Column(name = "ETR_MIL_ADDR_NM", length = 200, comment = "이메일주소명")
    private String etrMilAddrNm;

    @Column(name = "INLE_NO", length = 20, comment = "내선번호")
    private String inleNo;

    @Column(name = "PT_C", length = 5, comment = "직급코드")
    private String ptC;

    @Column(name = "PT_C_NM", length = 200, comment = "직급코드명")
    private String ptCNm;

    @Column(name = "TEM_C", length = 5, comment = "직무코드")
    private String temC;

    @Column(name = "TEM_NM", length = 100, comment = "직무명")
    private String temNm;

    @Column(name = "USR_NM", length = 100, comment = "사용자명")
    private String usrNm;

    @Column(name = "USR_WREN_NM", length = 100, comment = "사용자영문명")
    private String usrWrenNm;

    @Column(name = "CPN_TPN", length = 100, comment = "회사전화번호")
    private String cpnTpn;
}
