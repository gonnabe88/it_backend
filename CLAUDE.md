---
[ 프로젝트 메인 가이드 ]
본 파일은 IT Portal 백엔드의 개발 환경, 기술 스택, 코딩 표준을 정의합니다.
AI 어시스턴트는 코드 생성 시 이 지침을 준수하며, 모든 주석은 한글로 작성합니다.
---

## 1. 프로젝트 개요
- 명칭: IT Portal (IT 정보화 포탈)
- 주요 기능: 정보화 예산, 사업, 인력 관리
- 사용자: 약 3,000명의 사내 임직원
- 패키지 루트: `com.kdb.it`

## 2. 기술 스택 (Tech Stack)
- Framework: Spring Boot 4.0.5
- Language: Java 25
- Build: Gradle (Groovy DSL, `build.gradle`)
- ORM: Spring Data JPA + QueryDSL 5.1.0 (동적 쿼리)
- Security: Spring Security + JWT (JJWT 0.13.0)
- Database: Oracle Database (XEPDB1 / 사용자: ITPAPP)
- API 문서화: SpringDoc OpenAPI 3.0.3 (Swagger UI)
- 유틸: Lombok, Jsoup 1.18.3

## 3. 주요 명령어
- `./gradlew build`: 프로젝트 빌드
- `./gradlew bootRun`: 개발 서버 실행
- `./gradlew test`: 테스트 실행
- `./gradlew clean test`: 전체 테스트 재검증
- `./gradlew clean build`: 클린 빌드
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 4. 아키텍처 (Architecture)

### 4.1 레이어드 아키텍처
```
Controller Layer  →  Service Layer  →  Repository Layer  →  Oracle DB
(REST 엔드포인트)    (비즈니스 로직)    (JPA / QueryDSL)
```

### 4.2 디렉토리 구조
```
src/main/java/com/kdb/it/
├── config/                  - Spring Security, JPA Auditing, QueryDSL, Swagger, Web 설정
├── common/                  - 공통 도메인
│   ├── admin/               - 시스템관리 (AdminController, AdminService — ROLE_ADMIN 전용)
│   ├── approval/            - 결재 (ApplicationController, ApplicationService)
│   ├── code/                - 공통코드 (CodeController, CodeService, CodeRepository + Custom)
│   ├── iam/                 - 사용자/조직/권한 (UserController, OrganizationController, RoleRepository)
│   ├── system/              - 인증·보안 (AuthController, AuthService, JwtUtil, JwtAuthenticationFilter)
│   └── util/                - 공통 유틸 (CustomPasswordEncoder, CookieUtil, HtmlSanitizer)
├── domain/                  - 비즈니스 도메인 집합
│   ├── budget/              - 예산 관리
│   │   ├── project/         - 정보화사업 (ProjectController, ProjectService, Bprojm)
│   │   ├── cost/            - 전산업무비 (CostController, CostService, Bcostm, Btermm)
│   │   ├── document/        - 문서·검토의견 (GuideDocController, ServiceRequestDocController, ReviewCommentController)
│   │   ├── plan/            - 정보기술부문 계획 (PlanController, PlanService, Bplanm, Bproja)
│   │   ├── status/          - 예산현황 대시보드 (BudgetStatusController, BudgetStatusService)
│   │   └── work/            - 예산 작업 (BudgetWorkController, BudgetWorkService, Bbugtm)
│   ├── council/             - 정보화실무협의회 (CouncilController, 8개 서비스, 14개 엔티티)
│   ├── log/                 - 변경 로그 (BaseLogEntity, *L 로그 엔티티, ChangeLogEntityListener)
│   ├── cdp/                 - 경력개발 (빈 디렉토리)
│   ├── audit/               - 감사/이력 (빈 디렉토리)
│   └── entity/              - BaseEntity
├── exception/               - 전역 예외 핸들러 (GlobalExceptionHandler, CustomGeneralException)
└── infra/                   - 인프라 도메인
    ├── ai/                  - Gemini AI 연동 (GeminiController, GeminiService)
    └── file/                - 파일 관리 (FileController, FileService, Cfilem, FileRepository)
src/test/java/com/kdb/it/
├── common/system/controller/ - AuthControllerTest
├── common/system/service/    - AuthServiceTest
├── common/system/security/   - JwtUtilTest
├── common/util/              - CustomPasswordEncoderTest
├── budget/project/controller/ - ProjectControllerTest
└── budget/project/service/   - ProjectServiceTest
src/main/resources/
└── application.properties   - DB, JWT, CORS, 쿠키, 파일, Gemini, 로깅 설정
```

