package com.kdb.it.repository;

import com.kdb.it.domain.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그인 이력(LoginHistory) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 로그인 이력 테이블(LOGIN_HISTORY)에 대한 CRUD 기능을 제공합니다.</p>
 *
 * <p>기본키 타입: {@link Long} (id: 자동 증가)</p>
 *
 * <p>보안 감사 목적의 이력 조회 메서드를 제공합니다.</p>
 */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 사번으로 로그인 이력 조회 (최신순)
     *
     * <p>특정 사용자의 전체 로그인 이력을 최신 순으로 반환합니다.
     * 최신순 정렬은 {@code LOGIN_TIME DESC}로 처리됩니다.</p>
     *
     * @param eno 조회할 사용자의 사번
     * @return 해당 사용자의 로그인 이력 목록 (로그인 시간 내림차순)
     */
    List<LoginHistory> findByEnoOrderByLoginTimeDesc(String eno);

    /**
     * 사번과 날짜 범위로 로그인 이력 조회 (최신순)
     *
     * <p>특정 기간 동안의 사용자 로그인 이력을 조회합니다.
     * 이상 접근 탐지(특정 기간 내 과도한 로그인 실패 등)에 활용할 수 있습니다.</p>
     *
     * @param eno       조회할 사용자의 사번
     * @param startTime 조회 시작 시각 (포함)
     * @param endTime   조회 종료 시각 (포함)
     * @return 해당 기간의 로그인 이력 목록 (로그인 시간 내림차순)
     */
    List<LoginHistory> findByEnoAndLoginTimeBetweenOrderByLoginTimeDesc(
            String eno, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 로그인 타입으로 이력 조회 (최신순)
     *
     * <p>특정 유형의 이력(예: 로그인 실패만)을 전체에서 조회합니다.
     * 관리자 보안 모니터링에 활용할 수 있습니다.</p>
     *
     * @param loginType 조회할 이력 유형 ("LOGIN_SUCCESS", "LOGIN_FAILURE", "LOGOUT")
     * @return 해당 유형의 로그인 이력 목록 (로그인 시간 내림차순)
     */
    List<LoginHistory> findByLoginTypeOrderByLoginTimeDesc(String loginType);

    /**
     * 특정 사용자의 최근 10개 이력 조회 (최신순)
     *
     * <p>사용자 대시보드에서 최근 접속 이력을 간략하게 표시하는 데 사용됩니다.
     * {@code findTop10By} 접두사를 통해 Spring Data JPA가 자동으로 LIMIT 10 처리합니다.</p>
     *
     * @param eno 조회할 사용자의 사번
     * @return 해당 사용자의 최근 10개 로그인 이력 (로그인 시간 내림차순)
     */
    List<LoginHistory> findTop10ByEnoOrderByLoginTimeDesc(String eno);
}
