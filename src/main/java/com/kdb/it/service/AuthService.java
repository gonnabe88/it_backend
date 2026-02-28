package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.domain.entity.LoginHistory;
import com.kdb.it.domain.entity.RefreshToken;
import com.kdb.it.dto.AuthDto;
import com.kdb.it.repository.CuserIRepository;
import com.kdb.it.repository.LoginHistoryRepository;
import com.kdb.it.repository.RefreshTokenRepository;
import com.kdb.it.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 인증(Authentication) 서비스
 *
 * <p>사용자 회원가입, 로그인, 토큰 갱신, 로그아웃 비즈니스 로직을 처리합니다.</p>
 *
 * <p>인증 방식: JWT 기반 Stateless 인증</p>
 * <ul>
 *   <li>Access Token: 단기 유효 (기본 1시간), API 요청 시 Authorization 헤더에 포함</li>
 *   <li>Refresh Token: 장기 유효 (기본 7일), DB에 저장, Access Token 갱신에 사용</li>
 * </ul>
 *
 * <p>로그인 이력: 로그인 성공/실패, 로그아웃 시 {@link LoginHistory}에 자동 기록됩니다.</p>
 *
 * <p>비밀번호 처리: {@link PasswordEncoder} (SHA-256 + Base64 방식)로 암호화합니다.</p>
 */
