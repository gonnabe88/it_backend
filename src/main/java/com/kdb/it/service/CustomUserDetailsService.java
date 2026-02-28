package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.repository.CuserIRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security 사용자 상세 정보 로드 서비스
 *
 * <p>{@link UserDetailsService} 인터페이스를 구현하여 Spring Security가
 * 인증 처리 시 사용자 정보를 DB에서 조회할 수 있도록 합니다.</p>
 *
 * <p>주요 사용처:</p>
 * <ul>
 *   <li>{@link com.kdb.it.security.JwtAuthenticationFilter}: JWT 토큰의 사번으로
 *       {@link UserDetails}를 로드하여 {@link org.springframework.security.core.Authentication}
 *       객체를 생성합니다.</li>
 * </ul>
 *
 * <p>Spring Security는 이 서비스를 {@link UserDetailsService} 타입의 빈으로 자동 감지하여
 * {@link org.springframework.security.authentication.dao.DaoAuthenticationProvider}에
 * 주입합니다.</p>
 */
@Service             // Spring 서비스 빈으로 등록
@RequiredArgsConstructor // final 필드 생성자 자동 주입 (Lombok)
public class CustomUserDetailsService implements UserDetailsService {

    /** 사용자 정보 조회를 위한 리포지토리 */
    private final CuserIRepository cuserIRepository;

    /**
     * 사번(username)으로 사용자 정보를 로드합니다.
     *
     * <p>Spring Security의 인증 과정에서 자동으로 호출됩니다.
     * DB에서 사번으로 {@link CuserI} 엔티티를 조회하고
     * Spring Security가 사용하는 {@link UserDetails} 형태로 변환하여 반환합니다.</p>
     *
     * <p>반환되는 {@link UserDetails}:</p>
     * <ul>
     *   <li>{@code username}: 사번 (ENO)</li>
     *   <li>{@code password}: 암호화된 비밀번호 (SHA-256 + Base64)</li>
     *   <li>{@code authorities}: 빈 권한 목록 (역할 기반 접근 제어 미구현)</li>
     * </ul>
     *
     * @param eno 로드할 사용자의 사번 (Spring Security에서 username으로 전달)
     * @return 로드된 사용자 정보 ({@link UserDetails})
     * @throws UsernameNotFoundException 해당 사번의 사용자가 DB에 없을 경우
     */
    @Override
    public UserDetails loadUserByUsername(String eno) throws UsernameNotFoundException {
        // DB에서 사번으로 사용자 조회 (없으면 UsernameNotFoundException 발생)
        CuserI user = cuserIRepository.findByEno(eno)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + eno));

        // Spring Security의 UserDetails 구현체 생성
        return User.builder()
                .username(user.getEno())          // 사용자 식별자 (사번)
                .password(user.getUsrEcyPwd())    // 암호화된 비밀번호 (PasswordEncoder로 검증)
                .authorities(Collections.emptyList()) // 권한 목록 (현재 미구현, 필요 시 역할 추가)
                .build();
    }
}
