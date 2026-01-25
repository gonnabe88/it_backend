package com.kdb.it.repository;

import com.kdb.it.domain.entity.CorgnI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CorgnIRepository extends JpaRepository<CorgnI, String> {
}
