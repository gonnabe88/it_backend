package com.kdb.it.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 로그인 이력 엔티티
 */
@Entity
@Table(name = "LOGIN_HISTORY")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ENO", nullable = false, length = 20)
    private String eno;

    @Column(name = "LOGIN_TYPE", nullable = false, length = 20)
    private String loginType; // LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT

    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 500)
    private String userAgent;

    @Column(name = "LOGIN_TIME", nullable = false)
    private LocalDateTime loginTime;

    @Column(name = "FAILURE_REASON", length = 500)
    private String failureReason;

    /**
     * 로그인 성공 이력 생성
     */
    public static LoginHistory createLoginSuccess(String eno, String ipAddress, String userAgent) {
        return LoginHistory.builder()
                .eno(eno)
                .loginType("LOGIN_SUCCESS")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .build();
    }

    /**
     * 로그인 실패 이력 생성
     */
    public static LoginHistory createLoginFailure(String eno, String ipAddress, String userAgent, String failureReason) {
        return LoginHistory.builder()
                .eno(eno)
                .loginType("LOGIN_FAILURE")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .loginTime(LocalDateTime.now())
                .failureReason(failureReason)
                .build();
    }

    /**
     * 로그아웃 이력 생성
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
