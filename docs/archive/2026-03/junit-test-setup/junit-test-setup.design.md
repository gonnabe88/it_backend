# Design: 백엔드 테스트 구성 (JUnit)

**기능명**: junit-test-setup
**작성일**: 2026-03-08
**단계**: Design
**참조 Plan**: `docs/01-plan/features/junit-test-setup.plan.md`

---

## 1. 파일 구조

```
src/test/
├── java/com/kdb/it/
│   ├── config/
│   │   └── TestSecurityConfig.java
│   ├── util/
│   │   ├── JwtUtilTest.java
│   │   └── CustomPasswordEncoderTest.java
│   ├── service/
│   │   ├── AuthServiceTest.java
│   │   └── ProjectServiceTest.java
│   └── controller/
│       ├── AuthControllerTest.java
│       └── ProjectControllerTest.java
└── resources/
    └── application-test.properties
```

---

## 2. application-test.properties

**경로**: `src/test/resources/application-test.properties`

```properties
# Oracle DataSource 자동 설정 제외 (단위 테스트용)
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,\
  org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration

# JWT 테스트용 시크릿 (256bit 이상 필수)
jwt.secret=test-secret-key-for-junit-test-minimum-256-bits-length-ok
jwt.access-token-validity=900000
jwt.refresh-token-validity=604800000

# 쿠키 Secure 설정 (테스트 환경)
app.cookie.secure=false

# CORS
cors.allowed-origins=http://localhost:3000
```

> **주의**: `@WebMvcTest`는 Web 레이어만 로드하므로 DataSource 제외 설정이 없어도 동작합니다.
> 하지만 `@SpringBootTest` 기반 통합 테스트 추가 시를 대비해 미리 선언합니다.

---

## 3. TestSecurityConfig.java

**경로**: `src/test/java/com/kdb/it/config/TestSecurityConfig.java`

```java
package com.kdb.it.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트용 Spring Security 설정
 *
 * <p>@WebMvcTest 환경에서 실제 SecurityConfig 대신 사용하여
 * JWT 필터와 복잡한 보안 설정을 우회합니다.</p>
 *
 * <p>공개 엔드포인트: /api/auth/**
 * 인증 필요: 그 외 모든 경로 (@WithMockUser로 처리)</p>
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

---

## 4. JwtUtilTest.java

**경로**: `src/test/java/com/kdb/it/util/JwtUtilTest.java`

```java
package com.kdb.it.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 단위 테스트
 *
 * <p>Spring Context 없이 직접 생성자를 호출하여 테스트합니다.
 * @Value 의존성을 직접 주입하여 Oracle DB 연결이 불필요합니다.</p>
 */
class JwtUtilTest {

    /** 테스트용 고정 시크릿 키 (256bit 이상) */
    private static final String TEST_SECRET =
            "test-secret-key-for-junit-test-minimum-256-bits-length-ok";
    private static final long ACCESS_VALIDITY_MS = 900_000L;    // 15분
    private static final long REFRESH_VALIDITY_MS = 604_800_000L; // 7일
    private static final long EXPIRED_VALIDITY_MS = 1L;          // 즉시 만료

    private JwtUtil jwtUtil;
    private JwtUtil expiredJwtUtil; // 즉시 만료 토큰 생성용

    @BeforeEach
    void setUp() {
        // Spring 없이 직접 생성자 주입
        jwtUtil = new JwtUtil(TEST_SECRET, ACCESS_VALIDITY_MS, REFRESH_VALIDITY_MS);
        expiredJwtUtil = new JwtUtil(TEST_SECRET, EXPIRED_VALIDITY_MS, EXPIRED_VALIDITY_MS);
    }

