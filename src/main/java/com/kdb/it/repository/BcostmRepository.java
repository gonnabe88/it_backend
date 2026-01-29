package com.kdb.it.repository;

import com.kdb.it.domain.entity.Bcostm;
import com.kdb.it.domain.entity.BcostmId;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BcostmRepository extends JpaRepository<Bcostm, BcostmId> {

    // 특정 전산관리비 조회 (삭제되지 않은 항목)
    Optional<Bcostm> findByItMngcNoAndItMngcSnoAndDelYn(String itMngcNo, Integer itMngcSno, String delYn);

    // 전산관리비 목록 조회 (삭제되지 않은 항목)
    List<Bcostm> findAllByDelYn(String delYn);

    // IT 관리비 관리번호로 조회 (삭제되지 않은 항목, 일련번호 무관하게 모두 조회 시 사용 가능)
    List<Bcostm> findByItMngcNoAndDelYn(String itMngcNo, String delYn);

    // 시퀀스 채번 (S_IT_MNGC)
    @Query(value = "SELECT S_IT_MNGC.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextSequenceValue();

    // 시퀀스 생성을 위한 쿼리 (필요시 구현, 여기서는 예시로 남겨둠)
    @Query(value = "SELECT NVL(MAX(IT_MNGC_SNO), 0) + 1 FROM TAAABB_BCOSTM WHERE IT_MNGC_NO = :itMngcNo", nativeQuery = true)
    Integer getNextSnoValue(@Param("itMngcNo") String itMngcNo);
}
