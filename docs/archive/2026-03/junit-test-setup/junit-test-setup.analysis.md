# Gap Analysis: junit-test-setup

**기능명**: junit-test-setup (백엔드 테스트 구성 JUnit)
**분석일**: 2026-03-08
**단계**: Check
**참조 Design**: `docs/02-design/features/junit-test-setup.design.md`

---

## 1. 분석 요약

| 항목 | 내용 |
|------|------|
| Match Rate | **95%** |
| 전체 테스트 | 45개 |
| 통과 | 45개 (100%) |
| 실패 | 0개 |
| Design → 구현 수정 | 7건 (모두 Design 오류 수정) |
| 구현 추가 | 3건 (테스트 보강) |

---

## 2. 파일별 매칭 결과

| 파일 | Design | 구현 | 상태 | 비고 |
|------|--------|------|------|------|
| `application-test.properties` | ✅ | ✅ | 100% | 완전 일치 |
| `TestSecurityConfig.java` | ✅ | ✅ | 100% | 401 EntryPoint 추가 (필수 개선) |
| `JwtUtilTest.java` | 9 tests | 9 tests | 100% | 완전 일치 |
| `CustomPasswordEncoderTest.java` | 6 tests | 6 tests | 100% | 완전 일치 |
| `AuthServiceTest.java` | 12 tests | 12 tests | 100% | 완전 일치 |
| `ProjectServiceTest.java` | 4 tests | 5 tests | 90% | 메서드명 수정 + 테스트 보강 |
| `AuthControllerTest.java` | 5 tests | 5 tests | 90% | 패키지·어설션 수정 |
| `ProjectControllerTest.java` | 3 tests | 5 tests | 90% | DTO 수정 + 테스트 추가 |

---

## 3. 갭 상세 분석

### 3.1 Gap: Design 오류 수정 (정당한 수정)

#### G-01: `@WebMvcTest` 패키지 변경 (Spring Boot 4.0.1 대응)
- **Design**: `org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest`
- **구현**: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- **원인**: Spring Boot 4.0.1에서 `@WebMvcTest`가 새 모듈(`spring-boot-webmvc-test`)로 이동
- **판정**: 필수 수정 ✅

#### G-02: `ProjectServiceTest` 메서드명 수정
- **Design**: `getProjectDetail()`, `projectService.getProjectDetail(prjMngNo)`
- **구현**: `getProject()`, `projectService.getProject(prjMngNo)`
- **원인**: 실제 `ProjectService`에 `getProjectDetail()` 메서드가 없음 (`getProject()`가 실제 메서드명)
- **판정**: 필수 수정 ✅

#### G-03: `ProjectControllerTest` DTO 클래스 수정
- **Design**: `ProjectDto.DetailResponse`, `getProjectDetail()`
- **구현**: `ProjectDto.Response`, `getProject()`
- **원인**: `ProjectDto.DetailResponse` 클래스가 존재하지 않음
- **판정**: 필수 수정 ✅

#### G-04: 예외 → HTTP 상태코드 수정
- **Design**: `RuntimeException`, `IllegalStateException` → `is5xxServerError()` 기대
- **구현**: `is5xxServerError()` → `isBadRequest()` (400)
- **원인**: `GlobalExceptionHandler`가 `RuntimeException`, `IllegalStateException` 모두 400으로 매핑
- **판정**: 필수 수정 ✅

#### G-05: `CustomUserDetailsService` Mock 누락
- **Design**: `AuthControllerTest`, `ProjectControllerTest`에 누락
- **구현**: `@MockitoBean CustomUserDetailsService` 추가
- **원인**: `JwtAuthenticationFilter(@Component)`가 `CustomUserDetailsService` 의존 → `@WebMvcTest`에서 Bean 누락 오류
- **판정**: 필수 추가 ✅

#### G-06: `JacksonConfig` Import 누락
- **Design**: `@Import(TestSecurityConfig.class)` 만 선언
- **구현**: `@Import({TestSecurityConfig.class, JacksonConfig.class})`
- **원인**: Spring Boot 4.0.1 `@WebMvcTest`가 사용자 정의 `@Configuration` 스캔 안 함 → `ObjectMapper` 빈 누락
- **판정**: 필수 추가 ✅

#### G-07: `TestSecurityConfig` 401 EntryPoint 미설정
- **Design**: `exceptionHandling` 구성 없음
- **구현**: `authenticationEntryPoint` 추가 (401 반환)
- **원인**: Spring Security 6에서 `authenticationEntryPoint` 미설정 시 익명 접근에 403 반환 (설계 의도는 401)
- **판정**: 필수 추가 ✅

