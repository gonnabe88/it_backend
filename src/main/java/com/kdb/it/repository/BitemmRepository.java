package com.kdb.it.repository;

import com.kdb.it.domain.entity.Bitemm;
import com.kdb.it.domain.entity.BitemmId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BitemmRepository extends JpaRepository<Bitemm, BitemmId> {

    // 프로젝트 관리번호와 순번으로 품목 조회
    List<Bitemm> findByPrjMngNoAndPrjSno(String prjMngNo, Integer prjSno);

    /**
     * 프로젝트 관리번호, 순번, 삭제여부로 품목 목록 조회
     * 
     * @param prjMngNo 프로젝트 관리번호
     * @param prjSno   프로젝트 순번
     * @param delYn    삭제 여부 (Y/N)
     * @return 품목 엔티티 목록
     */
    List<Bitemm> findByPrjMngNoAndPrjSnoAndDelYn(String prjMngNo, Integer prjSno, String delYn);

    /**
     * 품목관리번호(GCL_MNG_NO) 생성을 위한 시퀀스 조회
     * 
     * @return 다음 시퀀스 값
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT S_GCL.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextSequenceValue();
}
