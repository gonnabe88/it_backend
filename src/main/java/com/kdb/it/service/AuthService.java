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

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CuserIRepository cuserIRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(AuthDto.SignupRequest request) {
        if (cuserIRepository.existsByEno(request.getEno())) {
            throw new RuntimeException("이미 존재하는 사번입니다.");
        }

        CuserI user = CuserI.builder()
                .eno(request.getEno())
                .usrNm(request.getEmpNm())
                .usrEcyPwd(passwordEncoder.encode(request.getPassword()))
                .delYn("N")
                .fstEnrDtm(LocalDateTime.now())
                .lstChgDtm(LocalDateTime.now())
                .fstEnrUsid(request.getEno()) // Self-registration
                .lstChgUsid(request.getEno())
                .build();

        cuserIRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getUserName(String eno) {
        return cuserIRepository.findByEno(eno)
                .map(CuserI::getUsrNm)
                .orElse("Unknown");
    }

    /**
     * 로그인 및 토큰 발급
     */
    @Transactional
    public AuthDto.LoginResponse login(String eno, String password, String ipAddress, String userAgent) {
        try {
            // 사용자 조회 및 비밀번호 검증
            CuserI user = cuserIRepository.findByEno(eno)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(password, user.getUsrEcyPwd())) {
                // 로그인 실패 이력 기록
                recordLoginFailure(eno, ipAddress, userAgent, "비밀번호 불일치");
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }

            // Access Token 생성
            String accessToken = jwtUtil.generateAccessToken(eno);

            // Refresh Token 생성
            String refreshTokenValue = jwtUtil.generateRefreshToken(eno);

            // 기존 Refresh Token 삭제 후 새로 저장
            refreshTokenRepository.deleteByEno(eno);
            RefreshToken refreshToken = RefreshToken.builder()
                    .token(refreshTokenValue)
                    .eno(eno)
                    .expiryDate(LocalDateTime.now().plusDays(7)) // 7일
                    .createdAt(LocalDateTime.now())
                    .build();
            refreshTokenRepository.save(refreshToken);

            // 로그인 성공 이력 기록
            recordLoginSuccess(eno, ipAddress, userAgent);

            return AuthDto.LoginResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshTokenValue)
                    .eno(eno)
                    .empNm(user.getUsrNm())
                    .build();
        } catch (RuntimeException e) {
            // 사용자를 찾을 수 없는 경우 실패 이력 기록
            if (e.getMessage().contains("사용자를 찾을 수 없습니다")) {
                recordLoginFailure(eno, ipAddress, userAgent, "존재하지 않는 사번");
            }
            throw e;
        }
    }

    /**
     * Access Token 갱신
     */
    @Transactional
    public AuthDto.RefreshResponse refreshAccessToken(String refreshTokenValue) {
        // Refresh Token 검증
        if (!jwtUtil.validateToken(refreshTokenValue)) {
            throw new RuntimeException("유효하지 않은 Refresh Token입니다.");
        }

        // DB에서 Refresh Token 조회
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh Token을 찾을 수 없습니다."));

        // 만료 여부 확인
        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("만료된 Refresh Token입니다.");
        }

        // 새로운 Access Token 생성
        String newAccessToken = jwtUtil.generateAccessToken(refreshToken.getEno());

        return AuthDto.RefreshResponse.builder()
                .accessToken(newAccessToken)
                .build();
    }

    /**
     * 로그아웃 (Refresh Token 삭제)
     */
    @Transactional
    public void logout(String eno, String ipAddress, String userAgent) {
        refreshTokenRepository.deleteByEno(eno);
        
        // 로그아웃 이력 기록
        recordLogout(eno, ipAddress, userAgent);
    }

    /**
     * 로그인 성공 이력 기록
     */
    private void recordLoginSuccess(String eno, String ipAddress, String userAgent) {
        LoginHistory loginHistory = LoginHistory.createLoginSuccess(eno, ipAddress, userAgent);
        loginHistoryRepository.save(loginHistory);
    }

    /**
     * 로그인 실패 이력 기록
     */
    private void recordLoginFailure(String eno, String ipAddress, String userAgent, String failureReason) {
        LoginHistory loginHistory = LoginHistory.createLoginFailure(eno, ipAddress, userAgent, failureReason);
        loginHistoryRepository.save(loginHistory);
    }

    /**
     * 로그아웃 이력 기록
     */
    private void recordLogout(String eno, String ipAddress, String userAgent) {
        LoginHistory loginHistory = LoginHistory.createLogout(eno, ipAddress, userAgent);
        loginHistoryRepository.save(loginHistory);
    }
}
