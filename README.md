# 개발 노트 (Development Notes)

## 1. 프로젝트 개요

KDB IT 관리 시스템의 백엔드 API 서버로, 정보화사업(IT 프로젝트) 및 전산관리비(IT 관리비) 관리,
결재(신청서) 처리, 사용자 인증/인가 기능을 제공합니다.

## 2. 기술 스택

| 구분 | 기술 | 버전 | 비고 |
|------|------|------|------|
| 언어 | Java | 25 | - |
| 프레임워크 | Spring Boot | 4.0.1 | Spring Framework 7.0 기반 |
| DB | Oracle Database | - | Native Query(시퀀스 채번) 사용 |
| ORM | Spring Data JPA + QueryDSL | - | 동적 쿼리용 QueryDSL 병행 |
| 인증 | Spring Security + JWT (JJWT 0.12.3) | - | httpOnly 쿠키 방식 (XSS 방어) |
| API 문서 | Springdoc OpenAPI 3.0.0 | - | Swagger UI 자동 생성 |
| 빌드 | Gradle (Kotlin DSL) | - | - |
| 유틸 | Lombok | - | 보일러플레이트 코드 제거 |

## 3. 아키텍처

### 3.1 계층 구조 (Layered Architecture)

```
Controller → Service → Repository → DB (Oracle)
    ↓            ↓          ↓
   DTO        Entity     JPA/QueryDSL
```

- **Controller**: REST API 엔드포인트 정의, 요청/응답 처리
- **Service**: 비즈니스 로직, 트랜잭션 관리 (`@Transactional`)
- **Repository**: 데이터 접근 (JPA + QueryDSL 커스텀 구현)
- **DTO**: 계층 간 데이터 전달 (정적 중첩 클래스 패턴)

### 3.2 주요 설계 결정

| 결정 | 내용 |
|------|------|
| **Soft Delete** | 물리 삭제 대신 `DEL_YN='Y'` 논리 삭제 사용 |
| **복합키** | `@IdClass` 방식으로 복합 기본키 정의 (`ProjectId`, `BcostmId`, `BitemmId`, `CdecimId`) |
| **JPA Auditing** | `BaseEntity`에서 생성/수정 일시·사용자 자동 관리 |
| **JWT 인증** | httpOnly 쿠키 기반 Access Token(30분) + Refresh Token(7일), `CookieUtil`로 관리 |
| **비밀번호** | SHA-256 + Base64 인코딩 (`CustomPasswordEncoder`) |
| **HTML 새니타이징** | `HtmlSanitizer` (Jsoup 기반)으로 서버 측 XSS 방어, 프론트엔드 DOMPurify와 이중 방어 |
| **Oracle 시퀀스** | 관리번호 채번에 Native Query로 Oracle 시퀀스 사용 |
| **DTO 패턴** | 정적 중첩 클래스(Static Nested Class)로 관련 DTO 그룹화 |
| **전역 예외 처리** | `@RestControllerAdvice` 기반 `GlobalExceptionHandler`로 표준 오류 응답 |
| **CORS** | `application.properties`의 `cors.allowed-origins` 속성으로 환경별 제어 |

### 3.3 BaseEntity 상속 구조

```
BaseEntity (추상 클래스)
 ├── Project     (정보화사업)
 ├── Bcostm      (전산관리비)
 ├── Bitemm      (품목)
 ├── Capplm      (신청서 마스터)
 ├── Cappla      (신청서-원본 관계)
 ├── Cdecim      (결재 정보)
 ├── Ccodem      (공통코드)
 ├── CorgnI      (조직)
 ├── CuserI      (사용자)
 ├── Bgdocm      (가이드 문서)
 ├── Brdocm      (요구사항 정의서)
 ├── Cfilem      (첨부파일)
 ├── CauthI      (자격등급)
 ├── CroleI      (역할 매핑)
 └── CroleIId    (역할 복합키)

독립 엔티티 (BaseEntity 미상속):
 ├── LoginHistory  (로그인 이력, ID 자동 증가)
 └── RefreshToken  (JWT Refresh Token)
```

