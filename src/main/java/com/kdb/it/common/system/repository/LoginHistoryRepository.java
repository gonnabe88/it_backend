package com.kdb.it.common.system.repository;

import com.kdb.it.common.system.entity.Clognh;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로그인이력(Clognh) 데이터 접근 리포지토리
 *
 * <p>Spring Data JPA의 {@link JpaRepository}를 상속하여
 * 로그인이력 테이블(TAAABB_CLOGNH)에 대한 CRUD 기능을 제공합니다.</p>
 *
 * <p>기본키 타입: {@link Long} (lgnSno: Oracle 시퀀스 S_LGN_SNO)</p>
 *
 * <p>보안 감사 목적의 이력 조회 메서드를 제공합니다.</p>
 */
public interface LoginHistoryRepository extends JpaRepository<Clognh, Long> {

    /**
     * 사번으로 로그인 이력 조회 (최신순)
     *
     * <p>특정 사용자의 전체 로그인 이력을 최신 순으로 반환합니다.
     * 최신순 정렬은 {@code LGN_DTM DESC}로 처리됩니다.</p>
     *
     * @param eno 조회할 사용자의 사번
     * @return 해당 사용자의 로그인 이력 목록 (로그인일시 내림차순)
     */
    List<Clognh> findByEnoOrderByLgnDtmDesc(String eno);

    /**
     * 사번과 날짜 범위로 로그인 이력 조회 (최신순)
     *
     * <p>특정 기간 동안의 사용자 로그인 이력을 조회합니다.
     * 이상 접근 탐지(특정 기간 내 과도한 로그인 실패 등)에 활용할 수 있습니다.</p>
     *
     * @param eno       조회할 사용자의 사번
     * @param startTime 조회 시작 시각 (포함)
     * @param endTime   조회 종료 시각 (포함)
     * @return 해당 기간의 로그인 이력 목록 (로그인일시 내림차순)
     */
    List<Clognh> findByEnoAndLgnDtmBetweenOrderByLgnDtmDesc(
            String eno, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 로그인유형으로 이력 조회 (최신순)
     *
     * <p>특정 유형의 이력(예: 로그인 실패만)을 전체에서 조회합니다.
     * 관리자 보안 모니터링에 활용할 수 있습니다.</p>
     *
     * @param lgnTp 조회할 이력 유형 ("LOGIN_SUCCESS", "LOGIN_FAILURE", "LOGOUT")
     * @return 해당 유형의 로그인 이력 목록 (로그인일시 내림차순)
     */
    List<Clognh> findByLgnTpOrderByLgnDtmDesc(String lgnTp);

    /**
     * 특정 사용자의 최근 50개 이력 조회 (최신순)
     *
     * <p>로그인 이력 목록 조회 시 DB 레벨에서 50건으로 제한합니다.
     * {@code findTop50By} 접두사를 통해 Spring Data JPA가 자동으로 LIMIT 50 처리합니다.</p>
     *
     * @param eno 조회할 사용자의 사번
     * @return 해당 사용자의 최근 50개 로그인 이력 (로그인일시 내림차순)
     */
    List<Clognh> findTop50ByEnoOrderByLgnDtmDesc(String eno);

    /**
     * 특정 사용자의 최근 10개 이력 조회 (최신순)
     *
     * <p>사용자 대시보드에서 최근 접속 이력을 간략하게 표시하는 데 사용됩니다.
     * {@code findTop10By} 접두사를 통해 Spring Data JPA가 자동으로 LIMIT 10 처리합니다.</p>
     *
     * @param eno 조회할 사용자의 사번
     * @return 해당 사용자의 최근 10개 로그인 이력 (로그인일시 내림차순)
     */
    List<Clognh> findTop10ByEnoOrderByLgnDtmDesc(String eno);
}
