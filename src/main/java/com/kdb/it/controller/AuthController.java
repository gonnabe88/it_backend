package com.kdb.it.controller;

import com.kdb.it.dto.AuthDto;
import com.kdb.it.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * 인증(Authentication) REST 컨트롤러
 *
 * <p>사용자 회원가입, 로그인, 로그아웃, JWT 토큰 갱신 기능을 담당합니다.</p>
 *
 * <p>기본 URL: {@code /api/auth}</p>
 *
 * <p>인증 불필요 공개 엔드포인트 (SecurityConfig에서 permitAll 설정):</p>
 * <ul>
 *   <li>{@code POST /api/auth/login}: 로그인</li>
 *   <li>{@code POST /api/auth/signup}: 회원가입</li>
 *   <li>{@code POST /api/auth/refresh}: 토큰 갱신</li>
 * </ul>
 *
 * <p>인증 필요 엔드포인트:</p>
 * <ul>
 *   <li>{@code POST /api/auth/logout}: 로그아웃 (JWT 토큰 필요)</li>
 * </ul>
 */
@RestController                      // REST API 컨트롤러로 등록
@RequestMapping("/api/auth")         // 기본 URL 경로 설정
@RequiredArgsConstructor             // final 필드 생성자 자동 주입 (Lombok)
@Tag(name = "Auth", description = "인증 API") // Swagger UI 그룹 태그
public class AuthController {

    /** 인증 비즈니스 로직 서비스 */
    private final AuthService authService;

    /**
     * 회원가입
     *
     * <p>새로운 사용자 계정을 생성합니다.
     * 비밀번호는 SHA-256으로 암호화하여 DB에 저장됩니다.</p>
     *
     * @param request 회원가입 요청 (사번, 이름, 비밀번호)
     * @return HTTP 200 + "회원가입 성공" 메시지
     */
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "회원가입을 합니다.")
    public ResponseEntity<String> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    /**
     * 로그인 및 JWT 토큰 발급
     *
     * <p>사번과 비밀번호로 인증 후 Access Token과 Refresh Token을 발급합니다.</p>
     *
     * <p>처리 흐름:</p>
     * <ol>
     *   <li>클라이언트 IP 주소 및 User-Agent 추출</li>
     *   <li>AuthService에서 사용자 인증 수행</li>
     *   <li>Access Token(단기) + Refresh Token(장기) 생성 및 반환</li>
     *   <li>로그인 이력 기록 (성공/실패)</li>
     * </ol>
     *
     * @param request     로그인 요청 (사번, 비밀번호)
     * @param httpRequest HTTP 요청 객체 (IP, User-Agent 추출용)
     * @return HTTP 200 + Access Token, Refresh Token, 사번, 이름
     */
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "로그인하여 Access Token과 Refresh Token을 발급받습니다.")
    public ResponseEntity<AuthDto.LoginResponse> login(
            @RequestBody AuthDto.LoginRequest request,
            HttpServletRequest httpRequest) {
        // 클라이언트의 실제 IP 주소 추출 (프록시 환경 고려)
        String ipAddress = getClientIp(httpRequest);
        // 클라이언트 브라우저/기기 정보
        String userAgent = httpRequest.getHeader("User-Agent");

        // 인증 처리 및 토큰 발급
        AuthDto.LoginResponse response = authService.login(
                request.getEno(), request.getPassword(), ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Access Token 갱신
     *
     * <p>만료된 Access Token 대신 유효한 Refresh Token을 사용하여
     * 새로운 Access Token을 발급받습니다.</p>
     *
     * <p>Refresh Token 검증 과정:</p>
     * <ol>
     *   <li>JwtUtil로 토큰 서명 검증</li>
     *   <li>DB(REFRESH_TOKEN 테이블)에서 토큰 조회</li>
     *   <li>토큰 만료 여부 확인</li>
     *   <li>새 Access Token 생성 후 반환</li>
     * </ol>
     *
     * @param request Refresh Token을 포함한 갱신 요청
     * @return HTTP 200 + 새로운 Access Token
     */
    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    public ResponseEntity<AuthDto.RefreshResponse> refresh(@RequestBody AuthDto.RefreshRequest request) {
        AuthDto.RefreshResponse response = authService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * 로그아웃
     *
     * <p>현재 로그인한 사용자의 Refresh Token을 DB에서 삭제하여 무효화합니다.
     * 이후 해당 Refresh Token으로는 Access Token 갱신이 불가능합니다.</p>
     *
     * <p>주의: Access Token 자체를 무효화하지는 않으므로,
     * Access Token이 만료될 때까지 클라이언트에서 삭제해야 합니다.</p>
     *
     * @param httpRequest HTTP 요청 객체 (IP, User-Agent 추출 및 이력 기록용)
     * @return HTTP 200 + "로그아웃 성공" 메시지
     */
    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃하여 Refresh Token을 무효화합니다.")
    public ResponseEntity<String> logout(HttpServletRequest httpRequest) {
        // SecurityContextHolder에서 현재 인증된 사용자 정보 조회
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // JWT에서 추출된 사번
            String eno = authentication.getName();
            String ipAddress = getClientIp(httpRequest);
            String userAgent = httpRequest.getHeader("User-Agent");

            // Refresh Token 삭제 및 로그아웃 이력 기록
            authService.logout(eno, ipAddress, userAgent);
        }
        return ResponseEntity.ok("로그아웃 성공");
    }

    /**
     * 클라이언트 IP 주소 추출
     *
     * <p>로드밸런서, 리버스 프록시(Nginx, Apache), CDN 등을 경유한
     * 요청에서 실제 클라이언트 IP를 추출합니다.</p>
     *
     * <p>헤더 우선순위 (앞에서부터 순서대로 확인):</p>
     * <ol>
     *   <li>{@code X-Forwarded-For}: 표준 프록시 헤더 (여러 IP가 있으면 첫 번째가 원래 IP)</li>
     *   <li>{@code Proxy-Client-IP}: 일부 프록시에서 사용</li>
     *   <li>{@code WL-Proxy-Client-IP}: WebLogic 프록시에서 사용</li>
     *   <li>{@code HTTP_CLIENT_IP}: 일부 환경에서 사용</li>
     *   <li>{@code HTTP_X_FORWARDED_FOR}: 비표준 헤더</li>
     *   <li>{@code request.getRemoteAddr()}: 직접 연결 IP (프록시 없는 경우)</li>
     * </ol>
     *
     * @param request HTTP 요청 객체
     * @return 클라이언트의 실제 IP 주소 문자열
     */
    private String getClientIp(HttpServletRequest request) {
        // X-Forwarded-For 헤더: 표준 프록시/로드밸런서 클라이언트 IP 헤더
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            // 프록시 없이 직접 연결된 경우의 IP
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
