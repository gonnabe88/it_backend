package com.kdb.it.repository;

import com.kdb.it.domain.entity.CauthI;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 자격등급(TAAABB_CAUTHI) JPA 리포지토리
 *
 * <p>기본 CRUD 기능만 사용합니다. (findById, findAll 등)</p>
 */
public interface CauthIRepository extends JpaRepository<CauthI, String> {
}
