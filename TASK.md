# 프로젝트 백로그 (Project Backlog)

> 마지막 갱신일: 2026-03-03

## 보안 개선

- [ ] `CustomPasswordEncoder`: SHA-256 + 빈 솔트 방식에서 **BCrypt** 방식으로 마이그레이션
  - 파일: `config/CustomPasswordEncoder.java`
  - 우선순위: 높음 (운영 배포 전 필수)

- [ ] `SecurityConfig`: CORS 설정을 `allowedOrigins("*")`에서 **운영 도메인으로 제한**
  - 파일: `config/SecurityConfig.java`
  - 우선순위: 높음 (운영 배포 전 필수)

- [ ] `AuthDto.SignupRequest`: 외부 회원가입 API에 **관리자 권한 제한** 적용
  - 파일: `dto/AuthDto.java`
  - 우선순위: 중간

## 코드 품질

- [ ] `JwtUtil.validateToken()`: `System.err.println` → **SLF4J Logger** 교체
  - 파일: `util/JwtUtil.java`
  - 우선순위: 중간
  - 태그: `TODO`

- [ ] `CustomUserDetailsService`: 권한(authorities) 목록이 빈 상태 → **역할 기반 접근 제어(RBAC)** 구현 검토
  - 파일: `service/CustomUserDetailsService.java`
  - 우선순위: 낮음

## 기능 개선

- [ ] 전역 예외 핸들러 `@ControllerAdvice` 구현하여 **표준화된 오류 응답** 제공
  - 현재 `CustomGeneralException`만 정의되어 있고 핸들러 미구현
  - 우선순위: 중간

- [ ] `LoginHistoryService.getLoginHistory()`: 메모리 기반 50건 제한 → **DB 레벨 페이징** 적용
  - 파일: `service/LoginHistoryService.java`
  - 우선순위: 낮음

## 완료된 항목

- [x] `CcodemDto`: Swagger `@Schema` 어노테이션 추가 (2026-03-03)
- [x] `CcodemRepositoryImpl`: 클래스/메서드 JavaDoc 보강 (2026-03-03)
- [x] `README.md`: 개발 노트 작성 (2026-03-03)
