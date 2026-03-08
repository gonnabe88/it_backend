# PRD: 백엔드 테스트 구성 (JUnit)

**작성일**: 2026-03-08
**작성자**: PM Agent Team (AI-Assisted)
**프로젝트**: IT Portal System 백엔드
**기능명**: junit-test-setup

---

## 1. Executive Summary

| 항목 | 내용 |
|------|------|
| 기능 | JUnit 5 기반 백엔드 테스트 인프라 구축 |
| 목표 | 핵심 비즈니스 로직(Service, Controller)에 대한 자동화 테스트 커버리지 확보 |
| 기간 | 2026-03-08 ~ 2026-03-15 (1주) |

### Value Delivered (4-Perspective)

| 관점 | 내용 |
|------|------|
| 문제 | 현재 테스트가 `contextLoads()` 1개뿐 — 버그 회귀 감지 불가, 코드 변경 시 안전망 없음 |
| 솔루션 | JUnit 5 + Mockito 단위 테스트(Service) + @WebMvcTest 컨트롤러 테스트 + Spring REST Docs API 문서화 |
| UX 효과 | 개발자가 기능 변경 시 즉시 회귀 감지 → 신뢰도 높은 배포, PR 코드 리뷰 품질 향상 |
| 핵심 가치 | Oracle DB 없이 실행 가능한 단위·통합 테스트 → CI/CD 파이프라인 연동 가능 |

---

## 2. Context & Problem

### 2.1 현황 분석 (As-Is)

```
src/test/java/com/kdb/it/
└── ItApplicationTests.java   ← contextLoads() 1개만 존재
```

**문제점**:
- Service 비즈니스 로직(로그인, 토큰 발급, 프로젝트 CRUD) 검증 없음
- Controller 엔드포인트 동작 검증 없음
- JWT 유틸리티 토큰 생성/검증 테스트 없음
- 비밀번호 암호화(SHA-256) 로직 검증 없음
- Oracle DB 연결 없이 contextLoads 자체가 실패하는 구조

### 2.2 영향 범위

| 레이어 | 파일 수 | 테스트 현황 |
|--------|---------|------------|
| Controller | 8개 | 0% |
| Service | 8개 | 0% |
| Repository | 8개 | 0% |
| Util/Security | 3개 | 0% |

---

## 3. Personas

### 3.1 백엔드 개발자 (김개발, 3년차)
- **목표**: 기능 변경 후 기존 동작이 깨지지 않았는지 빠르게 확인
- **불편함**: DB 없이 테스트 실행 불가 → 로컬에서 Oracle 연결 필수 → CI 환경 구성 어려움
- **필요**: Mockito로 DB 의존성 없이 빠른 단위 테스트

### 3.2 QA 엔지니어 (이QA, 5년차)
- **목표**: API 스펙 문서화 및 회귀 테스트 자동화
- **불편함**: Swagger만 있고 테스트 케이스 없음
- **필요**: Spring REST Docs와 연동된 API 문서화 테스트

### 3.3 DevOps 엔지니어 (박운영, 4년차)
- **목표**: CI/CD 파이프라인에서 테스트 자동 실행
- **불편함**: Oracle DB 연결 없이 테스트 실행 불가
- **필요**: DB 독립적인 테스트 구성

---

## 4. Opportunity Solution Tree

### 기회 (Opportunity)
> "코드 변경 시 안전하게 배포할 수 있는 자동화된 검증 체계 구축"

### 솔루션 옵션 비교

| 옵션 | 설명 | 장점 | 단점 | 권장 |
|------|------|------|------|------|
| A. 단위 테스트만 | Mockito 기반 Service 테스트 | 빠름, DB 불필요 | Controller 검증 없음 | 부분 |
| B. @WebMvcTest + Mockito | Controller + Service 레이어 테스트 | DB 불필요, HTTP 스펙 검증 | Repository 미검증 | **권장** |
| C. @SpringBootTest + H2 | 전체 통합 테스트 | 완전한 검증 | Oracle→H2 방언 차이, 느림 | 조건부 |
| D. Testcontainers + Oracle | 실제 Oracle 컨테이너 | 실제 환경 동일 | CI 환경 구성 복잡 | 장기 과제 |

**결정: 옵션 B (단위 + @WebMvcTest) → 옵션 D (Testcontainers) 장기 로드맵**

---

## 5. 기능 범위 (Scope)

### 5.1 In-Scope (이번 개발)

#### Phase 1: 단위 테스트 (Service + Util)
| 테스트 클래스 | 대상 | 핵심 시나리오 |
|--------------|------|--------------|
| `AuthServiceTest` | `AuthService` | 로그인 성공/실패, 토큰 갱신, 로그아웃 |
| `ProjectServiceTest` | `ProjectService` | 프로젝트 CRUD, 결재중 삭제 거부 |
| `JwtUtilTest` | `JwtUtil` | 토큰 생성, 검증, 만료 |
| `CustomPasswordEncoderTest` | `CustomPasswordEncoder` | SHA-256 암호화, 검증 |

