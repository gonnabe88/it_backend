# Plan: 백엔드 테스트 구성 (JUnit)

**기능명**: junit-test-setup
**작성일**: 2026-03-08
**단계**: Plan
**참조 PRD**: `docs/00-pm/백엔드-테스트-구성-junit.prd.md`

---

## Executive Summary

| 항목 | 내용 |
|------|------|
| 기능 | JUnit 5 + Mockito 기반 백엔드 단위·컨트롤러 테스트 인프라 구축 |
| 시작 | 2026-03-08 |
| 완료 목표 | 2026-03-15 |
| 현재 단계 | Plan |

### Value Delivered (4-Perspective)

| 관점 | 내용 |
|------|------|
| **문제** | 테스트 파일 1개(`contextLoads`만), Oracle DB 없이 실행 불가 — 로컬/CI 환경 모두에서 테스트 불가능 |
| **솔루션** | `@ExtendWith(MockitoExtension)` 단위 테스트 + `@WebMvcTest` 컨트롤러 테스트로 DB 의존성 완전 제거 |
| **UX 효과** | 기능 변경 후 `./gradlew test`로 즉시 회귀 감지, 60초 이내 실행으로 개발 사이클 단축 |
| **핵심 가치** | Service 70%+ 커버리지 확보, 핵심 비즈니스 제약(결재중 삭제 거부 등) 자동 검증 |

---

## 1. 목표

### 1.1 구현 목표
- Oracle DB 연결 없이 실행 가능한 단위·컨트롤러 테스트 구성
- `AuthService`, `ProjectService`, `JwtUtil`, `CustomPasswordEncoder` 핵심 로직 검증
- `AuthController`, `ProjectController` HTTP 스펙(요청/응답 구조, 상태 코드) 검증
- Spring Security 인증/인가 동작 테스트

### 1.2 성공 기준
| 지표 | 목표 |
|------|------|
| Service 레이어 테스트 커버리지 | 70% 이상 |
| Controller 엔드포인트 테스트 수 | 핵심 8개 이상 |
| DB 없이 `./gradlew test` 실행 | 성공 (단위 + Controller 테스트) |
| 테스트 실행 시간 | 60초 이내 |
| 비즈니스 제약 검증 테스트 | 5개 이상 |

---

## 2. 현황 분석 (As-Is → To-Be)

### As-Is
```
src/test/java/com/kdb/it/
└── ItApplicationTests.java   ← contextLoads() 1개 (Oracle DB 필요)
```

### To-Be
```
src/test/java/com/kdb/it/
├── ItApplicationTests.java
├── config/
│   └── TestSecurityConfig.java          ← 테스트용 Security 설정
├── util/
│   ├── JwtUtilTest.java                 ← JWT 생성·검증·만료 단위 테스트
│   └── CustomPasswordEncoderTest.java   ← SHA-256 암호화 단위 테스트
├── service/
│   ├── AuthServiceTest.java             ← 로그인/회원가입/토큰갱신 단위 테스트
│   └── ProjectServiceTest.java          ← 프로젝트 CRUD + 비즈니스 제약 단위 테스트
└── controller/
    ├── AuthControllerTest.java          ← @WebMvcTest 인증 API 테스트
    └── ProjectControllerTest.java       ← @WebMvcTest 프로젝트 API 테스트

src/test/resources/
└── application-test.properties          ← 테스트 전용 설정 (DB 없이 실행)
```

---

## 3. 테스트 전략

### 3.1 테스트 계층별 전략

#### 단위 테스트 (Unit Test)
- **어노테이션**: `@ExtendWith(MockitoExtension.class)`
- **Spring Context**: 로드하지 않음 (최대 속도)
- **DB 의존성**: 없음 (Repository → `@Mock`)
- **대상**: `AuthService`, `ProjectService`, `JwtUtil`, `CustomPasswordEncoder`

```java
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock  CuserIRepository cuserIRepository;
    @Mock  RefreshTokenRepository refreshTokenRepository;
    @Mock  LoginHistoryRepository loginHistoryRepository;
    @Mock  PasswordEncoder passwordEncoder;
    @Mock  JwtUtil jwtUtil;

    @InjectMocks AuthService authService;
}
```

#### 컨트롤러 테스트 (Controller Test)
- **어노테이션**: `@WebMvcTest(XxxController.class)`
- **Spring Context**: Web 레이어만 로드 (Service → `@MockBean`)
- **DB 의존성**: 없음
- **도구**: MockMvc + Jackson ObjectMapper

```java
@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)   // Security 설정 오버라이드
class AuthControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean  AuthService authService;
    @MockBean  JwtUtil jwtUtil;
}
```

### 3.2 JwtUtil 단위 테스트 전략

`JwtUtil`은 `@Component`이지만 생성자에서 `@Value`를 통해 주입받으므로,
테스트에서는 직접 생성자를 호출하여 테스트용 고정 값을 주입합니다.

