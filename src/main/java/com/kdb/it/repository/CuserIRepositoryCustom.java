package com.kdb.it.repository;

import com.kdb.it.domain.entity.CuserI;

import java.util.List;

/**
 * 사용자(CuserI) 커스텀 리포지토리 인터페이스
 *
 * <p>Spring Data JPA의 기본 메서드로 처리하기 어려운 복잡한 동적 쿼리를 위한
 * 커스텀 인터페이스입니다. {@link CuserIRepositoryImpl}에서 QueryDSL로 구현됩니다.</p>
 *
 * <p>사용 패턴: {@link CuserIRepository}가 이 인터페이스를 상속하므로,
 * {@code cuserIRepository.searchByName("홍길동")}과 같이 직접 사용 가능합니다.</p>
 *
 * <p>Spring Data JPA 커스텀 리포지토리 규칙:</p>
 * <ul>
 *   <li>인터페이스 이름이 자유롭지만, 구현 클래스 이름은 반드시 {@code [인터페이스명]Impl}이어야 함</li>
 *   <li>구현 클래스({@link CuserIRepositoryImpl})에 {@code @Repository} 어노테이션 불필요</li>
 * </ul>
 */
public interface CuserIRepositoryCustom {

    /**
     * 사용자명으로 사용자 검색 (부분 일치)
     *
     * <p>QueryDSL을 사용하여 {@code USR_NM} 컬럼에서 입력 문자열을 포함하는
     * 사용자를 검색합니다 (LIKE '%name%' 검색).</p>
     *
     * <p>구현: {@link CuserIRepositoryImpl#searchByName(String)}</p>
     *
     * @param name 검색할 사용자명 (부분 일치, 예: "홍" → "홍길동", "홍철수" 등 반환)
     * @return 이름에 해당 문자열을 포함하는 사용자 목록
     */
    List<CuserI> searchByName(String name);
}