#### Phase 2: 컨트롤러 테스트 (@WebMvcTest)
| 테스트 클래스 | 대상 | 핵심 시나리오 |
|--------------|------|--------------|
| `AuthControllerTest` | `AuthController` | POST /api/auth/login, /signup, /refresh |
| `ProjectControllerTest` | `ProjectController` | GET/POST/PUT/DELETE /api/projects |

#### Phase 3: 테스트 인프라
- `TestConfig.java` — 테스트용 SecurityConfig 오버라이드
- `application-test.properties` — 테스트 전용 설정 (DB 없이 실행)

### 5.2 Out-of-Scope (장기 과제)
- Testcontainers Oracle 통합 테스트
- @DataJpaTest Repository 테스트
- E2E 테스트 (Playwright/Cypress)
- 성능 테스트 (JMeter)

---

## 6. 기술 설계 방향

### 6.1 테스트 계층 구조

```
테스트 종류          어노테이션              DB 필요   속도
─────────────────────────────────────────────────────────
Service 단위 테스트  @ExtendWith(Mockito)    없음      매우 빠름
Controller 테스트    @WebMvcTest             없음      빠름
통합 테스트          @SpringBootTest         필요      느림 (향후)
```

### 6.2 핵심 의존성 (이미 build.gradle에 있음)

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-webmvc-test'  // @WebMvcTest + MockMvc
testImplementation 'org.springframework.boot:spring-boot-starter-security-test' // @WithMockUser
testImplementation 'org.springframework.restdocs:spring-restdocs-mockmvc'        // REST Docs
```

> JUnit 5 (`junit-jupiter`) 및 Mockito는 `spring-boot-starter-test`에 포함됨

### 6.3 Oracle DB 의존성 제거 전략

- `application-test.properties`에서 JPA `ddl-auto=none`, Datasource를 H2 또는 Mock으로 설정
- `@ExtendWith(MockitoExtension.class)` 사용 시 Spring Context 불필요 → DB 연결 없음
- `@WebMvcTest` + `@MockBean` 으로 Repository 계층 Mock 처리

### 6.4 JWT 테스트 전략

```java
// 고정 시크릿 키로 테스트 토큰 생성
@Value("${jwt.secret:test-secret-key-for-unit-test-minimum-256-bits}")
```

---

## 7. 비즈니스 제약 반영 테스트 케이스

| 비즈니스 규칙 | 테스트 케이스 |
|--------------|--------------|
| 결재중/완료 프로젝트 삭제 불가 | `deleteProject_결재중상태_예외발생()` |
| Refresh Token 1인 1토큰 정책 | `login_기존토큰삭제_새토큰저장()` |
| Soft Delete (DEL_YN='Y') | `deleteProject_DEL_YN_Y로변경()` |
| 비밀번호 SHA-256 암호화 | `encode_평문_SHA256해시반환()` |
| 로그인 실패 이력 기록 | `login_비밀번호불일치_실패이력저장()` |

---

## 8. 성공 지표 (KPI)

| 지표 | 목표 |
|------|------|
| Service 레이어 테스트 커버리지 | 70% 이상 |
| Controller 엔드포인트 테스트 | 핵심 6개 이상 |
| DB 없이 테스트 실행 성공 | 100% (단위 + Controller 테스트) |
| `./gradlew test` 소요 시간 | 60초 이내 |
| 빌드 실패 시 테스트로 감지 비율 | 기존 대비 개선 |

---

## 9. 구현 우선순위 (MoSCoW)

### Must Have
- [ ] AuthServiceTest (로그인/회원가입/토큰갱신)
- [ ] JwtUtilTest (토큰 생성·검증·만료)
- [ ] CustomPasswordEncoderTest
- [ ] AuthControllerTest (@WebMvcTest)

### Should Have
- [ ] ProjectServiceTest (CRUD + 비즈니스 제약)
- [ ] ProjectControllerTest (@WebMvcTest)
- [ ] application-test.properties 구성

### Could Have
- [ ] ApplicationServiceTest
- [ ] Spring REST Docs API 문서 생성 테스트

### Won't Have (이번 범위 외)
- [ ] Testcontainers Oracle 연동
- [ ] @DataJpaTest Repository 테스트

---

## 10. 리스크

| 리스크 | 가능성 | 영향 | 완화 방법 |
|--------|--------|------|----------|
| Oracle 방언 특수 쿼리(@Query native) Mock 어려움 | 중 | 중 | Repository 전체 Mock, 단위 테스트에서 제외 |
| Spring Boot 4.0.1 + Java 25 호환성 이슈 | 낮 | 높 | 실제 빌드 후 확인 필요 |
| JWT 시크릿 키 길이 부족 (테스트 환경) | 중 | 중 | 테스트용 256bit 이상 고정 키 설정 |
| QueryDSL APT 생성 Q클래스 테스트 환경 | 낮 | 중 | 단위 테스트에서 Repository Mock 처리 |

---

## 11. 다음 단계

```
/pdca plan junit-test-setup     ← Plan 문서 작성 (기술 설계 확정)
/pdca design junit-test-setup   ← 테스트 클래스별 상세 설계
/pdca do junit-test-setup       ← 테스트 코드 구현
/pdca analyze junit-test-setup  ← 커버리지 갭 분석
```
