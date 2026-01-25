package com.kdb.it.repository;

import com.kdb.it.domain.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그인 이력 리포지토리
 */
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {

    /**
     * 사번으로 이력 조회 (최신순)
     */
    List<LoginHistory> findByEnoOrderByLoginTimeDesc(String eno);

    /**
     * 사번과 날짜 범위로 이력 조회
     */
    List<LoginHistory> findByEnoAndLoginTimeBetweenOrderByLoginTimeDesc(
            String eno, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 로그인 타입으로 이력 조회
     */
    List<LoginHistory> findByLoginTypeOrderByLoginTimeDesc(String loginType);

    /**
     * 최근 N개 이력 조회
     */
    List<LoginHistory> findTop10ByEnoOrderByLoginTimeDesc(String eno);
}
