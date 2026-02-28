package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 이력 엔티티
 *
 * <p>DB 테이블: {@code LOGIN_HISTORY}</p>
 *
 * <p>사용자의 로그인, 로그인 실패, 로그아웃 이력을 기록합니다.
 * 보안 감사(Security Audit) 및 이상 접근 탐지에 활용됩니다.</p>
 *
 * <p>이력 유형({@code LOGIN_TYPE}):</p>
 * <ul>
 *   <li>{@code LOGIN_SUCCESS}: 로그인 성공</li>
 *   <li>{@code LOGIN_FAILURE}: 로그인 실패 (비밀번호 불일치, 존재하지 않는 사번 등)</li>
 *   <li>{@code LOGOUT}: 로그아웃</li>
 * </ul>
 *
 * <p>주의: {@link BaseEntity}를 상속하지 않고 독립적인 엔티티로 설계됩니다.
 * (ID 자동 증가, 삭제 불필요 등의 특성)</p>
 */
@Entity                  // JPA 엔티티로 등록
@Table(name = "LOGIN_HISTORY") // 매핑할 DB 테이블명
@Getter                  // 모든 필드의 getter 자동 생성 (Lombok)
@NoArgsConstructor       // 기본 생성자 자동 생성 (JPA 요구사항)
@AllArgsConstructor      // 전체 필드 생성자 자동 생성
@Builder                 // Builder 패턴 지원 (정적 팩토리 메서드와 함께 사용)
public class LoginHistory {

    /**
     * 이력 ID: 기본키. DB 시퀀스(IDENTITY 전략)로 자동 증가
     * (Oracle: SEQ, MySQL: AUTO_INCREMENT)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * 사번(행번): 로그인을 시도한 사용자의 사번
     * 로그인 실패의 경우 DB에 없는 사번도 기록될 수 있음
     */
    @Column(name = "ENO", nullable = false, length = 20)
    private String eno;

    /**
     * 로그인 타입: 이력의 종류
     * 값: "LOGIN_SUCCESS", "LOGIN_FAILURE", "LOGOUT"
     */
    @Column(name = "LOGIN_TYPE", nullable = false, length = 20)
    private String loginType;

    /** IP 주소: 클라이언트의 실제 IP 주소 (프록시 환경 고려, 최대 50자) */
    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    /** User Agent: 클라이언트 브라우저/기기 정보 (최대 500자) */
    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    /** 로그인 시간: 이벤트 발생 일시 (서버 기준 시각) */
    @Column(name = "LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    /**
     * 실패 사유: 로그인 실패 시 실패 원인 메시지 (최대 500자)
     * LOGIN_SUCCESS, LOGOUT의 경우 null
     * 예: "비밀번호 불일치", "존재하지 않는 사번"
     */
    @Column(name = "FAILURE_REASON", length = 500)
    private String failureReason;

    /**
     * 로그인 성공 이력 생성 정적 팩토리 메서드
     *
     * <p>로그인 성공 시 호출하여 이력 엔티티를 생성합니다.</p>
     *
     * @param eno       로그인에 성공한 사용자의 사번
     * @param ipAddress 접속 IP 주소
     * @param userAgent 접속 브라우저/기기 정보
     * @return 로그인 성공 이력 엔티티 ({@code loginType = "LOGIN_SUCCESS"})
     */
    public static LoginHistory createLoginSuccess(String eno, String ipAddress, String userAgent) {
        return LoginHistory.builder()
                .eno(eno)
                .loginType("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now()) // 현재 서버 시각으로 이력 기록
                .build();
    }

    /**
     * 로그인 실패 이력 생성 정적 팩토리 메서드
     *
     * <p>로그인 실패 시(비밀번호 불일치, 존재하지 않는 사번 등) 호출합니다.</p>
     *
     * @param eno           로그인을 시도한 사번 (DB에 없는 사번일 수 있음)
     * @param ipAddress     접속 IP 주소
     * @param userAgent     접속 브라우저/기기 정보
     * @param failureReason 실패 사유 (예: "비밀번호 불일치", "존재하지 않는 사번")
     * @return 로그인 실패 이력 엔티티 ({@code loginType = "LOGIN_FAILURE"})
     */
    public static LoginHistory createLoginFailure(String eno, String ipAddress, String userAgent, String failureReason) {
        return LoginHistory.builder()
                .eno(eno)
                .loginType("LOGIN_FAILURE")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .failureReason(failureReason) // 실패 원인 기록
                .build();
    }

    /**
     * 로그아웃 이력 생성 정적 팩토리 메서드
     *
     * <p>정상 로그아웃 시 호출하여 이력을 기록합니다.</p>
     *
     * @param eno       로그아웃한 사용자의 사번
     * @param ipAddress 접속 IP 주소
     * @param userAgent 접속 브라우저/기기 정보
     * @return 로그아웃 이력 엔티티 ({@code loginType = "LOGOUT"})
     */
    public static LoginHistory createLogout(String eno, String ipAddress, String userAgent) {
        return LoginHistory.builder()
                .eno(eno)
                .loginType("LOGOUT")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .build();
    }
}
