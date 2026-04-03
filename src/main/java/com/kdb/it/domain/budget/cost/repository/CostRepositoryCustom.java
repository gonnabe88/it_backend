package com.kdb.it.domain.budget.cost.repository;

import com.kdb.it.domain.budget.cost.dto.CostDto;
import com.kdb.it.domain.budget.cost.entity.Bcostm;

import java.util.List;

/**
 * 전산관리비(Bcostm) 커스텀 리포지토리 인터페이스
 *
 * <p>Spring Data JPA의 기본 메서드로 처리하기 어려운 복잡한 동적 쿼리를 위한
 * 커스텀 인터페이스입니다. {@link CostRepositoryImpl}에서 QueryDSL로 구현됩니다.</p>
 *
 * <p>사용 패턴: {@link CostRepository}가 이 인터페이스를 상속하므로,
 * {@code costRepository.searchByCondition(condition)}과 같이 직접 사용 가능합니다.</p>
 */
public interface CostRepositoryCustom {

    /**
     * 검색 조건으로 전산관리비 목록 조회 (동적 쿼리)
     *
     * <p>
     * {@link CostDto.SearchCondition}의 필드 값에 따라 동적으로 WHERE 절을 구성합니다.
     * 모든 조건이 null이면 {@code DEL_YN='N'}인 전체 전산관리비를 반환합니다.
     * </p>
     *
     * <p>
     * {@code apfSts} 처리 방식:
     * </p>
     * <ul>
     * <li>{@code "none"}: CAPPLA 연결이 없는 전산관리비 (NOT EXISTS 서브쿼리)</li>
     * <li>그 외 값: 해당 전산관리비의 최신 CAPPLA 연결 신청서의 결재상태가 일치하는 경우 (EXISTS + MAX 서브쿼리)</li>
     * </ul>
     *
     * <p>구현: {@link CostRepositoryImpl#searchByCondition(CostDto.SearchCondition)}</p>
     *
     * @param condition 검색 조건 DTO (apfSts, cttTp, biceDpm, biceTem, infPrtYn)
     * @return 조건에 맞는 전산관리비 목록 (DEL_YN='N' 필터 항상 적용)
     */
    List<Bcostm> searchByCondition(CostDto.SearchCondition condition);
}
