package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Refresh Token 엔티티
 *
 * <p>DB 테이블: {@code REFRESH_TOKEN}</p>
 *
 * <p>JWT Refresh Token을 DB에 저장하여 관리합니다.
 * 사용자당 최대 1개의 Refresh Token이 유지됩니다 (로그인 시 기존 토큰 삭제 후 재생성).</p>
 *
 * <p>토큰 수명:</p>
 * <ul>
 *   <li>Refresh Token: 7일 ({@code application.properties}의 {@code jwt.refresh-token-validity})</li>
 * </ul>
 *
 * <p>사용 흐름:</p>
 * <ol>
 *   <li>로그인 성공 → Refresh Token 생성 및 DB 저장</li>
 *   <li>Access Token 만료 → Refresh Token으로 새 Access Token 발급</li>
 *   <li>로그아웃 → DB에서 Refresh Token 삭제</li>
 * </ol>
 */
@Entity                       // JPA 엔티티로 등록
@Table(name = "REFRESH_TOKEN") // 매핑할 DB 테이블명
@Getter                       // 모든 필드의 getter 자동 생성 (Lombok)
@NoArgsConstructor            // 기본 생성자 자동 생성 (JPA 요구사항)
@AllArgsConstructor           // 전체 필드 생성자 자동 생성
@Builder                      // Builder 패턴 지원
public class RefreshToken {

    /**
     * 기본키: DB 시퀀스(IDENTITY 전략)로 자동 증가
     * (Oracle: SEQ, MySQL: AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * 토큰 문자열: JWT Refresh Token 값 (최대 500자)
     * UNIQUE 제약조건으로 중복 저장 방지
     */
    @Column(name = "TOKEN", nullable = false, unique = true, length = 500)
    private String token;

    /**
     * 사번(행번): 이 토큰을 소유한 사용자의 사번
     * 로그아웃 또는 재로그인 시 사번으로 기존 토큰 삭제에 사용
     */
    @Column(name = "ENO", nullable = false, length = 20)
    private String eno;

    /**
     * 만료일시: 이 Refresh Token이 유효한 마지막 일시
     * 이 시각 이후에는 토큰이 만료된 것으로 간주
     */
    @Column(name = "EXPIRY_DATE", nullable = false)
    private LocalDateTime expiryDate;

    /** 생성일시: 이 Refresh Token이 DB에 저장된 시각 */
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 토큰 만료 여부 확인 메서드
     *
     * <p>현재 서버 시각이 {@code expiryDate}를 초과했는지 확인합니다.</p>
     *
     * @return true이면 만료됨 (삭제 필요), false이면 유효함
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    /**
     * 토큰 갱신 메서드
     *
     * <p>재로그인 없이 Refresh Token을 갱신할 때 사용합니다.
     * 토큰 값과 만료일시를 새 값으로 업데이트합니다.</p>
     *
     * @param token      새로운 Refresh Token 문자열
     * @param expiryDate 새로운 만료일시
     */
    public void updateToken(String token, LocalDateTime expiryDate) {
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
