package com.kdb.it.security;

import com.kdb.it.service.CustomUserDetailsService;
import com.kdb.it.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 *
 * <p>HTTP 요청마다 {@code Authorization} 헤더에서 JWT Access Token을 추출하고
 * 검증하여 Spring Security의 SecurityContext에 인증 정보를 설정합니다.</p>
 *
 * <p>{@link OncePerRequestFilter}를 상속하여 요청당 정확히 한 번만 실행되도록 보장합니다.</p>
 *
 * <p>필터 처리 흐름:</p>
 * <pre>
 *   HTTP 요청 수신
 *     ↓
 *   Authorization 헤더에서 "Bearer {token}" 추출
 *     ↓
 *   JwtUtil.validateToken() → 토큰 서명/만료 검증
 *     ↓ (유효한 경우)
 *   JwtUtil.getEnoFromToken() → 사번 추출
 *     ↓
 *   CustomUserDetailsService.loadUserByUsername() → UserDetails 로드
 *     ↓
 *   UsernamePasswordAuthenticationToken 생성
 *     ↓
 *   SecurityContextHolder에 인증 객체 설정
 *     ↓
 *   다음 필터 체인으로 전달
 * </pre>
 *
 * <p>실행 위치: {@code SecurityConfig}에서 {@link org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter} 앞에 삽입됩니다.</p>
 */
@Component           // Spring 컴포넌트 빈으로 등록 (자동 감지)
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT 토큰 생성/검증 유틸리티 */
    private final JwtUtil jwtUtil;

    /** 사용자 상세 정보 로드 서비스 (DB에서 UserDetails 조회) */
    private final CustomUserDetailsService userDetailsService;

    /**
     * JWT 인증 처리 핵심 메서드
     *
     * <p>각 HTTP 요청마다 실행되어 JWT 토큰을 검증하고 인증 정보를 설정합니다.
     * 토큰이 없거나 유효하지 않은 경우 인증 설정 없이 다음 필터로 넘어갑니다.
     * (이후 인증이 필요한 URL은 authenticationEntryPoint에서 401 처리)</p>
     *
     * @param request     HTTP 요청 객체 (Authorization 헤더 포함)
     * @param response    HTTP 응답 객체
     * @param filterChain 다음 필터로 요청을 전달하는 필터 체인
     * @throws ServletException 서블릿 처리 중 예외
     * @throws IOException      I/O 처리 중 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Authorization 헤더에서 JWT 토큰 추출 ("Bearer " 접두사 제거)
            String jwt = getJwtFromRequest(request);

            // 토큰이 있고 서명/만료 검증을 통과한 경우에만 인증 설정
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 토큰의 Payload에서 subject(사번) 추출
                String eno = jwtUtil.getEnoFromToken(jwt);

                // DB에서 사용자 정보 로드 (UserDetails 객체 생성)
                UserDetails userDetails = userDetailsService.loadUserByUsername(eno);

                // Spring Security 인증 객체 생성 (credentials=null: 이미 토큰으로 인증됨)
                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // 요청 상세 정보(IP, 세션 등)를 인증 객체에 추가
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContextHolder에 인증 정보 설정 → 이후 컨트롤러에서 SecurityContextHolder.getContext().getAuthentication()으로 접근 가능
                SecurityContextHolder.getContext().setAuthentication(authentication);

                logger.debug("JWT 인증 성공: " + eno + " - " + request.getRequestURI());
            } else if (StringUtils.hasText(jwt)) {
                // 토큰은 있지만 검증 실패(만료, 서명 오류 등)
                logger.warn("=== JWT 토큰 검증 실패 ===");
                logger.warn("요청 URI: " + request.getRequestURI());
                // 보안상 토큰 앞 20자만 로그에 출력
                logger.warn("토큰: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                logger.warn("=======================");
            }
        } catch (Exception ex) {
            // 예외 발생 시 로그만 기록하고 필터 체인은 계속 진행 (인증 실패로 처리)
            logger.error("=== JWT 인증 처리 중 오류 ===");
            logger.error("요청 URI: " + request.getRequestURI());
            logger.error("오류 메시지: " + ex.getMessage());
            logger.error("=======================", ex);
        }

        // 인증 성공/실패에 관계없이 다음 필터로 진행
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP 요청의 Authorization 헤더에서 JWT 토큰 추출
     *
     * <p>표준 Bearer 토큰 인증 방식: {@code Authorization: Bearer {JWT토큰}}</p>
     *
     * <p>"Bearer " 접두사(7자)를 제거하고 순수 JWT 토큰 문자열만 반환합니다.</p>
     *
     * @param request HTTP 요청 객체
     * @return JWT 토큰 문자열 (Authorization 헤더가 없거나 형식이 맞지 않으면 null)
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // Authorization 헤더 값 조회 (예: "Bearer eyJhbGciOiJIUzI1NiJ9...")
        String bearerToken = request.getHeader("Authorization");
        // "Bearer "로 시작하는 경우에만 토큰 부분(7번 인덱스부터) 추출
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer ".length() == 7
        }
        return null; // Authorization 헤더 없음 또는 Bearer 형식이 아님
    }
}