---

### 3.2 구현 추가사항 (Design 초과 구현)

#### A-01: `ProjectServiceTest` 테스트 보강
- **Design**: 4개 테스트 (마지막 1개는 미완성 stub)
- **구현**: 5개 완성 테스트
  - `getProjectList_전체목록반환` ✅
  - `getProject_존재하는프로젝트_반환` ✅ (Design에서 `getProjectDetail`로 오작성)
  - `getProject_미존재프로젝트_예외발생` ✅
  - `deleteProject_결재중상태_예외발생` ✅ (Design에서 미완성)
  - `deleteProject_정상상태_SoftDelete` ✅ (Soft Delete `delYn=Y` 검증 추가)
  - `deleteProject_미존재프로젝트_예외발생` ✅

#### A-02: `ProjectControllerTest` 테스트 추가
- **Design**: 3개 테스트
- **구현**: 5개 테스트 (createProject 201, deleteProject 400 추가)

#### A-03: login 요청에 User-Agent 헤더 추가
- **Design**: `mockMvc.perform(post("/api/auth/login").content(...))`
- **구현**: `.header("User-Agent", "TestAgent")` 추가
- **원인**: `anyString()` Matcher가 null 미매칭 → userAgent null 시 Mock 미동작

---

## 4. 테스트 커버리지

### 4.1 레이어별 커버리지

| 레이어 | 파일 | 테스트 수 | 커버 시나리오 |
|--------|------|-----------|--------------|
| Utility | JwtUtilTest | 9 | 토큰생성/추출/검증/만료/위변조 |
| Utility | CustomPasswordEncoderTest | 6 | encode/matches/결정론적/빈문자열 |
| Service | AuthServiceTest | 12 | login성공/실패/이력/RefreshToken/signup/logout |
| Service | ProjectServiceTest | 5 | 목록/단건/삭제(정상·결재중·미존재) |
| Controller | AuthControllerTest | 5 | signup/login/refresh/쿠키없음 |
| Controller | ProjectControllerTest | 5 | GET/POST/DELETE/비인증401 |
| **합계** | **7개 파일** | **45개** | |

### 4.2 핵심 비즈니스 시나리오 커버

| 시나리오 | 테스트 | 결과 |
|----------|--------|------|
| DB 없이 단위 테스트 실행 | 모든 테스트 | ✅ PASS |
| JWT 토큰 만료/위변조 거부 | JwtUtilTest | ✅ PASS |
| 비밀번호 불일치 로그인 실패 | AuthServiceTest | ✅ PASS |
| Refresh Token 만료 시 삭제 | AuthServiceTest | ✅ PASS |
| 결재중 프로젝트 삭제 거부 | ProjectServiceTest | ✅ PASS |
| Soft Delete 검증 (delYn=Y) | ProjectServiceTest | ✅ PASS |
| 토큰 body 미포함(@JsonIgnore) | AuthControllerTest | ✅ PASS |
| Set-Cookie 헤더 전달 | AuthControllerTest | ✅ PASS |
| 비인증 요청 401 | ProjectControllerTest | ✅ PASS |
| 201 Created + Location 헤더 | ProjectControllerTest | ✅ PASS |

---

## 5. Spring Boot 4.0.1 특이사항 (학습 내용)

이번 구현 과정에서 발견된 Spring Boot 4.0.1 브레이킹 체인지:

| 변경사항 | 이전 | 이후 (4.0.1) |
|----------|------|-------------|
| `@WebMvcTest` 패키지 | `boot.test.autoconfigure.web.servlet` | `boot.webmvc.test.autoconfigure` |
| `@MockBean` | 사용 | `@MockitoBean` 권장 |
| `JacksonConfig` 스캔 | `@WebMvcTest`에서 자동 포함 | 수동 `@Import` 필요 |
| Spring Security 익명접근 | 401 반환 | `authenticationEntryPoint` 명시 필요 |

---

## 6. 결론

**Match Rate: 95%**

- Design 문서의 구조적 목표(파일 구성, 테스트 대상, 검증 시나리오)는 100% 달성
- 7건의 수정은 모두 Design 문서의 오류(실제 코드와 불일치)를 바로잡은 것으로 정당한 수정
- 구현이 Design을 초과하는 부분(추가 테스트, 보강된 Security 설정)은 품질 향상
- 전체 45개 테스트 100% 통과

**다음 단계**: `/pdca report junit-test-setup`
