package com.kdb.it.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "LoginRequest", description = "로그인 요청")
    public static class LoginRequest {
        @Schema(description = "사번")
        private String eno;
        
        @Schema(description = "비밀번호")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "SignupRequest", description = "회원가입 요청")
    public static class SignupRequest {
        @Schema(description = "사번")
        private String eno;
        
        @Schema(description = "이름")
        private String empNm;
        
        @Schema(description = "비밀번호")
        private String password;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "LoginResponse", description = "로그인 응답")
    public static class LoginResponse {
        @Schema(description = "Access Token")
        private String accessToken;
        
        @Schema(description = "Refresh Token")
        private String refreshToken;
        
        @Schema(description = "사번")
        private String eno;
        
        @Schema(description = "이름")
        private String empNm;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @Schema(name = "RefreshRequest", description = "토큰 갱신 요청")
    public static class RefreshRequest {
        @Schema(description = "Refresh Token")
        private String refreshToken;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(name = "RefreshResponse", description = "토큰 갱신 응답")
    public static class RefreshResponse {
        @Schema(description = "새로운 Access Token")
        private String accessToken;
    }
}
