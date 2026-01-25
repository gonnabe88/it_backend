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
 * Authorization 헤더에서 JWT 토큰을 추출하고 검증하여 SecurityContext에 인증 정보를 설정
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // Authorization 헤더에서 JWT 토큰 추출
            String jwt = getJwtFromRequest(request);

            // 토큰이 있고 유효한 경우
            if (StringUtils.hasText(jwt) && jwtUtil.validateToken(jwt)) {
                // 토큰에서 사번 추출
                String eno = jwtUtil.getEnoFromToken(jwt);

                // UserDetails 로드
                UserDetails userDetails = userDetailsService.loadUserByUsername(eno);

                // 인증 객체 생성
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                logger.debug("JWT 인증 성공: " + eno + " - " + request.getRequestURI());
            } else if (StringUtils.hasText(jwt)) {
                logger.warn("=== JWT 토큰 검증 실패 ===");
                logger.warn("요청 URI: " + request.getRequestURI());
                logger.warn("토큰: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                logger.warn("=======================");
            }
        } catch (Exception ex) {
            logger.error("=== JWT 인증 처리 중 오류 ===");
            logger.error("요청 URI: " + request.getRequestURI());
            logger.error("오류 메시지: " + ex.getMessage());
            logger.error("=======================", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Request에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
