# 프로젝트 백로그 (Project Backlog)

> 마지막 갱신일: 2026-04-10

## 코드 품질

*(현재 해결이 필요한 코드 품질 이슈 없음)*

## 완료된 항목

- [x] Rich Text XSS **서버 측 HTML 새니타이징** 추가 (2026-03-04)
  - 파일: `util/HtmlSanitizer.java` (신규), `service/ProjectService.java`, `build.gradle`
  - jsoup 기반 이중 방어: `prjDes`(사업설명), `prjRng`(사업범위) 필드 대상

- [x] `ApplicationService.updateApprovalLineInDetail()`: `e.printStackTrace()` → **SLF4J Logger** 교체 (2026-03-04)
  - 파일: `service/ApplicationService.java`
  - SLF4J `log.warn()`으로 전환, Logger 필드 추가

- [x] `SecurityConfig`: CORS 설정을 `allowedOrigins("*")`에서 **properties 기반으로 변경** (2026-03-04)
  - 파일: `config/SecurityConfig.java`, `resources/application.properties`
  - `cors.allowed-origins` 속성으로 환경별 도메인 제한 가능

- [x] `JwtUtil.validateToken()`: `System.err.println` → **SLF4J Logger** 교체 (2026-03-04)
  - 파일: `util/JwtUtil.java`
  - `SecurityConfig`의 `System.out.println`도 함께 SLF4J로 교체

- [x] 전역 예외 핸들러 `@ControllerAdvice` 구현하여 **표준화된 오류 응답** 제공 (2026-03-04)
  - 파일: `exception/GlobalExceptionHandler.java` (신규)
  - 응답 형식: `{ timestamp, status, message }`

- [x] `LoginHistoryService.getLoginHistory()`: 메모리 기반 50건 제한 → **DB 레벨 페이징** 적용 (2026-03-04)
  - 파일: `service/LoginHistoryService.java`, `repository/LoginHistoryRepository.java`

- [x] `ProjectId`: Lombok 미적용 복합키 클래스 → **기존 패턴 통일** (2026-03-04)
  - 파일: `domain/entity/ProjectId.java`

- [x] `Project.prjYy`: 컬럼 길이 오류 수정 `length = 1020` → `length = 4` (2026-03-04)
  - 파일: `domain/entity/Project.java`

- [x] `ProjectRepository`: 중복 확인 메서드 **효율화** (2026-03-04)
  - 파일: `repository/ProjectRepository.java`, `service/ProjectService.java`

- [x] `UserDto`: `bbrC`(부점코드) 필드를 `ListResponse`, `DetailResponse`에 추가 (2026-03-04)
  - 파일: `dto/UserDto.java`

- [x] `CcodemDto`: Swagger `@Schema` 어노테이션 추가 (2026-03-03)
- [x] `CcodemRepositoryImpl`: 클래스/메서드 JavaDoc 보강 (2026-03-03)
- [x] `README.md`: 개발 노트 작성 및 최신화 (2026-03-04, 2026-03-25)
- [x] 전체 소스 코드 주석 전수 점검 (81개 파일) — 한글 JavaDoc/인라인 주석 표준 충족 확인 (2026-03-25)
- [x] 테스트 코드 (ProjectServiceTest, AuthControllerTest 등) 컴파일 오류 및 도메인 패키지 이동 문제 수정 (2026-03-26)
- [x] 엔티티 클래스 전수 검사하여 데이터베이스 스키마 JPA 3.2 주석(`@Table(comment=...)`, `@Column`) 부착 (2026-03-30)
- [x] 전체 프로젝트 문서/주석 리프레시 — AdminController JavaDoc 보강, README/CLAUDE/TASK.md 최신화 (2026-04-10)
- [x] 정보화실무협의회(council) 도메인 구현 — CouncilController(23 엔드포인트), 8개 서비스, 14개 엔티티, 9개 Repository (2026-04-05)
- [x] 시스템관리(admin) 모듈 구현 — AdminController/AdminService, @PreAuthorize ROLE_ADMIN 이중 보호 (2026-04-04)
- [x] 예산작업(budget/work) 구현 — BudgetWorkController(3 API), Bbugtm 엔티티, 편성률 Upsert (2026-04-04)
- [x] 정보기술부문 계획(budget/plan) 구현 — PlanController, Bplanm/Bproja 엔티티, JSON 스냅샷 저장 (2026-04-02)
