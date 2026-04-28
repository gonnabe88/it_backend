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
 * 프로젝트-계획 관계(TAAABB_BPROJA) 변경 로그 엔티티.
 */
@Entity
@Table(name = "TAAABB_BPROJAL", comment = "프로젝트-계획 관계 변경 로그")
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class BprojaL extends BaseLogEntity {

    @Column(name = "PRJ_MNG_NO", length = 32, comment = "프로젝트관리번호")
    private String prjMngNo;

    @Column(name = "BZ_MNG_NO", length = 32, comment = "업무관리번호")
    private String bzMngNo;
}
