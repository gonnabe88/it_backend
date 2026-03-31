package com.kdb.it.common.admin.controller;

import com.kdb.it.common.admin.dto.AdminDto;
import com.kdb.it.common.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 시스템 관리자 전용 API 컨트롤러
 *
 * <p>
 * 모든 엔드포인트는 {@code ROLE_ADMIN} (ITPAD001) 역할 보유자만 접근 가능합니다.
 * SecurityConfig에서 {@code /api/admin/**} 경로에 대해 {@code hasRole("ADMIN")} 설정이
 * 이미 적용되어 있으며, 메서드 레벨에서도 이중으로 보호합니다.
 * </p>
 *
 * <p>
 * Design Ref: §2.2 — AdminController 설계, §6.1 — 보안 설계
 * </p>
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "시스템 관리자 전용 API (ITPAD001 역할 필요)")
public class AdminController {

    private final AdminService adminService;

    // =========================================================================
    // 공통코드 관리 (TAAABB_CCODEM)
    // =========================================================================

    /**
     * 공통코드 목록 조회
     * 삭제되지 않은(DEL_YN='N') 전체 공통코드를 코드순서 오름차순으로 반환합니다.
     *
     * @return 공통코드 응답 DTO 목록
     */
    @GetMapping("/codes")
    @Operation(summary = "공통코드 목록 조회", description = "삭제되지 않은 전체 공통코드를 코드순서 오름차순으로 반환합니다.")
    public ResponseEntity<List<AdminDto.CodeResponse>> getCodes() {
        return ResponseEntity.ok(adminService.getCodes());
    }

    /**
     * 공통코드 추가
     *
     * @param req 공통코드 생성 요청 DTO
     * @return 201 Created
     */
    @PostMapping("/codes")
    @Operation(summary = "공통코드 추가", description = "새로운 공통코드를 추가합니다. C_ID 중복 시 400 반환.")
    public ResponseEntity<Void> createCode(@Valid @RequestBody AdminDto.CodeRequest req) {
        adminService.createCode(req);
        return ResponseEntity.status(201).build();
    }

    /**
     * 공통코드 수정 (인라인 편집 즉시 저장)
     *
     * @param cdId 코드ID
     * @param req  공통코드 수정 요청 DTO
     * @return 200 OK
     */
    @PutMapping("/codes/{cdId}")
    @Operation(summary = "공통코드 수정", description = "공통코드 정보를 수정합니다. 인라인 편집 즉시 저장에 사용됩니다.")
    public ResponseEntity<Void> updateCode(
            @PathVariable String cdId,
            @Valid @RequestBody AdminDto.CodeRequest req) {
        adminService.updateCode(cdId, req);
        return ResponseEntity.ok().build();
    }

    /**
     * 공통코드 삭제 (Soft Delete)
     * DEL_YN='Y' 처리 — 물리 삭제 아님.
     *
     * @param cdId 코드ID
     * @return 204 No Content
     */
    @DeleteMapping("/codes/{cdId}")
    @Operation(summary = "공통코드 삭제(논리)", description = "DEL_YN='Y'로 논리 삭제합니다. 물리 삭제 아님.")
    public ResponseEntity<Void> deleteCode(@PathVariable String cdId) {
        adminService.deleteCode(cdId);
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // 자격등급, 사용자, 조직, 역할, 로그인이력, JWT토큰, 첨부파일, 대시보드
    // — Session 2/3에서 구현 예정
    // =========================================================================
}
