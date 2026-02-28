package com.kdb.it.config;

import com.kdb.it.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.kdb.it.service.AuthService;

import java.util.List;

/**
 * Spring Security 보안 설정 클래스
 *
 * <p>
 * JWT 기반 Stateless 인증 방식의 보안 정책을 정의합니다.
 * </p>
 *
 * <p>
 * 주요 보안 설정:
 * </p>
 * <ul>
 * <li>CSRF: 비활성화 (JWT 사용으로 불필요)</li>
 * <li>세션: STATELESS (JWT 토큰으로 인증 상태 유지)</li>
 * <li>CORS: 전체 Origin 허용 (개발 환경)</li>
 * <li>인증 필터: {@link JwtAuthenticationFilter} →
 * {@link UsernamePasswordAuthenticationFilter} 앞에 삽입</li>
 * </ul>
 *
 * <p>
 * 공개 엔드포인트 (인증 불필요):
 * </p>
 * <ul>
 * <li>{@code POST /api/auth/login}: 로그인</li>
 * <li>{@code POST /api/auth/signup}: 회원가입</li>
 * <li>{@code POST /api/auth/refresh}: 토큰 갱신</li>
 * <li>{@code /swagger-ui/**}: Swagger UI</li>
 * <li>{@code /v3/api-docs/**}: OpenAPI 명세</li>
 * </ul>
 */
@Configuration // Spring 설정 클래스로 등록
@EnableWebSecurity // Spring Security 활성화
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class SecurityConfig {

    /** JWT 인증 처리 필터 (매 요청마다 JWT 토큰 검증) */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 필터 체인 설정
     *
     * <p>
     * HTTP 요청에 대한 보안 정책을 정의합니다.
     * </p>
     *
     * <p>
     * 필터 체인 처리 순서:
     * </p>
     * <ol>
     * <li>CORS 처리</li>
     * <li>CSRF 비활성화</li>
     * <li>세션 STATELESS 설정</li>
     * <li>URL별 인증 권한 검사</li>
     * <li>예외 처리 (인증 실패, 접근 거부)</li>
     * <li>JWT 인증 필터 실행</li>
     * </ol>
     *
     * @param http {@link HttpSecurity} 설정 빌더
     * @return 설정된 {@link SecurityFilterChain}
     * @throws Exception 설정 중 오류 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용 (corsConfigurationSource 빈 사용)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // CSRF 보호 비활성화 (JWT 사용 시 불필요; REST API는 CSRF 공격 대상이 아님)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless 세션 설정 (JWT 사용): 서버가 세션을 생성/유지하지 않음
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URL별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // 인증 없이 접근 가능한 엔드포인트
                        .requestMatchers("/api/auth/login", "/api/auth/signup", "/api/auth/refresh",
                                "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/error")
                        .permitAll()
                        // 나머지는 인증 필요 (유효한 JWT 토큰 필수)
                        .anyRequest().authenticated())
                // 인증/접근 예외 처리 핸들러 설정
                .exceptionHandling(exception -> exception
                        // 인증 실패 시 처리 (401 Unauthorized)
                        .authenticationEntryPoint((request, response, authException) -> {
                            System.out.println("=== 인증 실패 ===");
                            System.out.println("요청 URI: " + request.getRequestURI());
                            System.out.println("요청 메서드: " + request.getMethod());
                            System.out.println("클라이언트 IP: " + request.getRemoteAddr());
                            System.out.println("Authorization 헤더: " + request.getHeader("Authorization"));
                            System.out.println("오류 메시지: " + authException.getMessage());
                            System.out.println("==================");
                            // HTTP 401 응답 반환
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        // 권한 부족 시 처리 (403 Forbidden)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            System.out.println("=== 접근 거부 ===");
                            System.out.println("요청 URI: " + request.getRequestURI());
                            System.out.println("요청 메서드: " + request.getMethod());
                            System.out.println("클라이언트 IP: " + request.getRemoteAddr());
                            System.out.println("오류 메시지: " + accessDeniedException.getMessage());
                            System.out.println("==================");
                            // HTTP 403 응답 반환
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        }))
                // JWT 필터를 UsernamePasswordAuthenticationFilter 앞에 삽입
                // → 모든 요청에서 JWT 토큰 먼저 검증
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS(Cross-Origin Resource Sharing) 설정
     *
     * <p>
     * 프론트엔드와 백엔드가 다른 도메인/포트에서 실행될 때 브라우저의
     * CORS 정책을 허용하도록 설정합니다.
     * </p>
     *
     * <p>
     * 현재 설정 (개발 환경):
     * </p>
     * <ul>
     * <li>허용 Origin: 전체 ({@code *}) - 운영 환경에서는 특정 도메인으로 제한 필요</li>
     * <li>허용 메서드: GET, POST, PUT, DELETE, OPTIONS, PATCH</li>
     * <li>허용 헤더: 전체 ({@code *})</li>
     * <li>자격증명(쿠키 등) 포함 허용: true</li>
     * </ul>
     *
     * @return 모든 경로({@code /**})에 적용되는 {@link CorsConfigurationSource}
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 모든 Origin 허용 (개발 환경용; 운영 시 특정 도메인으로 제한 권장)
        configuration.setAllowedOriginPatterns(List.of("*")); // Allow all origins for dev
        // 허용할 HTTP 메서드 목록
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // 모든 요청 헤더 허용 (Authorization, Content-Type 등)
        configuration.setAllowedHeaders(List.of("*"));
        // 쿠키, Authorization 헤더 등 자격증명 포함 허용
        configuration.setAllowCredentials(true);

        // 모든 URL 경로에 CORS 설정 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 비밀번호 인코더 빈 등록
     *
     * <p>
     * SHA-256 기반의 {@link CustomPasswordEncoder}를 Spring Security의
     * {@link PasswordEncoder}로 등록합니다. 회원가입 시 비밀번호 암호화,
     * 로그인 시 비밀번호 검증에 사용됩니다.
     * </p>
     *
     * @return {@link CustomPasswordEncoder} 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new CustomPasswordEncoder();
    }

    /**
     * 인증 관리자 빈 등록
     *
     * <p>
     * {@link AuthenticationManager}는 Spring Security의 인증 처리 핵심 컴포넌트입니다.
     * {@link AuthService}에서 사용자 인증(로그인) 시 활용됩니다.
     * </p>
     *
     * @param authenticationConfiguration Spring Security 인증 설정 컨텍스트
     * @return {@link AuthenticationManager} 인스턴스
     * @throws Exception 인증 관리자 생성 실패 시
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
