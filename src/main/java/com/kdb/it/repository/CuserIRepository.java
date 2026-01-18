package com.kdb.it.repository;

import com.kdb.it.domain.entity.CuserI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CuserIRepository extends JpaRepository<CuserI, String>, CuserIRepositoryCustom {
    Optional<CuserI> findByEno(String eno);

    boolean existsByEno(String eno);
}
