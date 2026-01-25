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

public class LoginHistoryDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "LoginHistoryResponse", description = "로그인 이력 응답")
    public static class Response {
        @Schema(description = "이력 ID")
        private Long id;

        @Schema(description = "사번")
        private String eno;

        @Schema(description = "로그인 타입 (LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT)")
        private String loginType;

        @Schema(description = "IP 주소")
        private String ipAddress;

        @Schema(description = "User Agent")
        private String userAgent;

        @Schema(description = "로그인 시간")
        private LocalDateTime loginTime;

        @Schema(description = "실패 사유")
        private String failureReason;

        public static Response fromEntity(LoginHistory loginHistory) {
            return Response.builder()
                    .id(loginHistory.getId())
                    .eno(loginHistory.getEno())
                    .loginType(loginHistory.getLoginType())
                    .ipAddress(loginHistory.getIpAddress())
                    .userAgent(loginHistory.getUserAgent())
                    .loginTime(loginHistory.getLoginTime())
                    .failureReason(loginHistory.getFailureReason())
                    .build();
        }

        public static List<Response> fromEntities(List<LoginHistory> loginHistories) {
            return loginHistories.stream()
                    .map(Response::fromEntity)
                    .collect(Collectors.toList());
        }
    }
}
