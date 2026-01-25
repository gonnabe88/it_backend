package com.kdb.it.repository;

import com.kdb.it.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Refresh Token 리포지토리
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * 토큰으로 조회
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 사번으로 조회
     */
    Optional<RefreshToken> findByEno(String eno);

    /**
     * 사번으로 삭제
     */
    void deleteByEno(String eno);

    /**
     * 토큰으로 삭제
     */
    void deleteByToken(String token);
}
