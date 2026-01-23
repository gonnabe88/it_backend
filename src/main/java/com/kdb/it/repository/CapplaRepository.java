package com.kdb.it.repository;

import com.kdb.it.domain.entity.Cappla;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

public interface CapplaRepository extends JpaRepository<Cappla, String> {
    @Query(value = "SELECT S_APF_REL_SNO.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextVal();
}
