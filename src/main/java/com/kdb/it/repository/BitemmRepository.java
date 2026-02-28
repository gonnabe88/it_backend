package com.kdb.it.repository;

import com.kdb.it.domain.entity.Bitemm;
import com.kdb.it.domain.entity.BitemmId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 정보화사업 품목(Bitemm) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속하여 기본 CRUD 기능을 제공하며,
 * 품목 도메인에 특화된 조회 메서드를 추가로 정의합니다.</p>
 *
 * <p>복합키 타입: {@link BitemmId} (gclMngNo + gclSno)</p>
 *
 * <p>프로젝트와의 연관: {@code prjMngNo} + {@code prjSno}로 특정 프로젝트의 품목을 조회합니다.</p>
 */
public interface BitemmRepository extends JpaRepository<Bitemm, BitemmId> {

    /**
     * 프로젝트 관리번호와 순번으로 품목 목록 조회 (삭제 여부 무관)
     *
     * <p>특정 프로젝트에 속한 모든 품목을 조회합니다.
     * 프로젝트 삭제 시 관련 품목을 일괄 Soft Delete 처리하는 데 사용됩니다.</p>
     *
     * @param prjMngNo 프로젝트 관리번호 (예: PRJ-2026-0001)
     * @param prjSno   프로젝트 순번
     * @return 해당 프로젝트의 모든 품목 목록 (삭제된 항목 포함)
     */
    List<Bitemm> findByPrjMngNoAndPrjSno(String prjMngNo, Integer prjSno);

    /**
     * 프로젝트 관리번호, 순번, 삭제여부로 품목 목록 조회
     *
     * <p>특정 프로젝트의 유효한(미삭제) 품목만 조회합니다.
     * 프로젝트 상세 조회 및 품목 동기화(CUD) 처리에 사용됩니다.</p>
     *
     * @param prjMngNo 프로젝트 관리번호
     * @param prjSno   프로젝트 순번
     * @param delYn    삭제 여부 ('N'=미삭제, 'Y'=삭제)
     * @return 조건에 맞는 품목 엔티티 목록
     */
    List<Bitemm> findByPrjMngNoAndPrjSnoAndDelYn(String prjMngNo, Integer prjSno, String delYn);

    /**
     * Oracle 시퀀스(S_GCL) 다음 값 조회
     *
     * <p>신규 품목 생성 시 품목관리번호(GCL_MNG_NO) 채번에 사용합니다.
     * Oracle DB 전용 Native Query입니다.</p>
     *
     * @return 시퀀스의 다음 값 (Long)
     */
    @org.springframework.data.jpa.repository.Query(value = "SELECT S_GCL.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextSequenceValue();
}
