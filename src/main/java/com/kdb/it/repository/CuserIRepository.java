package com.kdb.it.repository;

import com.kdb.it.domain.entity.CuserI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자(CuserI) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}와 커스텀 리포지토리
 * {@link CuserIRepositoryCustom}을 동시에 상속하여
 * 표준 CRUD + QueryDSL 기반의 동적 쿼리 기능을 제공합니다.</p>
 *
 * <p>기본키 타입: {@link String} (eno: 사번)</p>
 *
 * <p>EntityGraph 전략:</p>
 * <ul>
 *   <li>{@code @EntityGraph(attributePaths = "organization")}: LAZY 로딩으로 설정된
 *       {@code organization}(CorgnI) 연관관계를 JOIN FETCH로 즉시 로딩하여
 *       N+1 문제를 방지합니다.</li>
 * </ul>
 */
@Repository // Spring 리포지토리 빈으로 등록 (예외 변환 포함)
public interface CuserIRepository extends JpaRepository<CuserI, String>, CuserIRepositoryCustom {

    /**
     * 부서코드(BBR_C)로 사용자 목록 조회 (조직 정보 즉시 로딩)
     *
     * <p>{@code @EntityGraph}를 사용하여 {@code organization}(CorgnI) 연관관계를
     * JOIN FETCH로 한 번에 조회합니다. 부점명(bbrNm) 표시에 활용됩니다.</p>
     *
     * @param bbrC 부서코드 (최대 3자, CorgnI.prlmOgzCCone과 조인)
     * @return 해당 부서의 사용자 목록 (조직 정보 포함)
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "organization")
    java.util.List<CuserI> findByBbrC(String bbrC);

    /**
     * 사번(ENO)으로 사용자 단건 조회 (조직 정보 즉시 로딩)
     *
     * <p>{@code @EntityGraph}를 사용하여 {@code organization}(CorgnI) 연관관계를
     * JOIN FETCH로 함께 조회합니다. 로그인, 사용자 상세 조회에 사용됩니다.</p>
     *
     * @param eno 사번(행번)
     * @return 해당 사번의 사용자 (없으면 {@link Optional#empty()})
     */
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = "organization")
    Optional<CuserI> findByEno(String eno);

    /**
     * 사번(ENO) 존재 여부 확인
     *
     * <p>회원가입 시 중복 사번 검사에 사용됩니다.</p>
     *
     * @param eno 확인할 사번
     * @return 사번이 존재하면 true, 없으면 false
     */
    boolean existsByEno(String eno);
}