### 4.3 주요 도메인 테이블 매핑
| 엔티티         | 테이블명              | 역할              |
|--------------|---------------------|-----------------|
| Bprojm       | TAAABB_BPROJM       | 정보화사업 마스터  |
| Bitemm       | TAAABB_BITEMM       | 프로젝트 품목     |
| Bcostm       | TAAABB_BCOSTM       | 전산관리비        |
| Btermm       | TAAABB_BTERMM       | 단말기            |
| Bplanm       | TAAABB_BPLANM       | 정보기술부문 계획  |
| Bproja       | TAAABB_BPROJA       | 계획-사업 연결    |
| Bbugtm       | TAAABB_BBUGTM       | 예산 편성률       |
| Basctm       | TAAABB_BASCTM       | 협의회 심의과제   |
| Bchklc       | TAAABB_BCHKLC       | 타당성 검토항목   |
| Bcmmtm       | TAAABB_BCMMTM       | 평가위원          |
| Bevalm       | TAAABB_BEVALM       | 평가의견          |
| Bperfm       | TAAABB_BPERFM       | 성과지표          |
| Bpovwm       | TAAABB_BPOVWM       | 사업개요          |
| Bpqnam       | TAAABB_BPQNAM       | 사전질의응답      |
| Brsltm       | TAAABB_BRSLTM       | 결과서            |
| Bschdm       | TAAABB_BSCHDM       | 일정              |
| Bgdocm       | TAAABB_BGDOCM       | 가이드 문서       |
| Brdocm       | TAAABB_BRDOCM       | 요구사항 정의서   |
| Brivgm       | TAAABB_BRIVGM       | 요구사항 검토의견 |
| CuserI       | TAAABB_CUSERI       | 사용자/직원 정보  |
| CorgnI       | TAAABB_CORGNI       | 조직/부점 정보   |
| CauthI       | TAAABB_CAUTHI       | 자격등급          |
| CroleI       | TAAABB_CROLEI       | 역할 매핑         |
| Capplm       | TAAABB_CAPPLM       | 신청서 마스터     |
| Cappla       | TAAABB_CAPPLA       | 신청서-원본 연결  |
| Cdecim       | TAAABB_CDECIM       | 결재선 정보       |
| Ccodem       | TAAABB_CCODEM       | 코드 마스터       |
| Cfilem       | TAAABB_CFILEM       | 첨부파일          |
| Clognh       | TAAABB_CLOGNH       | 로그인이력        |
| Crtokm       | TAAABB_CRTOKM       | 갱신토큰          |

변경 로그는 `BaseLogEntity`를 기반으로 `BprojmL`, `BcostmL`, `BrdocmL` 등 원본 엔티티별 `*L` 로그 엔티티에 저장합니다.

## 5. 코딩 스타일 및 가이드라인

### 5.1 공통 원칙
- 모든 JavaDoc 주석 및 인라인 주석은 **한글**로 작성합니다.
- Lombok을 적극 활용합니다: `@Getter`, `@RequiredArgsConstructor`, `@SuperBuilder`, `@NoArgsConstructor`
- 생성자 주입 방식을 사용합니다 (`@RequiredArgsConstructor` + `private final` 필드).

