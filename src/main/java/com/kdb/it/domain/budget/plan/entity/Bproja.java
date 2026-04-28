package com.kdb.it.domain.budget.plan.entity;

import com.kdb.it.domain.audit.annotation.LogTarget;
import com.kdb.it.domain.audit.entity.BprojaL;
import com.kdb.it.domain.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 프로젝트관계(TAAABB_BPROJA) 엔티티
 *
 * <p>
 * 정보화사업(TAAABB_BPROJM)과 정보기술부문계획(TAAABB_BPLANM) 간의
 * N:N 관계를 매핑하는 중간 테이블입니다.
 * </p>
 *
 * <ul>
 * <li>{@code prjMngNo}: 프로젝트관리번호 (BPROJM의 PK)</li>
 * <li>{@code bzMngNo}: 업무관리번호 (BPLANM의 PLN_MNG_NO)</li>
 * </ul>
 */
@LogTarget(entity = BprojaL.class)
@Entity
@Table(name = "TAAABB_BPROJA", comment = "프로젝트-계획 관계")
@IdClass(BprojaId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Bproja extends BaseEntity {

    /** 프로젝트관리번호 (복합 PK의 첫 번째 키) */
    @Id
    @Column(name = "PRJ_MNG_NO", length = 32, comment = "프로젝트관리번호")
    private String prjMngNo;

    /** 업무관리번호 (복합 PK의 두 번째 키, BPLANM의 PLN_MNG_NO에 대응) */
    @Id
    @Column(name = "BZ_MNG_NO", length = 32, comment = "업무관리번호")
    private String bzMngNo;
}
