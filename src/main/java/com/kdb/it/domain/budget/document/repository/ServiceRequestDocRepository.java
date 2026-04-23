package com.kdb.it.domain.budget.document.repository;

import com.kdb.it.domain.budget.document.entity.Brdocm;
import com.kdb.it.domain.budget.document.entity.BrdocmId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 요구사항 정의서(Brdocm) 데이터 접근 리포지토리
 *
 * <p>
 * Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 요구사항 정의서 테이블(TAAABB_BRDOCM)의 기본 CRUD 기능을 제공합니다.
 * </p>
 *
 * <p>
 * 테이블 PK는 (DOC_MNG_NO, DOC_VRS) 복합키이며, 복합키 클래스 {@link BrdocmId} 를 사용합니다.
 * </p>
 *
 * <p>
 * Soft Delete 패턴 적용: 조회 시 {@code delYn='N'} 조건을 사용합니다.
 * </p>
 */
@Repository
public interface ServiceRequestDocRepository extends JpaRepository<Brdocm, BrdocmId> {

    /**
     * 문서관리번호로 최신 버전 단건 조회 (일반 조회/수정/삭제의 기본 진입점)
     *
     * @param docMngNo 문서관리번호
     * @param delYn    삭제여부 ('N'=미삭제)
     * @return 최신 버전의 요구사항 정의서
     */
    Optional<Brdocm> findTopByDocMngNoAndDelYnOrderByDocVrsDesc(String docMngNo, String delYn);

    /**
     * 문서관리번호 + 특정 버전 단건 조회
     *
     * @param docMngNo 문서관리번호
     * @param docVrs   문서 버전
     * @param delYn    삭제여부 ('N'=미삭제)
     * @return 조건에 맞는 요구사항 정의서
     */
    Optional<Brdocm> findByDocMngNoAndDocVrsAndDelYn(String docMngNo, BigDecimal docVrs, String delYn);

    /**
     * 문서관리번호로 전체 버전 히스토리 조회 (버전 내림차순)
     *
     * @param docMngNo 문서관리번호
     * @param delYn    삭제여부 ('N'=미삭제)
     * @return 전체 버전 목록 (버전 내림차순)
     */
    List<Brdocm> findAllByDocMngNoAndDelYnOrderByDocVrsDesc(String docMngNo, String delYn);

    /**
     * 문서관리번호의 전체 버전 조회 (소프트 삭제 일괄 처리용, 순서 무관)
     *
     * @param docMngNo 문서관리번호
     * @param delYn    삭제여부 ('N'=미삭제)
     * @return 전체 버전 목록
     */
    List<Brdocm> findAllByDocMngNoAndDelYn(String docMngNo, String delYn);

    /**
     * 삭제여부로 전체 목록 조회
     *
     * @param delYn 삭제여부 ('N'=미삭제)
     * @return 조건에 맞는 요구사항 정의서 목록
     */
    List<Brdocm> findAllByDelYn(String delYn);

    /**
     * 문서관리번호와 삭제여부로 존재 여부 확인
     *
     * @param docMngNo 문서관리번호
     * @param delYn    삭제여부 ('N'=미삭제)
     * @return 존재하면 {@code true}
     */
    boolean existsByDocMngNoAndDelYn(String docMngNo, String delYn);

    /**
     * 문서관리번호 단위 최신 버전 목록 조회 (목록 화면용)
     *
     * <p>
     * 각 DOC_MNG_NO 그룹 내에서 가장 큰 DOC_VRS 행만 반환합니다.
     * 삭제되지 않은(DEL_YN='N') 레코드만 대상으로 하며,
     * 최초 등록일시(FST_ENR_DTM) 내림차순으로 정렬합니다.
     * </p>
     *
     * @return 문서별 최신 버전 목록
     */
    @Query(value = """
        SELECT * FROM TAAABB_BRDOCM d
        WHERE d.DEL_YN = 'N'
          AND d.DOC_VRS = (
              SELECT MAX(d2.DOC_VRS) FROM TAAABB_BRDOCM d2
              WHERE d2.DOC_MNG_NO = d.DOC_MNG_NO AND d2.DEL_YN = 'N'
          )
        ORDER BY d.FST_ENR_DTM DESC
        """, nativeQuery = true)
    List<Brdocm> findLatestVersionsAll();

    /**
     * Oracle 시퀀스(S_DOC) 다음 값 조회
     *
     * <p>
     * 신규 요구사항 정의서 생성 시 문서관리번호 채번에 사용합니다.
     * 형식: {@code DOC-{연도}-{4자리 시퀀스}} (예: {@code DOC-2026-0001})
     * </p>
     *
     * @return Oracle 시퀀스(S_DOC)의 다음 값
     */
    @Query(value = "SELECT S_DOC.NEXTVAL FROM DUAL", nativeQuery = true)
    Long getNextSequenceValue();
}
