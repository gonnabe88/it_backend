package com.kdb.it.repository;

import com.kdb.it.domain.entity.Capplm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CapplmRepository extends JpaRepository<Capplm, String> {
    @Query(value = "SELECT S_APF.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextVal();
}
