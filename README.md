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
| 인증 | Spring Security + JWT (JJWT 0.12.3) | - | Stateless Bearer Token 방식 |
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
| **복합키** | `@IdClass` 방식으로 복합 기본키 정의 (`BcostmId`, `BitemmId`, `CdecimId`) |
| **JPA Auditing** | `BaseEntity`에서 생성/수정 일시·사용자 자동 관리 |
| **JWT 인증** | Access Token(15분) + Refresh Token(7일) 이중 토큰 전략 |
| **비밀번호** | SHA-256 + Base64 인코딩 (`CustomPasswordEncoder`) |
| **Oracle 시퀀스** | 관리번호 채번에 Native Query로 Oracle 시퀀스 사용 |
| **DTO 패턴** | 정적 중첩 클래스(Static Nested Class)로 관련 DTO 그룹화 |

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
 └── CuserI      (사용자)

독립 엔티티 (BaseEntity 미상속):
 ├── LoginHistory  (로그인 이력, ID 자동 증가)
 └── RefreshToken  (JWT Refresh Token)
```

## 4. 모듈 관계

### 4.1 패키지 구조

```
com.kdb.it
├── config/         # 설정 (Security, JPA, Jackson, QueryDSL, Swagger, PasswordEncoder)
├── controller/     # REST API 컨트롤러 (8개)
├── service/        # 비즈니스 로직 서비스 (9개)
├── repository/     # JPA 리포지토리 + QueryDSL 커스텀 (15개)
├── domain/entity/  # JPA 엔티티 (15개, 복합키 ID 클래스 포함)
├── dto/            # 데이터 전송 객체 (8개)
├── security/       # JWT 인증 필터 (1개)
├── util/           # JWT 유틸리티 (1개)
└── exception/      # 커스텀 예외 (1개)
```

### 4.2 도메인 모듈 관계

| 도메인 | Controller | Service | Repository | Entity |
|--------|-----------|---------|------------|--------|
| 정보화사업 | `ProjectController` | `ProjectService` | `ProjectRepository`, `BitemmRepository` | `Project`, `Bitemm` |
| 전산관리비 | `CostController` | `CostService` | `BcostmRepository` | `Bcostm` |
| 신청서(결재) | `ApplicationController` | `ApplicationService` | `CapplmRepository`, `CapplaRepository`, `CdecimRepository` | `Capplm`, `Cappla`, `Cdecim` |
| 인증 | `AuthController` | `AuthService` | `CuserIRepository`, `RefreshTokenRepository`, `LoginHistoryRepository` | `CuserI`, `RefreshToken`, `LoginHistory` |
| 공통코드 | `CcodemController` | `CcodemService` | `CcodemRepository` + Custom | `Ccodem` |
| 사용자 | `UserController` | `UserService` | `CuserIRepository` | `CuserI` |
| 조직 | `OrganizationController` | `OrganizationService` | `CorgnIRepository` | `CorgnI` |
| 로그인이력 | `LoginHistoryController` | `LoginHistoryService` | `LoginHistoryRepository` | `LoginHistory` |

## 5. 인증/인가 흐름

```
[로그인] POST /api/auth/login
  → 사번/비밀번호 검증 → Access Token + Refresh Token 발급
  → Refresh Token DB 저장 → 로그인 이력 기록

[API 요청] Authorization: Bearer {AccessToken}
  → JwtAuthenticationFilter → 토큰 검증
  → SecurityContext에 인증 정보 설정 → Controller 진입

[토큰 갱신] POST /api/auth/refresh
  → Refresh Token 검증 → 새 Access Token 발급

[로그아웃] POST /api/auth/logout
  → DB에서 Refresh Token 삭제 → 로그아웃 이력 기록
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
| GET | `/api/ccodem/**` | 공통코드 조회 | 필요 |
| GET | `/api/users/**` | 사용자 조회 | 필요 |
| GET | `/api/organizations` | 조직 목록 조회 | 필요 |
| GET | `/api/login-history/**` | 로그인 이력 조회 | 필요 |

> Swagger UI: `http://localhost:8080/swagger-ui/index.html`

## 7. 빌드 및 실행

```bash
# 빌드
./gradlew build

# 실행
./gradlew bootRun

# 테스트 (현재 테스트 비활성화)
./gradlew test
```

## 8. 환경 설정

- `application.properties`: DB 접속 정보, JWT 비밀키/유효시간 설정
- CORS: 개발환경 전체 허용(`*`), 운영 시 도메인 제한 필요
- CSRF: Stateless JWT 방식으로 비활성화