```java
// 직접 생성자 주입 (Spring Context 불필요)
private static final String TEST_SECRET =
    "test-secret-key-for-junit-test-minimum-256-bits-length-ok";
private static final long ACCESS_VALIDITY = 900_000L;    // 15분
private static final long REFRESH_VALIDITY = 604_800_000L; // 7일

JwtUtil jwtUtil = new JwtUtil(TEST_SECRET, ACCESS_VALIDITY, REFRESH_VALIDITY);
```

### 3.3 CustomPasswordEncoder 단위 테스트 전략

순수 Java 클래스 (`implements PasswordEncoder`) → Spring 없이 직접 인스턴스화:

```java
CustomPasswordEncoder encoder = new CustomPasswordEncoder();
```

### 3.4 application-test.properties 전략

Spring Context 로드 시 Oracle DB 연결을 시도하지 않도록 설정:

```properties
# DB 자동 설정 비활성화 (단위 테스트 전용)
spring.autoconfigure.exclude=\
  org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,\
  org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,\
  org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration

# JWT 테스트용 시크릿 (256bit 이상)
jwt.secret=test-secret-key-for-junit-test-minimum-256-bits-length-ok
jwt.access-token-validity=900000
jwt.refresh-token-validity=604800000
```

---

## 4. 테스트 케이스 목록

### 4.1 JwtUtilTest

| # | 메서드명 | 검증 내용 |
|---|---------|----------|
| 1 | `generateAccessToken_사번입력_유효한토큰반환` | 반환 토큰이 null이 아니고 non-empty |
| 2 | `generateAccessToken_토큰에서사번추출` | `getEnoFromToken()` 결과 == 입력 사번 |
| 3 | `generateRefreshToken_사번입력_유효한토큰반환` | Refresh Token 생성 및 검증 |
| 4 | `validateToken_유효한토큰_true반환` | `validateToken()` == true |
| 5 | `validateToken_만료된토큰_false반환` | 유효시간 0ms 토큰 → false |
| 6 | `validateToken_위변조토큰_false반환` | 임의 문자열 토큰 → false |
| 7 | `validateToken_빈문자열_false반환` | "" 입력 → false |
| 8 | `isTokenExpired_유효한토큰_false반환` | 아직 만료되지 않음 |
| 9 | `isTokenExpired_만료된토큰_true반환` | 유효시간 0ms 토큰 → true |

### 4.2 CustomPasswordEncoderTest

| # | 메서드명 | 검증 내용 |
|---|---------|----------|
| 1 | `encode_평문입력_SHA256Base64반환` | 결과가 null이 아니고 non-empty |
| 2 | `encode_동일평문_동일해시반환` | 결정적(Deterministic) 해시 검증 |
| 3 | `encode_다른평문_다른해시반환` | "password1" ≠ "password2" 해시 |
| 4 | `matches_일치하는비밀번호_true반환` | raw → encode → matches == true |
| 5 | `matches_불일치비밀번호_false반환` | 다른 비밀번호 → false |
| 6 | `encode_알려진값_기대해시검증` | SHA-256("test") Base64 결과 상수 검증 |

### 4.3 AuthServiceTest

| # | 메서드명 | 검증 내용 |
|---|---------|----------|
| 1 | `login_성공_액세스토큰반환` | 사용자 존재 + 비밀번호 일치 → LoginResponse 반환 |
| 2 | `login_존재하지않는사번_예외발생` | findByEno empty → RuntimeException |
| 3 | `login_비밀번호불일치_예외발생` | passwordEncoder.matches false → RuntimeException |
| 4 | `login_비밀번호불일치_실패이력저장` | loginHistoryRepository.save() 호출 검증 |
| 5 | `login_성공_기존RefreshToken삭제후저장` | deleteByEno() 호출 후 save() 호출 검증 |
| 6 | `login_성공_성공이력저장` | LOGIN_SUCCESS 이력 저장 검증 |
| 7 | `signup_성공_사용자저장` | existsByEno false → save() 호출 |
| 8 | `signup_중복사번_예외발생` | existsByEno true → RuntimeException |
| 9 | `refreshAccessToken_유효한토큰_새AccessToken반환` | validateToken true, DB 존재, 미만료 → 새 토큰 |
| 10 | `refreshAccessToken_유효하지않은토큰_예외발생` | validateToken false → RuntimeException |
| 11 | `refreshAccessToken_만료된DB토큰_예외발생및삭제` | isExpired true → delete() + RuntimeException |
| 12 | `logout_성공_RefreshToken삭제` | deleteByEno() + LOGOUT 이력 저장 |

### 4.4 ProjectServiceTest

| # | 메서드명 | 검증 내용 |
|---|---------|----------|
| 1 | `getProjects_전체목록반환` | findAllByDelYn("N") 호출, 결과 반환 |
| 2 | `getProjectDetail_존재하는프로젝트_반환` | findByPrjMngNoAndDelYn 성공 |
| 3 | `getProjectDetail_미존재프로젝트_예외발생` | Optional.empty() → 예외 |
| 4 | `deleteProject_결재중상태_예외발생` | 결재중 신청서 존재 → CustomGeneralException |
| 5 | `deleteProject_결재완료상태_예외발생` | 결재완료 신청서 존재 → CustomGeneralException |
| 6 | `deleteProject_정상상태_SoftDelete` | delYn == "Y" 설정 검증 |

