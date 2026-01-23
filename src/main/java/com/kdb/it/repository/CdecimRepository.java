package com.kdb.it.repository;

import com.kdb.it.domain.entity.Cdecim;
import com.kdb.it.domain.entity.CdecimId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CdecimRepository extends JpaRepository<Cdecim, CdecimId> {
    List<Cdecim> findByDcdMngNoOrderByDcdSqnAsc(String dcdMngNo);

    Optional<Cdecim> findByDcdMngNoAndDcdSqn(String dcdMngNo, Integer dcdSqn);
}
