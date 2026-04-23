package com.kdb.it.domain.budget.document.repository;

import com.kdb.it.domain.budget.document.entity.Brivgm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 문서 검토의견(Brivgm) JPA 리포지토리
 *
 * <p>
 * 테이블: {@code TAAABB_BRIVGM}
 * </p>
 *
 * <p>
 * 기본 CRUD는 {@link JpaRepository}를 통해 제공되며, 본 인터페이스는 파생 쿼리 메서드만 선언합니다.
 * 복잡한 동적 쿼리가 필요해지면 별도의 {@code BrivgmRepositoryCustom} + QueryDSL 구현체로 분리합니다.
 * </p>
 */
public interface BrivgmRepository extends JpaRepository<Brivgm, String> {

    /**
     * 특정 문서+버전의 미삭제 코멘트 전체 조회 (생성일 오름차순)
     *
     * @param docMngNo 문서관리번호 (예: DOC-2026-0001)
     * @param docVrs   문서버전 (Oracle NUMBER(5,2))
     * @param delYn    삭제여부 ('N'=미삭제, 'Y'=삭제)
     * @return 조건에 해당하는 검토의견 목록 (생성일시 오름차순 정렬)
     */
    List<Brivgm> findByDocMngNoAndDocVrsAndDelYnOrderByFstEnrDtmAsc(
            String docMngNo, BigDecimal docVrs, String delYn);

    /**
     * 코멘트 단건 조회 (미삭제 건만 대상)
     *
     * @param ivgSno 의견일련번호 (UUID v4 기반 32자)
     * @param delYn  삭제여부 ('N'=미삭제, 'Y'=삭제)
     * @return 조건에 해당하는 검토의견 (없으면 {@link Optional#empty()})
     */
    Optional<Brivgm> findByIvgSnoAndDelYn(String ivgSno, String delYn);
}
