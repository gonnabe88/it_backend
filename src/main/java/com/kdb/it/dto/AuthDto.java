package com.kdb.it.dto;

import lombok.Getter;
import lombok.Setter;

public class AuthDto {

    @Getter
    @Setter
    public static class LoginRequest {
        private String eno;
        private String password;
    }

    @Getter
    @Setter
    public static class SignupRequest {
        private String eno;
        private String empNm;
        private String password;
    }

    @Getter
    @Setter
    public static class LoginResponse {
        private String token; // For future use if JWT is implemented, currently just success message or
                              // Session ID implicitly
        private String eno;
        private String empNm;

        public LoginResponse(String eno, String empNm) {
            this.eno = eno;
            this.empNm = empNm;
        }
    }
}