### 5.2 엔티티 설계 원칙
- 모든 업무 엔티티는 **`BaseEntity`를 상속**합니다.
- `BaseEntity`가 제공하는 공통 필드:
  - `DEL_YN` : 논리 삭제 여부 ('N'=미삭제, 'Y'=삭제)
  - `GUID` : UUID 자동 생성
  - `FST_ENR_DTM` / `FST_ENR_USID` : 최초 등록 일시/사용자 (JPA Auditing)
  - `LST_CHG_DTM` / `LST_CHG_USID` : 최종 변경 일시/사용자 (JPA Auditing)
- 삭제는 반드시 **Soft Delete**(`delete()` 메서드 호출, DEL_YN='Y')를 사용합니다. 물리 삭제 금지.

### 5.3 DTO 설계 원칙
- 관련 DTO는 **정적 중첩 클래스(Static Nested Class)** 방식으로 하나의 파일에 묶습니다.
  - 예: `AuthDto.LoginRequest`, `AuthDto.LoginResponse`, `AuthDto.RefreshRequest`
- Swagger 문서를 위해 `@Schema(name = "...", description = "...")` 주석을 필수로 작성합니다.

### 5.4 Repository 패턴
- 기본 CRUD: `JpaRepository` 상속으로 처리합니다.
- 동적·복잡한 쿼리: `RepositoryCustom` 인터페이스 + `RepositoryImpl` 구현체 패턴 (QueryDSL) 을 사용합니다.
- Oracle 시퀀스 조회 등 DB 종속 쿼리는 `@Query(nativeQuery = true)`를 사용합니다.

### 5.5 Service 트랜잭션 원칙
- 조회 메서드: `@Transactional(readOnly = true)` 필수 적용.
- 쓰기 메서드: `@Transactional` 적용 (기본값 readOnly=false).
- 엔티티 수정은 JPA Dirty Checking을 활용하며, 불필요한 `save()` 호출을 지양합니다.

### 5.6 인증 및 보안
- 인증 방식: **httpOnly 쿠키 기반 JWT** (Stateless 세션)
  - Access Token 유효시간: 15분 (`jwt.access-token-expiration=900000`)
  - Refresh Token 유효시간: 7일 (`jwt.refresh-token-expiration=604800000`)
- 비밀번호 암호화: `CustomPasswordEncoder` (SHA-256 + Base64)
- Access Token과 Refresh Token은 `CookieUtil`로 httpOnly 쿠키에 설정합니다.
- 보호 API는 쿠키 자동 전송을 기본으로 하며, `JwtAuthenticationFilter`는 Authorization Bearer 헤더를 폴백으로만 허용합니다.
- 공개 엔드포인트 (인증 불필요): `/api/auth/login`, `/api/auth/signup`, `/api/auth/refresh`, `/swagger-ui/**`, `/v3/api-docs/**`
- 관리자 전용 엔드포인트: `/api/admin/**` — SecurityConfig URL 패턴 + `@PreAuthorize("hasRole('ADMIN')")` 이중 보호
- RBAC 모델: 자격등급(`CauthI`) + 역할 매핑(`CroleI`) 기반. `ITPAD001`=시스템관리자, `ITPZZ001`=일반사용자, `ITPZZ002`=기획통할담당자
- CORS는 `cors.allowed-origins=http://localhost:3000,http://localhost:3002` 기준으로 개발 프론트엔드를 허용합니다.
- 운영 배포에서는 `app.cookie.secure=true`와 HTTPS를 함께 적용합니다.

### 5.7 채번 규칙
- 정보화사업 관리번호: `PRJ-{사업연도}-{4자리 시퀀스}` (예: `PRJ-2026-0001`)
- 신청서 관리번호: `APF_{연도}{8자리 시퀀스}`
- 시퀀스 값은 Oracle Native Query로 조회합니다.

