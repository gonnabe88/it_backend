package com.kdb.it.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdb.it.config.JacksonConfig;
import com.kdb.it.config.TestSecurityConfig;
import com.kdb.it.dto.AuthDto;
import com.kdb.it.service.AuthService;
import com.kdb.it.service.CustomUserDetailsService;
import com.kdb.it.util.CookieUtil;
import com.kdb.it.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController @WebMvcTest
 *
 * <p>Web 레이어만 로드하여 인증 API의 HTTP 요청/응답 스펙을 검증합니다.
 * 토큰은 응답 body가 아닌 Set-Cookie 헤더로 전달됨을 검증합니다.</p>
 */
@WebMvcTest(AuthController.class)
@Import({TestSecurityConfig.class, JacksonConfig.class})
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthService authService;
    @MockitoBean private CookieUtil cookieUtil;
    @MockitoBean private JwtUtil jwtUtil;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @Test
    @DisplayName("POST /api/auth/signup - 성공 시 200 + '회원가입 성공' 반환")
    void signup_성공_200반환() throws Exception {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        request.setEno("10001");
        request.setEmpNm("홍길동");
        request.setPassword("password123");

        doNothing().when(authService).signup(org.mockito.ArgumentMatchers.any(AuthDto.SignupRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 성공 시 200 + eno/empNm body + Set-Cookie 헤더")
    void login_성공_200및쿠키반환() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEno("10001");
        request.setPassword("password123");

        AuthDto.LoginResponse loginResponse = AuthDto.LoginResponse.builder()
                .eno("10001").empNm("홍길동")
                .accessToken("access-token").refreshToken("refresh-token")
                .build();

        ResponseCookie accessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE, "access-token")
                .httpOnly(true).path("/").build();
        ResponseCookie refreshCookie = ResponseCookie.from(CookieUtil.REFRESH_TOKEN_COOKIE, "refresh-token")
                .httpOnly(true).path("/api/auth").build();

        given(authService.login(anyString(), anyString(), anyString(), anyString()))
                .willReturn(loginResponse);
        given(cookieUtil.createAccessTokenCookie("access-token")).willReturn(accessCookie);
        given(cookieUtil.createRefreshTokenCookie("refresh-token")).willReturn(refreshCookie);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "TestAgent")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eno").value("10001"))
                .andExpect(jsonPath("$.empNm").value("홍길동"))
                // @JsonIgnore: 토큰이 응답 body에 없어야 함
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                // 토큰은 Set-Cookie 헤더로 전달
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 서비스 예외 시 500 반환")
    void login_서비스예외_500반환() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEno("99999");
        request.setPassword("password");

        given(authService.login(anyString(), anyString(), anyString(), anyString()))
                .willThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "TestAgent")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Refresh Token 쿠키 없으면 401 반환")
    void refresh_쿠키없음_401반환() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 유효한 Refresh Token 쿠키 → 200 + '토큰 갱신 성공'")
    void refresh_유효한쿠키_200반환() throws Exception {
        // given
        AuthDto.RefreshResponse refreshResponse = AuthDto.RefreshResponse.builder()
                .accessToken("new-access-token").build();

        ResponseCookie newAccessCookie = ResponseCookie.from(CookieUtil.ACCESS_TOKEN_COOKIE, "new-access-token")
                .httpOnly(true).path("/").build();

        given(authService.refreshAccessToken("valid-refresh-token")).willReturn(refreshResponse);
        given(cookieUtil.createAccessTokenCookie("new-access-token")).willReturn(newAccessCookie);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(CookieUtil.REFRESH_TOKEN_COOKIE, "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(content().string("토큰 갱신 성공"));
    }
}
