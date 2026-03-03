package com.kdb.it.domain.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * 정보화사업(Project) 엔티티의 복합 기본키 클래스
 *
 * <p>
 * {@link Project} 엔티티의 복합키를 정의합니다.
 * JPA {@code @IdClass}로 사용됩니다.
 * </p>
 *
 * <p>
 * 복합키 구성:
 * </p>
 * <ul>
 * <li>{@code prjMngNo}: 프로젝트관리번호 (예: PRJ-2026-0001)</li>
 * <li>{@code prjSno}: 프로젝트순번 (동일 관리번호 내 버전 구분)</li>
 * </ul>
 *
 * <p>
 * JPA 복합키 클래스 요구사항:
 * </p>
 * <ul>
 * <li>{@link Serializable} 구현 필수</li>
 * <li>{@link #equals(Object)}, {@link #hashCode()} 오버라이드 필수</li>
 * <li>기본 생성자 필수</li>
 * <li>필드명이 엔티티의 {@code @Id} 필드명과 정확히 일치해야 함</li>
 * </ul>
 */
public class ProjectId implements Serializable {

    /** 프로젝트관리번호 (예: PRJ-2026-0001) */
    private String prjMngNo;

    /** 프로젝트순번 (동일 관리번호 내 버전 구분, 1부터 시작) */
    private Integer prjSno;

    /** 기본 생성자 (JPA 필수) */
    public ProjectId() {
    }

    /**
     * 전체 필드 생성자
     *
     * @param prjMngNo 프로젝트관리번호
     * @param prjSno   프로젝트순번
     */
    public ProjectId(String prjMngNo, Integer prjSno) {
        this.prjMngNo = prjMngNo;
        this.prjSno = prjSno;
    }

    /**
     * 동등성 비교: 두 복합키의 prjMngNo와 prjSno가 모두 같으면 동일한 키
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ProjectId that = (ProjectId) o;
        return Objects.equals(prjMngNo, that.prjMngNo) && Objects.equals(prjSno, that.prjSno);
    }

    /**
     * 해시코드: prjMngNo와 prjSno 기반 해시 생성
     */
    @Override
    public int hashCode() {
        return Objects.hash(prjMngNo, prjSno);
    }
}