## 4. 모듈 관계

### 4.1 패키지 구조

2026-03-27 도메인 기반 레이어드 아키텍처로 전환 완료.

```
com.kdb.it
├── config/                  # 전역 설정 (Security, JPA, Jackson, QueryDSL, Swagger) — 5개
├── exception/               # 전역 예외 핸들러 + 커스텀 예외 — 2개
├── common/
│   ├── system/              # 인증·로그인 (AuthController, AuthService, JwtUtil, JwtAuthenticationFilter)
│   ├── iam/                 # 사용자·조직·권한 (UserController, OrganizationController, UserRepository)
│   ├── approval/            # 신청서·결재 (ApplicationController, ApplicationService, ApplicationMapRepository)
│   ├── code/                # 공통 코드 (CodeController, CodeService, CodeRepository)
│   └── util/                # 공통 유틸 (CustomPasswordEncoder, CookieUtil, HtmlSanitizer)
├── domain/                  # 비즈니스 도메인 집합
│   ├── budget/              # 예산 관리 (project, cost, document)
│   |   ├── project/             # 정보화사업 (ProjectController, ProjectService, Bprojm, ProjectItemRepository)
│   |   └── cost/                # 전산업무비 (CostController, CostService, CostRepository, BudgetDocController)
│   ├── cdp/                 # 경력개발 (빈 디렉토리)
│   ├── audit/               # 감사/이력 (빈 디렉토리)
│   └── entity/              # BaseEntity
└── infra/
    ├── file/                # 파일 관리 (FileController, FileService, FileRepository, Cfilem)
    └── ai/                  # Gemini AI (GeminiController, GeminiService)
```

**의존성 규칙 (단방향)**
```
budget → common (O)   infra → common (O)
common → budget (X)   common → infra  (X)
```

### 4.2 도메인 모듈 관계

| 도메인 | Controller | Service | Repository | Entity |
|--------|-----------|---------|------------|--------|
| 정보화사업 | `ProjectController` | `ProjectService` | `ProjectRepository`, `ProjectItemRepository` | `Bprojm`, `Bitemm` |
| 전산업무비 | `CostController` | `CostService` | `CostRepository` + Custom | `Bcostm` |
| 예산문서 | `BudgetDocController` | `BudgetDocService` | `BudgetDocRepository` | `Bgdocm` |
| 프로젝트문서 | `ProjectDocController` | `ProjectDocService` | `ProjectDocRepository` | `Brdocm` |
| 신청서(결재) | `ApplicationController` | `ApplicationService` | `ApplicationRepository`, `ApplicationMapRepository`, `ApproverRepository` | `Capplm`, `Cappla`, `Cdecim` |
| 인증 | `AuthController` | `AuthService` | `UserRepository`, `RefreshTokenRepository`, `LoginHistoryRepository` | `CuserI`, `RefreshToken`, `LoginHistory` |
| 공통코드 | `CodeController` | `CodeService` | `CodeRepository` + Custom | `Ccodem` |
| 사용자 | `UserController` | `UserService` | `UserRepository` | `CuserI` |
| 조직 | `OrganizationController` | `OrganizationService` | `OrganizationRepository` | `CorgnI` |
| 첨부파일 | `FileController` | `FileService` | `FileRepository` | `Cfilem` |
| Gemini AI | `GeminiController` | `GeminiService` | `FileRepository` (파일 첨부) | - |
| 로그인이력 | `LoginHistoryController` | `LoginHistoryService` | `LoginHistoryRepository` | `LoginHistory` |

## 5. 인증/인가 흐름

```
[로그인] POST /api/auth/login
  → 사번/비밀번호 검증 → Access Token + Refresh Token 발급
  → httpOnly 쿠키(Set-Cookie)로 토큰 전달 → 로그인 이력 기록

[API 요청] httpOnly 쿠키 자동 전송 (또는 Authorization: Bearer {token} 폴백)
  → JwtAuthenticationFilter → 쿠키/헤더에서 토큰 추출 → 검증
  → JWT claims(eno, athIds, bbrC)로 CustomUserDetails 생성 (DB 재조회 없음)
  → SecurityContext에 인증 정보 설정 → Controller 진입

[토큰 갱신] POST /api/auth/refresh
  → httpOnly 쿠키의 Refresh Token 검증 → 새 Access Token 쿠키 발급

[로그아웃] POST /api/auth/logout
  → DB에서 Refresh Token 삭제 → 쿠키 만료(maxAge=0) → 로그아웃 이력 기록
```