### 4.5 AuthControllerTest (@WebMvcTest)

| # | 메서드명 | HTTP | 검증 내용 |
|---|---------|------|----------|
| 1 | `login_성공_200반환` | POST /api/auth/login | 200, accessToken 포함 응답 |
| 2 | `login_잘못된요청_400반환` | POST /api/auth/login | 필수 필드 누락 → 400 |
| 3 | `signup_성공_201반환` | POST /api/auth/signup | 201 반환 |
| 4 | `refresh_성공_200반환` | POST /api/auth/refresh | 200, 새 accessToken |
| 5 | `refresh_유효하지않은토큰_400반환` | POST /api/auth/refresh | 400 반환 |

### 4.6 ProjectControllerTest (@WebMvcTest)

| # | 메서드명 | HTTP | 검증 내용 |
|---|---------|------|----------|
| 1 | `getProjects_인증된사용자_200반환` | GET /api/projects | @WithMockUser, 200 |
| 2 | `getProjects_비인증_401반환` | GET /api/projects | 인증 없음 → 401 |
| 3 | `getProjectDetail_존재하는프로젝트_200반환` | GET /api/projects/{id} | 200, 프로젝트 정보 |
| 4 | `createProject_성공_201반환` | POST /api/projects | 201, Location 헤더 포함 |
| 5 | `deleteProject_결재중_400반환` | DELETE /api/projects/{id} | 결재중 → 400 |

---

## 5. 테스트 인프라 구성

### 5.1 TestSecurityConfig.java

`@WebMvcTest` 사용 시 Spring Security가 자동 적용되어
모든 요청에 인증을 요구합니다. 테스트용 Security 설정으로 오버라이드합니다.

```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
```

### 5.2 application-test.properties 위치

```
src/test/resources/application-test.properties
```

테스트 실행 시 `@ActiveProfiles("test")` 또는
`@SpringBootTest(properties = "spring.profiles.active=test")`로 활성화.

### 5.3 build.gradle 추가 의존성

현재 `build.gradle`의 테스트 의존성이 이미 충분합니다:
- `spring-boot-starter-webmvc-test` (MockMvc 포함)
- `spring-boot-starter-security-test` (`@WithMockUser` 포함)
- `junit-platform-launcher` (JUnit 5 런처)

**추가 불필요** — 기존 의존성으로 구현 가능.

---

## 6. 구현 순서 (Implementation Order)

```
[1단계] 테스트 인프라
  └── application-test.properties
  └── TestSecurityConfig.java

[2단계] 유틸 단위 테스트 (의존성 없음, 가장 빠른 검증)
  └── CustomPasswordEncoderTest.java
  └── JwtUtilTest.java

[3단계] 서비스 단위 테스트 (Mockito)
  └── AuthServiceTest.java          ← 우선순위 높음
  └── ProjectServiceTest.java

[4단계] 컨트롤러 테스트 (@WebMvcTest)
  └── AuthControllerTest.java       ← 우선순위 높음
  └── ProjectControllerTest.java
```

---

## 7. 제약 및 리스크

| 항목 | 내용 | 완화 방법 |
|------|------|----------|
| Oracle 전용 Native Query | `@Query(nativeQuery=true)`는 H2로 대체 불가 | Repository 전체 Mock → 단위 테스트에서 제외 |
| JwtUtil `@Value` 의존성 | Spring 없이 직접 생성자 주입 필요 | 테스트용 고정 시크릿 키 사용 |
| Spring Boot 4.0.1 + Java 25 | 최신 버전 → 일부 테스트 어노테이션 동작 확인 필요 | 빌드 후 즉시 확인 |
| QueryDSL Q클래스 | APT 생성 클래스 → 컴파일 전 없음 | `./gradlew compileJava` 먼저 실행 |
| `@WebMvcTest` + Security | 기본 설정에서 모든 요청에 인증 필요 | `TestSecurityConfig`로 공개 엔드포인트 허용 |

---

## 8. 관련 파일 매핑

| 테스트 파일 | 대상 소스 파일 |
|------------|--------------|
| `JwtUtilTest.java` | `src/main/java/.../util/JwtUtil.java` |
| `CustomPasswordEncoderTest.java` | `src/main/java/.../config/CustomPasswordEncoder.java` |
| `AuthServiceTest.java` | `src/main/java/.../service/AuthService.java` |
| `ProjectServiceTest.java` | `src/main/java/.../service/ProjectService.java` |
| `AuthControllerTest.java` | `src/main/java/.../controller/AuthController.java` |
| `ProjectControllerTest.java` | `src/main/java/.../controller/ProjectController.java` |

---

## 9. 다음 단계

```
/pdca design junit-test-setup   ← 각 테스트 클래스 상세 코드 설계
```