@Service             // Spring 서비스 빈으로 등록
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class AuthService {

    /** 사용자 정보 데이터 접근 리포지토리 (TAAABB_CUSERI) */
    private final CuserIRepository cuserIRepository;

    /** Refresh Token 데이터 접근 리포지토리 (REFRESH_TOKEN) */
    private final RefreshTokenRepository refreshTokenRepository;

    /** 로그인 이력 데이터 접근 리포지토리 (LOGIN_HISTORY) */
    private final LoginHistoryRepository loginHistoryRepository;

    /** 비밀번호 암호화 및 검증 (SHA-256 + Base64) */
    private final PasswordEncoder passwordEncoder;

    /** JWT Access/Refresh Token 생성 및 검증 유틸리티 */
    private final JwtUtil jwtUtil;

    /**
     * 회원가입 (사용자 등록)
     *
     * <p>사번(eno) 중복 여부를 확인하고, 비밀번호를 암호화하여 사용자 정보를 저장합니다.</p>
     *
     * <p>Self-registration: 최초 등록자(fstEnrUsid)와 최종 수정자(lstChgUsid)를
     * 본인 사번으로 설정합니다.</p>
     *
     * @param request 회원가입 요청 DTO (사번, 이름, 비밀번호)
     * @throws RuntimeException 이미 존재하는 사번인 경우
     */
    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        // 사번 중복 확인
        if (cuserIRepository.existsByEno(request.getEno())) {
            throw new RuntimeException("이미 존재하는 사번입니다.");
        }

        // 사용자 엔티티 생성 (비밀번호는 암호화하여 저장)
        CuserI user = CuserI.builder()
                .eno(request.getEno())                            // 사번 (PK)
                .usrNm(request.getEmpNm())                        // 사용자명
                .usrEcyPwd(passwordEncoder.encode(request.getPassword())) // 암호화된 비밀번호
                .delYn("N")                                       // 삭제여부: 미삭제
                .fstEnrDtm(LocalDateTime.now())                   // 최초 등록 일시
                .lstChgDtm(LocalDateTime.now())                   // 최종 변경 일시
                .fstEnrUsid(request.getEno())                     // 최초 등록자: 본인 (Self-registration)
                .lstChgUsid(request.getEno())                     // 최종 변경자: 본인
                .build();

        cuserIRepository.save(user);
    }

    /**
     * 사용자 이름 조회
     *
     * <p>사번으로 사용자 이름을 조회합니다. 사용자가 없으면 "Unknown"을 반환합니다.</p>
     *
     * @param eno 조회할 사번
     * @return 사용자 이름 (없으면 "Unknown")
     */
    @Transactional(readOnly = true)
    public String getUserName(String eno) {
        return cuserIRepository.findByEno(eno)
                .map(CuserI::getUsrNm) // Optional에서 사용자명 추출
                .orElse("Unknown");    // 사용자가 없으면 기본값 반환
    }

    /**
     * 로그인 및 JWT 토큰 발급
     *
     * <p>사번과 비밀번호를 검증하고, 성공 시 Access Token과 Refresh Token을 발급합니다.</p>
     *
     * <p>처리 흐름:</p>
     * <ol>
     *   <li>DB에서 사번으로 사용자 조회 (없으면 로그인 실패 이력 기록 후 예외)</li>
     *   <li>비밀번호 검증 (불일치 시 로그인 실패 이력 기록 후 예외)</li>
     *   <li>Access Token 생성 (단기 유효)</li>
     *   <li>Refresh Token 생성 및 DB 저장 (기존 토큰 삭제 후 신규 저장)</li>
     *   <li>로그인 성공 이력 기록</li>
     *   <li>토큰 및 사용자 정보 반환</li>
     * </ol>
     *
     * @param eno       로그인할 사번
     * @param password  입력한 비밀번호 (평문)
     * @param ipAddress 클라이언트 IP 주소 (이력 기록용)
     * @param userAgent 클라이언트 User-Agent 문자열 (이력 기록용)
     * @return 로그인 응답 DTO (Access Token, Refresh Token, 사번, 사용자명)
     * @throws RuntimeException 사용자 미존재 또는 비밀번호 불일치 시
     */
    @Transactional
    public AuthDto.LoginResponse login(String eno, String password, String ipAddress, String userAgent) {
        try {
            // 사용자 조회 (없으면 예외)
            CuserI user = cuserIRepository.findByEno(eno)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            // 비밀번호 검증 (SHA-256 + Base64 방식)
            if (!passwordEncoder.matches(password, user.getUsrEcyPwd())) {
                // 비밀번호 불일치 시 실패 이력 기록 후 예외
                recordLoginFailure(eno, ipAddress, userAgent, "비밀번호 불일치");
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            // Access Token 생성 (단기 유효, 기본 1시간)
            String accessToken = jwtUtil.generateAccessToken(eno);

            // Refresh Token 생성 (장기 유효, 기본 7일)
            String refreshTokenValue = jwtUtil.generateRefreshToken(eno);

            // 기존 Refresh Token 삭제 후 새 토큰 저장 (1인 1토큰 정책)
            refreshTokenRepository.deleteByEno(eno);
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)                       // JWT Refresh Token 문자열
                    .eno(eno)                                       // 토큰 소유자 사번
                    .expiryDate(LocalDateTime.now().plusDays(7))    // 만료일: 7일 후
                    .createdAt(LocalDateTime.now())                 // 생성일시: 현재
                    .build();
            refreshTokenRepository.save(refreshToken);

            // 로그인 성공 이력 기록
            recordLoginSuccess(eno, ipAddress, userAgent);

            // 응답 DTO 반환
            return AuthDto.LoginResponse.builder()
                    .accessToken(accessToken)           // 발급된 Access Token
                    .refreshToken(refreshTokenValue)    // 발급된 Refresh Token
                    .eno(eno)                           // 로그인한 사번
                    .empNm(user.getUsrNm())             // 사용자명
                    .build();

        } catch (RuntimeException e) {
            // 사용자를 찾을 수 없는 경우 실패 이력 기록 (비밀번호 불일치는 위에서 이미 기록됨)
            if (e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                recordLoginFailure(eno, ipAddress, userAgent, "존재하지 않는 사번");
            }
            throw e; // 예외 재전파
        }
    }

    /**
     * Access Token 갱신
     *
     * <p>만료된 Access Token 대신 유효한 Refresh Token을 사용하여
     * 새로운 Access Token을 발급합니다.</p>
     *
     * <p>처리 흐름:</p>
     * <ol>
     *   <li>Refresh Token JWT 서명/만료 검증</li>
     *   <li>DB에서 Refresh Token 존재 여부 확인</li>
     *   <li>DB 저장 만료일 기준 만료 여부 재확인 (보안 이중 검증)</li>
     *   <li>새로운 Access Token 생성 및 반환</li>
     * </ol>
     *
     * @param refreshTokenValue 클라이언트가 제출한 Refresh Token 문자열
     * @return 토큰 갱신 응답 DTO (새로운 Access Token)
     * @throws RuntimeException Refresh Token이 유효하지 않거나 만료된 경우
     */
    @Transactional
    public AuthDto.RefreshResponse refreshAccessToken(String refreshTokenValue) {
        // JWT 서명/만료 검증 (1차 검증: JwtUtil)
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token 조회 (2차 검증: DB 존재 여부)
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token을 찾을 수 없습니다."));

        // DB 저장 만료일 기준 만료 여부 확인 (3차 검증: expiryDate 필드)
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken); // 만료된 토큰 즉시 삭제
            throw new RuntimeException("만료된 Refresh Token입니다.");
        }

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(refreshToken.getEno());

        return AuthDto.RefreshResponse.builder()
                .accessToken(newAccessToken) // 새로 발급된 Access Token
                .build();
    }

    /**
     * 로그아웃 (Refresh Token 삭제)
     *
     * <p>DB에 저장된 Refresh Token을 삭제하여 세션을 무효화합니다.
     * Access Token은 stateless이므로 서버에서 직접 무효화할 수 없으며,
     * 클라이언트가 토큰을 삭제하는 방식으로 처리합니다.</p>
     *
     * @param eno       로그아웃할 사용자의 사번
     * @param ipAddress 클라이언트 IP 주소 (이력 기록용)
     * @param userAgent 클라이언트 User-Agent 문자열 (이력 기록용)
     */
    @Transactional
    public void logout(String eno, String ipAddress, String userAgent) {
        refreshTokenRepository.deleteByEno(eno); // 해당 사번의 Refresh Token 삭제

        // 로그아웃 이력 기록
        recordLogout(eno, ipAddress, userAgent);
    }

    /**
     * 로그인 성공 이력 기록 (내부 헬퍼 메서드)
     *
     * <p>{@link LoginHistory#createLoginSuccess(String, String, String)} 팩토리 메서드를 사용하여
     * LOGIN_SUCCESS 타입의 이력을 생성하고 저장합니다.</p>
     *
     * @param eno       로그인 성공한 사번
     * @param ipAddress 접속 IP 주소
     * @param userAgent 접속 User-Agent
     */
    private void recordLoginSuccess(String eno, String ipAddress, String userAgent) {
        LoginHistory loginHistory = LoginHistory.createLoginSuccess(eno, ipAddress, userAgent);
        loginHistoryRepository.save(loginHistory);
    }

    /**
     * 로그인 실패 이력 기록 (내부 헬퍼 메서드)
     *
     * <p>{@link LoginHistory#createLoginFailure(String, String, String, String)} 팩토리 메서드를 사용하여
     * LOGIN_FAILURE 타입의 이력을 생성하고 저장합니다.</p>
     *
     * @param eno           로그인 시도한 사번
     * @param ipAddress     접속 IP 주소
     * @param userAgent     접속 User-Agent
     * @param failureReason 실패 사유 (예: "비밀번호 불일치", "존재하지 않는 사번")
     */
    private void recordLoginFailure(String eno, String ipAddress, String userAgent, String failureReason) {
        LoginHistory loginHistory = LoginHistory.createLoginFailure(eno, ipAddress, userAgent, failureReason);
        loginHistoryRepository.save(loginHistory);
    }

    /**
     * 로그아웃 이력 기록 (내부 헬퍼 메서드)
     *
     * <p>{@link LoginHistory#createLogout(String, String, String)} 팩토리 메서드를 사용하여
     * LOGOUT 타입의 이력을 생성하고 저장합니다.</p>
     *
     * @param eno       로그아웃한 사번
     * @param ipAddress 접속 IP 주소
     * @param userAgent 접속 User-Agent
     */
    private void recordLogout(String eno, String ipAddress, String userAgent) {
        LoginHistory loginHistory = LoginHistory.createLogout(eno, ipAddress, userAgent);
        loginHistoryRepository.save(loginHistory);
    }
}