## 6. 주요 API 엔드포인트

| Method | Path | 설명 | 인증 |
|--------|------|------|------|
| POST | `/api/auth/login` | 로그인 | 불필요 |
| POST | `/api/auth/signup` | 회원가입 | 불필요 |
| POST | `/api/auth/refresh` | 토큰 갱신 | 불필요 |
| GET/POST | `/api/projects/**` | 정보화사업 CRUD | 필요 |
| GET/POST | `/api/costs/**` | 전산관리비 CRUD | 필요 |
| GET/POST | `/api/applications/**` | 신청서(결재) 관리 | 필요 |
| GET/POST | `/api/documents/**` | 요구사항 정의서 CRUD | 필요 |
| GET/POST | `/api/guide-documents/**` | 가이드 문서 CRUD | 필요 |
| GET/POST | `/api/files/**` | 첨부파일 업로드/다운로드/미리보기 | 필요 |
| POST | `/api/gemini/generate` | Gemini AI 프록시 | 필요 |
| GET | `/api/ccodem/**` | 공통코드 조회 | 필요 |
| GET | `/api/users/**` | 사용자 조회 | 필요 |
| GET | `/api/organizations` | 조직 목록 조회 | 필요 |
| GET | `/api/login-history/**` | 로그인 이력 조회 | 필요 |

> Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 7. 빌드 및 실행

```bash
# QueryDSL 어노테이션 프로세서 실행 (필요시)
./gradlew compileJava

# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트 (45개 단위 테스트 — ProjectServiceTest, AuthServiceTest 등)
./gradlew test
```

## 8. 환경 설정

- `application.properties`: DB 접속 정보, JWT 비밀키/유효시간, CORS 도메인 설정
- CORS: `cors.allowed-origins` 속성으로 허용 도메인 관리 (기본값 `*`, 운영 시 도메인 지정)
- CSRF: Stateless JWT 방식으로 비활성화
- 쿠키: `app.cookie.secure` 속성으로 Secure 플래그 제어 (개발=false, 운영=true)
- Gemini AI: `gemini.api.key`, `gemini.api.base-url`, `gemini.api.model` 설정 필요
- 파일 업로드: `file.upload.path` 속성으로 저장 경로 설정

## 9. 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2026-03-26~27 | **도메인 기반 레이어드 아키텍처 리팩토링**: flat 패키지 → common/budget/infra 3개 도메인 분리, 33개 클래스 리네이밍(CodeController, FileRepository 등), 테스트 파일 도메인 패키지 이동, Match Rate 100% |
| 2026-03-25 | 전체 프로젝트 문서화 리프레시: 소스 코드 주석 전수 점검(81개 파일), README.md 최신화 |
| 2026-03-22 | Tiptap 에디터 관련 수정, `HtmlSanitizer` 테이블 태그 허용 확대 |
| 2026-03-14 | 요구사항 정의서 테이블 포맷 보존 수정, TOC 스크롤 기능 |
| 2026-03-09 | httpOnly 쿠키 인증 전환 (`CookieUtil`, `JwtAuthenticationFilter` 쿠키 우선 추출) |
| 2026-03-08 | `GeminiService` 파일 첨부 지원, `CfilemController` 일괄 업로드 API 추가 |
| 2026-03-04 | `GlobalExceptionHandler` 신규 추가, CORS `properties` 기반 전환 |
| 2026-03-03 | `CcodemDto` Swagger 어노테이션 추가, `CcodemRepositoryImpl` JavaDoc 보강 |
| 2026-03-02 | `Bcostm` 추진부서(`PUL_DPM`) 필드 추가, `Project` 응답에 부서명/사용자명 포함 |