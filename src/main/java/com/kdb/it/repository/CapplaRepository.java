package com.kdb.it.repository;

import com.kdb.it.domain.entity.Cappla;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

public interface CapplaRepository extends JpaRepository<Cappla, String> {
    @Query(value = "SELECT S_APF_REL_SNO.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextVal();

    /**
     * 원본 테이블 코드와 PK값, SNO값으로 신청서 조회 (최신순)
     */
    java.util.List<Cappla> findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc(String orcTbCd, String orcPkVl, Integer orcSnoVl);
}
