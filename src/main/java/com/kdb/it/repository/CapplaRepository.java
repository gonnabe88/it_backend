package com.kdb.it.repository;

import com.kdb.it.domain.entity.Cappla;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

/**
 * 신청서-원본 데이터 관계(Cappla) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속하여 기본 CRUD 기능을 제공하며,
 * 신청서와 원본 데이터 간의 연결 관계를 조회하는 특화 메서드를 제공합니다.</p>
 *
 * <p>기본키 타입: {@link String} (apfRelSno: 신청서관계일련번호)</p>
 *
 * <p>주요 활용:</p>
 * <ul>
 *   <li>특정 원본 데이터(프로젝트, 전산관리비 등)에 연결된 신청서 조회</li>
 *   <li>결재중/결재완료 상태의 신청서 존재 여부 확인 (수정/삭제 제약)</li>
 * </ul>
 */
public interface CapplaRepository extends JpaRepository<Cappla, String> {

    /**
     * Oracle 시퀀스(S_APF_REL_SNO) 다음 값 조회
     *
     * <p>신청서관계일련번호(APF_REL_SNO) 채번에 사용합니다.
     * Oracle DB 전용 Native Query입니다.</p>
     *
     * @return 시퀀스의 다음 값 (Long)
     */
    @Query(value = "SELECT S_APF_REL_SNO.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextVal();

    /**
     * 원본 테이블 코드, PK값, SNO값으로 신청서 관계 목록 조회 (최신순)
     *
     * <p>특정 원본 데이터에 연결된 신청서 목록을 최신순으로 반환합니다.
     * 주로 프로젝트의 최신 신청서 정보를 조회하는 데 사용됩니다.</p>
     *
     * <p>사용 예: 프로젝트 조회 시 최신 신청서 정보 표시</p>
     *
     * @param orcTbCd  원본 테이블 코드 (예: 'BPRJTM'=프로젝트)
     * @param orcPkVl  원본 데이터의 PK 값 (예: 프로젝트 관리번호)
     * @param orcSnoVl 원본 데이터의 순번 값 (예: 프로젝트 순번)
     * @return 관련 신청서 관계 목록 (APF_REL_SNO 역순 정렬, 즉 최신 신청서가 첫 번째)
     */
    java.util.List<Cappla> findByOrcTbCdAndOrcPkVlAndOrcSnoVlOrderByApfRelSnoDesc(String orcTbCd, String orcPkVl,
            Integer orcSnoVl);

    /**
     * 원본 데이터에 특정 상태의 신청서가 존재하는지 확인
     *
     * <p>원본 테이블 코드, PK, SNO 조건으로 Cappla와 Capplm을 조인하여
     * 지정한 상태 목록({@code statuses})에 해당하는 신청서가 존재하는지 확인합니다.</p>
     *
     * <p>주요 사용처: 프로젝트/전산관리비 수정·삭제 전 결재중 또는 결재완료 여부 검사</p>
     *
     * <p>JPQL 쿼리: Cappla와 Capplm을 apfMngNo로 조인하여 상태 검사</p>
     *
     * @param orcTbCd  원본 테이블 코드 (예: 'BPRJTM')
     * @param orcPkVl  원본 데이터의 PK 값
     * @param orcSnoVl 원본 데이터의 순번 값
     * @param statuses 확인할 신청서 상태 목록 (예: ["결재중", "결재완료"])
     * @return 해당 조건의 신청서가 존재하면 true, 없으면 false
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
