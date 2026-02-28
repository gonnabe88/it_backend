package com.kdb.it.dto;

import com.kdb.it.domain.entity.LoginHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 로그인 이력 관련 DTO 클래스 모음
 *
 * <p>로그인/로그아웃 이력(LOGIN_HISTORY) 조회에 사용되는 Response DTO를
 * 정적 중첩 클래스(Static Nested Class) 형태로 관리합니다.</p>
 *
 * <p>포함된 DTO:</p>
 * <ul>
 *   <li>{@link Response}: 로그인 이력 조회 응답 (단건 및 목록 변환 지원)</li>
 * </ul>
 */
public class LoginHistoryDto {

    /**
     * 로그인 이력 조회 응답 DTO
     *
     * <p>{@link LoginHistory} 엔티티의 정보를 클라이언트에 전달합니다.</p>
     *
     * <p>로그인 유형({@code loginType}) 종류:</p>
     * <ul>
     *   <li>{@code LOGIN_SUCCESS}: 로그인 성공</li>
     *   <li>{@code LOGIN_FAILURE}: 로그인 실패 (비밀번호 불일치, 사번 없음 등)</li>
     *   <li>{@code LOGOUT}: 로그아웃</li>
     * </ul>
     *
     * <p>{@link #fromEntity(LoginHistory)}: 단건 변환</p>
     * <p>{@link #fromEntities(List)}: 목록 변환</p>
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "LoginHistoryResponse", description = "로그인 이력 응답")
    public static class Response {
        /** 이력 ID (자동 증가 PK) */
        @Schema(description = "이력 ID")
        private Long id;

        /** 사번 (이력의 주체 사용자) */
        @Schema(description = "사번")
        private String eno;

        /**
         * 로그인 타입
         * <p>허용값: {@code LOGIN_SUCCESS}, {@code LOGIN_FAILURE}, {@code LOGOUT}</p>
         */
        @Schema(description = "로그인 타입 (LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT)")
        private String loginType;

        /** 클라이언트 IP 주소 (X-Forwarded-For, X-Real-IP 헤더 우선 적용) */
        @Schema(description = "IP 주소")
        private String ipAddress;

        /** 클라이언트 브라우저/앱 정보 (User-Agent 헤더 값) */
        @Schema(description = "User Agent")
        private String userAgent;

        /** 이벤트 발생 시각 (로그인/로그아웃 시각) */
        @Schema(description = "로그인 시간")
        private LocalDateTime loginTime;

        /**
         * 로그인 실패 사유
         * <p>loginType이 {@code LOGIN_FAILURE}인 경우에만 값이 있습니다.
         * (예: "비밀번호 불일치", "존재하지 않는 사번")</p>
         */
        @Schema(description = "실패 사유")
        private String failureReason;

        /**
         * {@link LoginHistory} 엔티티를 단건 응답 DTO로 변환하는 정적 팩토리 메서드
         *
         * @param loginHistory 변환할 LoginHistory 엔티티
         * @return 변환된 응답 DTO
         */
        public static Response fromEntity(LoginHistory loginHistory) {
            return Response.builder()
                    .id(loginHistory.getId())                     // 이력 ID
                    .eno(loginHistory.getEno())                   // 사번
                    .loginType(loginHistory.getLoginType())       // 로그인 타입
                    .ipAddress(loginHistory.getIpAddress())       // IP 주소
                    .userAgent(loginHistory.getUserAgent())       // User Agent
                    .loginTime(loginHistory.getLoginTime())       // 이벤트 시각
                    .failureReason(loginHistory.getFailureReason()) // 실패 사유
                    .build();
        }

        /**
         * {@link LoginHistory} 엔티티 목록을 응답 DTO 목록으로 변환하는 정적 팩토리 메서드
         *
         * <p>{@link com.kdb.it.service.LoginHistoryService}에서 목록 변환 시 사용합니다.</p>
         *
         * @param loginHistories 변환할 LoginHistory 엔티티 목록
         * @return 변환된 응답 DTO 목록
         */
        public static List<Response> fromEntities(List<LoginHistory> loginHistories) {
            return loginHistories.stream()
                    .map(Response::fromEntity) // 각 엔티티를 DTO로 변환
                    .collect(Collectors.toList());
        }
    }
}