    @Test
    @DisplayName("Access Token 생성 - 사번 입력 시 유효한 토큰 반환")
    void generateAccessToken_사번입력_유효한토큰반환() {
        // given
        String eno = "12345";

        // when
        String token = jwtUtil.generateAccessToken(eno);

        // then
        assertThat(token).isNotNull().isNotEmpty();
        // JWT 형식 검증 (header.payload.signature)
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Access Token에서 사번 추출")
    void generateAccessToken_토큰에서사번추출() {
        // given
        String eno = "12345";
        String token = jwtUtil.generateAccessToken(eno);

        // when
        String extractedEno = jwtUtil.getEnoFromToken(token);

        // then
        assertThat(extractedEno).isEqualTo(eno);
    }

    @Test
    @DisplayName("Refresh Token 생성 - 사번 입력 시 유효한 토큰 반환")
    void generateRefreshToken_사번입력_유효한토큰반환() {
        // given
        String eno = "12345";

        // when
        String token = jwtUtil.generateRefreshToken(eno);

        // then
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(jwtUtil.getEnoFromToken(token)).isEqualTo(eno);
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 유효한 토큰은 true 반환")
    void validateToken_유효한토큰_true반환() {
        // given
        String token = jwtUtil.generateAccessToken("12345");

        // when & then
        assertThat(jwtUtil.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 만료된 토큰은 false 반환")
    void validateToken_만료된토큰_false반환() throws InterruptedException {
        // given
        String expiredToken = expiredJwtUtil.generateAccessToken("12345");
        Thread.sleep(10); // 만료 대기

        // when & then
        assertThat(jwtUtil.validateToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 위변조 토큰은 false 반환")
    void validateToken_위변조토큰_false반환() {
        // given
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.tampered.signature";

        // when & then
        assertThat(jwtUtil.validateToken(tamperedToken)).isFalse();
    }

    @Test
    @DisplayName("토큰 유효성 검증 - 빈 문자열은 false 반환")
    void validateToken_빈문자열_false반환() {
        assertThat(jwtUtil.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("토큰 만료 확인 - 유효한 토큰은 false 반환")
    void isTokenExpired_유효한토큰_false반환() {
        // given
        String token = jwtUtil.generateAccessToken("12345");

        // when & then
        assertThat(jwtUtil.isTokenExpired(token)).isFalse();
    }

    @Test
    @DisplayName("토큰 만료 확인 - 만료된 토큰은 true 반환")
    void isTokenExpired_만료된토큰_true반환() throws InterruptedException {
        // given
        String expiredToken = expiredJwtUtil.generateAccessToken("12345");
        Thread.sleep(10); // 만료 대기

        // when & then
        assertThat(jwtUtil.isTokenExpired(expiredToken)).isTrue();
    }

    @Test
    @DisplayName("Access Token과 Refresh Token은 서로 다른 값")
    void accessToken과RefreshToken_서로다른값() {
        // given
        String eno = "12345";

        // when
        String accessToken = jwtUtil.generateAccessToken(eno);
        String refreshToken = jwtUtil.generateRefreshToken(eno);

        // then: 유효시간이 달라 payload가 다르므로 토큰값도 다름
        assertThat(accessToken).isNotEqualTo(refreshToken);
    }
}
```

---

## 5. CustomPasswordEncoderTest.java

**경로**: `src/test/java/com/kdb/it/util/CustomPasswordEncoderTest.java`

```java
package com.kdb.it.util;

import com.kdb.it.config.CustomPasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CustomPasswordEncoder 단위 테스트
 *
 * <p>순수 Java 클래스이므로 Spring Context 없이 직접 인스턴스화합니다.</p>
 */
class CustomPasswordEncoderTest {

    private CustomPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new CustomPasswordEncoder();
    }

    @Test
    @DisplayName("encode - 평문 입력 시 SHA-256 Base64 해시 반환")
    void encode_평문입력_SHA256Base64반환() {
        // given
        String rawPassword = "password123";

        // when
        String encoded = encoder.encode(rawPassword);

        // then
        assertThat(encoded).isNotNull().isNotEmpty();
        // Base64 형식 검증 (A-Z, a-z, 0-9, +, /, = 로만 구성)
        assertThat(encoded).matches("[A-Za-z0-9+/=]+");
    }

    @Test
    @DisplayName("encode - 동일한 평문은 항상 동일한 해시 반환 (결정론적)")
    void encode_동일평문_동일해시반환() {
        // given
        String rawPassword = "myPassword";

        // when
        String first = encoder.encode(rawPassword);
        String second = encoder.encode(rawPassword);

        // then
        assertThat(first).isEqualTo(second);
    }

    @Test
    @DisplayName("encode - 서로 다른 평문은 서로 다른 해시 반환")
    void encode_다른평문_다른해시반환() {
        // given & when
        String hash1 = encoder.encode("password1");
        String hash2 = encoder.encode("password2");

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("matches - 일치하는 비밀번호는 true 반환")
    void matches_일치하는비밀번호_true반환() {
        // given
        String rawPassword = "mySecretPassword";
        String encodedPassword = encoder.encode(rawPassword);

        // when & then
        assertThat(encoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("matches - 불일치 비밀번호는 false 반환")
    void matches_불일치비밀번호_false반환() {
        // given
        String rawPassword = "correctPassword";
        String encodedPassword = encoder.encode(rawPassword);

        // when & then
        assertThat(encoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("encode - 알려진 입력값에 대해 기대하는 Base64 해시 반환")
    void encode_알려진값_기대해시검증() {
        // given: SHA-256("") + Base64 = "47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU="
        String emptyInput = "";

        // when
        String encoded = encoder.encode(emptyInput);

        // then
        assertThat(encoded).isEqualTo("47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=");
    }
}
```

---

## 6. AuthServiceTest.java

**경로**: `src/test/java/com/kdb/it/service/AuthServiceTest.java`

```java
package com.kdb.it.service;

import com.kdb.it.domain.entity.CuserI;
import com.kdb.it.domain.entity.LoginHistory;
import com.kdb.it.domain.entity.RefreshToken;
import com.kdb.it.dto.AuthDto;
import com.kdb.it.repository.CuserIRepository;
import com.kdb.it.repository.LoginHistoryRepository;
import com.kdb.it.repository.RefreshTokenRepository;
import com.kdb.it.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * AuthService 단위 테스트
 *
 * <p>Mockito를 사용하여 Repository, PasswordEncoder, JwtUtil 등
 * 모든 의존성을 Mock으로 대체합니다. Oracle DB 연결이 불필요합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private CuserIRepository cuserIRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private LoginHistoryRepository loginHistoryRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // ── 로그인 테스트 ──────────────────────────────────────────────

    @Test
    @DisplayName("login - 성공 시 LoginResponse 반환")
    void login_성공_LoginResponse반환() {
        // given
        CuserI user = CuserI.builder()
                .eno("10001").usrNm("홍길동").usrEcyPwd("encodedPwd").delYn("N").build();

        given(cuserIRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password", "encodedPwd")).willReturn(true);
        given(jwtUtil.generateAccessToken("10001")).willReturn("access-token");
        given(jwtUtil.generateRefreshToken("10001")).willReturn("refresh-token");

        // when
        AuthDto.LoginResponse response = authService.login("10001", "password", "127.0.0.1", "TestAgent");

        // then
        assertThat(response).isNotNull();
        assertThat(response.getEno()).isEqualTo("10001");
        assertThat(response.getEmpNm()).isEqualTo("홍길동");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
    }

    @Test
    @DisplayName("login - 존재하지 않는 사번 → RuntimeException 발생")
    void login_존재하지않는사번_예외발생() {
        // given
        given(cuserIRepository.findByEno("99999")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.login("99999", "pwd", "127.0.0.1", "Agent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("login - 비밀번호 불일치 → RuntimeException 발생")
    void login_비밀번호불일치_예외발생() {
        // given
        CuserI user = CuserI.builder()
                .eno("10001").usrNm("홍길동").usrEcyPwd("encodedPwd").delYn("N").build();

        given(cuserIRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongPwd", "encodedPwd")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.login("10001", "wrongPwd", "127.0.0.1", "Agent"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("비밀번호가 일치하지 않습니다");
    }

    @Test
    @DisplayName("login - 비밀번호 불일치 시 실패 이력 저장")
    void login_비밀번호불일치_실패이력저장() {
        // given
        CuserI user = CuserI.builder()
                .eno("10001").usrNm("홍길동").usrEcyPwd("encodedPwd").delYn("N").build();

        given(cuserIRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(false);

        // when
        try { authService.login("10001", "wrong", "127.0.0.1", "Agent"); } catch (Exception ignored) {}

        // then: 실패 이력 저장 호출 검증
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
    }

    @Test
    @DisplayName("login - 성공 시 기존 Refresh Token 삭제 후 새 토큰 저장")
    void login_성공_기존RefreshToken삭제후저장() {
        // given
        CuserI user = CuserI.builder()
                .eno("10001").usrNm("홍길동").usrEcyPwd("encodedPwd").delYn("N").build();

        given(cuserIRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.generateAccessToken(anyString())).willReturn("access-token");
        given(jwtUtil.generateRefreshToken(anyString())).willReturn("refresh-token");

        // when
        authService.login("10001", "password", "127.0.0.1", "Agent");

        // then: 기존 토큰 삭제 후 새 토큰 저장
        verify(refreshTokenRepository, times(1)).deleteByEno("10001");
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("login - 성공 시 로그인 성공 이력 저장")
    void login_성공_성공이력저장() {
        // given
        CuserI user = CuserI.builder()
                .eno("10001").usrNm("홍길동").usrEcyPwd("encodedPwd").delYn("N").build();

        given(cuserIRepository.findByEno("10001")).willReturn(Optional.of(user));
        given(passwordEncoder.matches(anyString(), anyString())).willReturn(true);
        given(jwtUtil.generateAccessToken(anyString())).willReturn("access");
        given(jwtUtil.generateRefreshToken(anyString())).willReturn("refresh");

        // when
        authService.login("10001", "password", "127.0.0.1", "Agent");

        // then
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
    }

    // ── 회원가입 테스트 ──────────────────────────────────────────────

    @Test
    @DisplayName("signup - 성공 시 사용자 저장")
    void signup_성공_사용자저장() {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        request.setEno("10002");
        request.setEmpNm("김테스트");
        request.setPassword("password");

        given(cuserIRepository.existsByEno("10002")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encodedPwd");

        // when
        authService.signup(request);

        // then
        verify(cuserIRepository, times(1)).save(any(CuserI.class));
    }

    @Test
    @DisplayName("signup - 중복 사번 → RuntimeException 발생")
    void signup_중복사번_예외발생() {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        request.setEno("10001");

        given(cuserIRepository.existsByEno("10001")).willReturn(true);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("이미 존재하는 사번");
    }

    // ── 토큰 갱신 테스트 ──────────────────────────────────────────────

    @Test
    @DisplayName("refreshAccessToken - 유효한 Refresh Token → 새 Access Token 반환")
    void refreshAccessToken_유효한토큰_새AccessToken반환() {
        // given
        String refreshTokenValue = "valid-refresh-token";
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue).eno("10001")
                .expiryDate(LocalDateTime.now().plusDays(7))
                .build();

        given(jwtUtil.validateToken(refreshTokenValue)).willReturn(true);
        given(refreshTokenRepository.findByToken(refreshTokenValue)).willReturn(Optional.of(refreshToken));
        given(jwtUtil.generateAccessToken("10001")).willReturn("new-access-token");

        // when
        AuthDto.RefreshResponse response = authService.refreshAccessToken(refreshTokenValue);

        // then
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("refreshAccessToken - 유효하지 않은 토큰 → RuntimeException")
    void refreshAccessToken_유효하지않은토큰_예외발생() {
        // given
        given(jwtUtil.validateToken("invalid-token")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken("invalid-token"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("유효하지 않은 Refresh Token");
    }

    @Test
    @DisplayName("refreshAccessToken - DB 만료 토큰 → 삭제 후 RuntimeException")
    void refreshAccessToken_만료된DB토큰_예외발생및삭제() {
        // given
        String tokenValue = "expired-refresh-token";
        RefreshToken expiredToken = RefreshToken.builder()
                .token(tokenValue).eno("10001")
                .expiryDate(LocalDateTime.now().minusDays(1)) // 이미 만료
                .build();

        given(jwtUtil.validateToken(tokenValue)).willReturn(true);
        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(expiredToken));

        // when & then
        assertThatThrownBy(() -> authService.refreshAccessToken(tokenValue))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("만료된 Refresh Token");

        // 만료 토큰 삭제 검증
        verify(refreshTokenRepository, times(1)).delete(expiredToken);
    }

    // ── 로그아웃 테스트 ──────────────────────────────────────────────

    @Test
    @DisplayName("logout - 성공 시 Refresh Token 삭제 및 로그아웃 이력 저장")
    void logout_성공_RefreshToken삭제및이력저장() {
        // when
        authService.logout("10001", "127.0.0.1", "Agent");

        // then
        verify(refreshTokenRepository, times(1)).deleteByEno("10001");
        verify(loginHistoryRepository, times(1)).save(any(LoginHistory.class));
    }
}
```

---

## 7. ProjectServiceTest.java

**경로**: `src/test/java/com/kdb/it/service/ProjectServiceTest.java`

```java
package com.kdb.it.service;

import com.kdb.it.domain.entity.Project;
import com.kdb.it.exception.CustomGeneralException;
import com.kdb.it.repository.BitemmRepository;
import com.kdb.it.repository.CapplaRepository;
import com.kdb.it.repository.CapplmRepository;
import com.kdb.it.repository.CorgnIRepository;
import com.kdb.it.repository.CuserIRepository;
import com.kdb.it.repository.ProjectRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * ProjectService 단위 테스트
 *
 * <p>Repository 계층을 모두 Mock으로 대체하여 Oracle DB 없이 비즈니스 로직만 검증합니다.</p>
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private CapplaRepository capplaRepository;
    @Mock private CapplmRepository capplmRepository;
    @Mock private BitemmRepository bitemmRepository;
    @Mock private CorgnIRepository corgnIRepository;
    @Mock private CuserIRepository cuserIRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    @DisplayName("getProjectList - DEL_YN=N 프로젝트 목록 반환")
    void getProjectList_전체목록반환() {
        // given
        Project project = Project.builder().prjMngNo("PRJ-2026-0001").delYn("N").build();
        given(projectRepository.findAllByDelYn("N")).willReturn(List.of(project));

        // when
        var result = projectService.getProjectList();

        // then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("getProjectDetail - 존재하는 프로젝트 조회")
    void getProjectDetail_존재하는프로젝트_반환() {
        // given
        String prjMngNo = "PRJ-2026-0001";
        Project project = Project.builder().prjMngNo(prjMngNo).delYn("N").build();
        given(projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N"))
                .willReturn(Optional.of(project));

        // when
        var result = projectService.getProjectDetail(prjMngNo);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getProjectDetail - 미존재 프로젝트 조회 시 예외 발생")
    void getProjectDetail_미존재프로젝트_예외발생() {
        // given
        given(projectRepository.findByPrjMngNoAndDelYn("INVALID", "N"))
                .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> projectService.getProjectDetail("INVALID"))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("deleteProject - 결재중 신청서 존재 시 삭제 불가")
    void deleteProject_결재중상태_예외발생() {
        // given: 프로젝트 존재
        Project project = Project.builder().prjMngNo("PRJ-2026-0001").delYn("N").build();
        given(projectRepository.findByPrjMngNoAndDelYn("PRJ-2026-0001", "N"))
                .willReturn(Optional.of(project));
        // 결재중 신청서 존재 (실제 구현 확인 후 Mock 보완 필요)

        // Note: 비즈니스 제약 상세 구현에 따라 Mock 보완 필요
        // given(capplaRepository.existsInProgressByPrjMngNo(...)).willReturn(true);
    }
}
```

> **Note**: `ProjectService.deleteProject()` 상세 구현이 확인되면 결재중 검증 Mock을 보완합니다.

---

## 8. AuthControllerTest.java

**경로**: `src/test/java/com/kdb/it/controller/AuthControllerTest.java`

> **중요 설계 포인트**:
> - 로그인 응답 body에는 `eno`, `empNm`만 포함 (`@JsonIgnore`로 토큰 제외)
> - 토큰은 `Set-Cookie` 헤더로 전달 (httpOnly 쿠키)
> - Refresh Token은 쿠키에서 읽음 (요청 body가 아님)
> - `CookieUtil`도 `@MockBean`으로 등록 필요

```java
package com.kdb.it.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdb.it.config.TestSecurityConfig;
import com.kdb.it.dto.AuthDto;
import com.kdb.it.service.AuthService;
import com.kdb.it.util.CookieUtil;
import com.kdb.it.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController @WebMvcTest
 *
 * <p>Web 레이어만 로드하여 HTTP 요청/응답 스펙을 검증합니다.
 * AuthService, CookieUtil, JwtUtil은 MockBean으로 대체합니다.</p>
 */
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("POST /api/auth/signup - 성공 시 200 반환")
    void signup_성공_200반환() throws Exception {
        // given
        AuthDto.SignupRequest request = new AuthDto.SignupRequest();
        request.setEno("10001");
        request.setEmpNm("홍길동");
        request.setPassword("password123");

        doNothing().when(authService).signup(any(AuthDto.SignupRequest.class));

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("회원가입 성공"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 성공 시 200 + Set-Cookie 반환")
    void login_성공_200및쿠키반환() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEno("10001");
        request.setPassword("password123");

        AuthDto.LoginResponse loginResponse = AuthDto.LoginResponse.builder()
                .eno("10001").empNm("홍길동")
                .accessToken("access-token").refreshToken("refresh-token")
                .build();

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "access-token").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "refresh-token").build();

        given(authService.login(anyString(), anyString(), anyString(), anyString()))
                .willReturn(loginResponse);
        given(cookieUtil.createAccessTokenCookie("access-token")).willReturn(accessCookie);
        given(cookieUtil.createRefreshTokenCookie("refresh-token")).willReturn(refreshCookie);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eno").value("10001"))
                .andExpect(jsonPath("$.empNm").value("홍길동"))
                // @JsonIgnore로 인해 accessToken/refreshToken은 응답 body에 없음
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(header().exists("Set-Cookie"));
    }

    @Test
    @DisplayName("POST /api/auth/login - 존재하지 않는 사번 → 500 반환")
    void login_존재하지않는사번_에러반환() throws Exception {
        // given
        AuthDto.LoginRequest request = new AuthDto.LoginRequest();
        request.setEno("99999");
        request.setPassword("password");

        given(authService.login(anyString(), anyString(), anyString(), anyString()))
                .willThrow(new RuntimeException("사용자를 찾을 수 없습니다."));

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - Refresh Token 쿠키 없으면 401 반환")
    void refresh_쿠키없음_401반환() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/refresh - 유효한 Refresh Token 쿠키 → 200 반환")
    void refresh_유효한쿠키_200반환() throws Exception {
        // given
        AuthDto.RefreshResponse refreshResponse = AuthDto.RefreshResponse.builder()
                .accessToken("new-access-token").build();

        ResponseCookie newAccessCookie = ResponseCookie.from("accessToken", "new-access-token").build();

        given(authService.refreshAccessToken("valid-refresh-token")).willReturn(refreshResponse);
        given(cookieUtil.createAccessTokenCookie("new-access-token")).willReturn(newAccessCookie);

        // when & then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(content().string("토큰 갱신 성공"));
    }
}
```

---

## 9. ProjectControllerTest.java

**경로**: `src/test/java/com/kdb/it/controller/ProjectControllerTest.java`

```java
package com.kdb.it.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kdb.it.config.TestSecurityConfig;
import com.kdb.it.dto.ProjectDto;
import com.kdb.it.service.ProjectService;
import com.kdb.it.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController @WebMvcTest
 *
 * <p>Spring Security 인증/인가 동작과 HTTP 응답 구조를 검증합니다.
 * 인증 필요 엔드포인트는 @WithMockUser로 처리합니다.</p>
 */
@WebMvcTest(ProjectController.class)
@Import(TestSecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("GET /api/projects - 인증된 사용자 → 200 + 목록 반환")
    @WithMockUser(username = "10001")
    void getProjects_인증된사용자_200반환() throws Exception {
        // given
        ProjectDto.Response project = ProjectDto.Response.builder()
                .prjMngNo("PRJ-2026-0001")
                .build();
        given(projectService.getProjectList()).willReturn(List.of(project));

        // when & then
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prjMngNo").value("PRJ-2026-0001"));
    }

    @Test
    @DisplayName("GET /api/projects - 비인증 요청 → 401 반환")
    void getProjects_비인증_401반환() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/projects/{prjMngNo} - 존재하는 프로젝트 → 200 반환")
    @WithMockUser(username = "10001")
    void getProjectDetail_존재하는프로젝트_200반환() throws Exception {
        // given
        ProjectDto.DetailResponse detail = ProjectDto.DetailResponse.builder()
                .prjMngNo("PRJ-2026-0001")
                .build();
        given(projectService.getProjectDetail("PRJ-2026-0001")).willReturn(detail);

        // when & then
        mockMvc.perform(get("/api/projects/PRJ-2026-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.prjMngNo").value("PRJ-2026-0001"));
    }
}
```

---

## 10. 설계 주의사항

### 10.1 `@MockitoBean` vs `@MockBean`

Spring Boot 4.0.1에서는 `@MockBean` 대신 `@MockitoBean`을 사용합니다.

```java
// Spring Boot 4.0+ (deprecated @MockBean)
@MockitoBean
private AuthService authService;
```

### 10.2 RefreshToken.isExpired() 구현 확인

`AuthServiceTest`에서 `refreshToken.isExpired()`를 호출합니다.
`RefreshToken` 엔티티에 해당 메서드가 있는지 확인 후, 없으면 테스트 코드 조정이 필요합니다.

### 10.3 ProjectDto.Response / DetailResponse 필드 확인

`ProjectControllerTest`에서 `ProjectDto.Response.builder()`를 사용합니다.
실제 DTO에 `@Builder`가 있는지 확인 후 조정하세요.

### 10.4 ProjectService 메서드명 확인

Plan 문서에서 `getProjectList()`, `getProjectDetail()` 로 작성했으나,
실제 `ProjectService` 메서드명을 확인하여 일치시켜야 합니다.

---

## 11. 구현 순서

```
1. application-test.properties 생성
2. TestSecurityConfig.java 생성
3. JwtUtilTest.java → ./gradlew test 실행 확인
4. CustomPasswordEncoderTest.java → 테스트 실행
5. AuthServiceTest.java → 테스트 실행
6. ProjectServiceTest.java → 실제 메서드명 확인 후 구현
7. AuthControllerTest.java → 테스트 실행
8. ProjectControllerTest.java → DTO 필드 확인 후 구현
```

---

## 12. 다음 단계

```
/pdca do junit-test-setup   ← 실제 코드 구현 시작
```
