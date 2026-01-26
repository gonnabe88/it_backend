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
    java.util.List<Cappla> findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc(String orcTbCd, String orcPkVl,
            Integer orcSnoVl);

    /**
     * 원본 테이블 코드, PK, SNO 및 신청서 상태 목록에 해당하는 신청서가 존재하는지 확인
     */
    @Query("""
            SELECT COUNT(c) > 0
            FROM Cappla c
            JOIN Capplm m ON c.apfMngNo = m.apfMngNo
            WHERE c.orcTbCd = :orcTbCd
            AND c.orcPkVl = :orcPkVl
            AND c.orcSnoVl = :orcSnoVl
            AND m.apfSts IN :statuses
            """)
    boolean existsByOrcTbCdAndOrcPkVlAndOrcSnoVlAndApfStsIn(
            @org.springframework.data.repository.query.Param("orcTbCd") String orcTbCd,
            @org.springframework.data.repository.query.Param("orcPkVl") String orcPkVl,
            @org.springframework.data.repository.query.Param("orcSnoVl") Integer orcSnoVl,
            @org.springframework.data.repository.query.Param("statuses") java.util.List<String> statuses);
}