### 5.8 주요 비즈니스 제약
- 신청 상태가 **"결재중"** 또는 **"결재완료"** 인 경우, 연결된 프로젝트의 수정 및 삭제가 불가합니다.
- 프로젝트 수정 시 품목(Bitemm) 동기화: 요청에 포함된 품목은 추가/수정, 누락된 기존 품목은 Soft Delete.
- 요구사항 정의서 검토의견은 `Brivgm` 기준으로 문서에 종속되며, 삭제 시 물리 삭제 대신 논리 삭제 정책을 우선 검토합니다.
- 예산현황 조회는 `BudgetStatusQueryRepository`의 집계 쿼리를 기준으로 하며, 화면 요구사항 변경 시 DTO와 쿼리 조건을 함께 갱신합니다.

### 5.9 환경 설정 키
- JWT: `jwt.secret`, `jwt.access-token-expiration`, `jwt.refresh-token-expiration`
- CORS: `cors.allowed-origins`
- 쿠키: `app.cookie.secure`
- 파일: `app.file.base-path=C:/data/files`, multipart 최대 파일 50MB/요청 200MB
- Gemini: `gemini.api.key`, `gemini.api.base-url`, `gemini.api.model=gemini-2.5-flash`
- 서버 식별자: `app.server.instance-id=SVR1`

### 5.10 테스트 기준
- 기능 변경 후 최소 `./gradlew test`를 실행합니다.
- 인증, 결재, 파일, QueryDSL 집계, 변경 로그처럼 공통 영향이 있는 변경은 `./gradlew clean test`로 재검증합니다.
- 2026-04-29 기준 백엔드 테스트는 35개 테스트 파일 / 291개 케이스입니다.

## 6. API 응답 형식
- `200 OK` : 조회 및 수정 성공
- `201 Created` : 생성 성공 (Location 헤더 포함)
- `204 No Content` : 삭제 성공
- `400 Bad Request` : 비즈니스 로직 오류
- `401 Unauthorized` : 인증 실패 (토큰 없음/만료)
- `403 Forbidden` : 접근 권한 없음

## 7. 주석 작성 예시

```java
/**
 * 로그인 처리
 * 사번(eno)과 비밀번호를 검증하여 Access Token 및 Refresh Token을 발급합니다.
 *
 * [처리 순서]
 * 1. 사번으로 사용자 조회 → 미존재 시 예외 발생
 * 2. 비밀번호 SHA-256 해시 비교 → 불일치 시 실패 이력 저장 후 예외 발생
 * 3. Access Token / Refresh Token 생성
 * 4. Refresh Token DB 저장 (기존 토큰 덮어쓰기)
 * 5. 로그인 성공 이력 저장
 * 6. 컨트롤러에서 토큰을 httpOnly 쿠키로 내려보냄
 *
 * @param request   로그인 요청 DTO (eno, password)
 * @param clientIp  클라이언트 IP 주소 (이력 기록용)
 * @param userAgent 클라이언트 User-Agent (이력 기록용)
 * @return 로그인 응답 DTO (토큰은 @JsonIgnore 대상이며 쿠키로만 전달, 본문에는 사용자 정보 포함)
 * @throws CustomGeneralException 사용자 미존재 또는 비밀번호 불일치 시
 */
public AuthDto.LoginResponse login(AuthDto.LoginRequest request, String clientIp, String userAgent) {
    // 1. 사용자 조회
    CuserI user = cuserIRepository.findByEno(request.getEno())
        .orElseThrow(() -> new CustomGeneralException("존재하지 않는 사번입니다."));
    // 이하 로직 ...
}
```

```java
/**
 * 프로젝트 논리 삭제
 * DEL_YN을 'Y'로 변경하며, 결재 진행 중인 신청서가 있을 경우 삭제를 거부합니다.
 *
 * @param prjMngNo 프로젝트 관리번호 (예: PRJ-2026-0001)
 * @throws CustomGeneralException 프로젝트 미존재 또는 결재 진행 중인 경우
 */
public void deleteProject(String prjMngNo) {
    // 프로젝트 존재 여부 확인
    Bprojm project = projectRepository.findByPrjMngNoAndDelYn(prjMngNo, "N")
        .orElseThrow(() -> new CustomGeneralException("존재하지 않는 프로젝트입니다."));
    // 이하 로직 ...
}
```
