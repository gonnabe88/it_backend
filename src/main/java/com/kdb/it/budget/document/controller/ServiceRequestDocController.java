package com.kdb.it.budget.document.controller;

import com.kdb.it.budget.document.dto.ServiceRequestDocDto;
import com.kdb.it.budget.document.service.ServiceRequestDocService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * 요구사항 정의서 관리 REST 컨트롤러
 *
 * <p>
 * 요구사항 정의서(TAAABB_BRDOCM)의 CRUD 기능을 담당합니다.
 * </p>
 *
 * <p>
 * 기본 URL: {@code /api/documents}
 * </p>
 *
 * <p>
 * 보안: JWT 토큰 인증 필요
 * </p>
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document", description = "요구사항 정의서 API")
public class ServiceRequestDocController {

    /** 요구사항 정의서 비즈니스 로직 서비스 */
    private final ServiceRequestDocService serviceRequestDocService;

    /**
     * 요구사항 정의서 목록 조회
     *
     * <p>
     * DEL_YN='N'인 삭제되지 않은 요구사항 정의서 전체 목록을 반환합니다.
     * </p>
     *
     * @return HTTP 200 + 요구사항 정의서 목록
     */
    @GetMapping
    @Operation(summary = "요구사항 정의서 목록 조회", description = "삭제되지 않은 요구사항 정의서 전체 목록을 조회합니다.")
    public ResponseEntity<List<ServiceRequestDocDto.Response>> getDocuments() {
        return ResponseEntity.ok(serviceRequestDocService.getDocumentList());
    }

    /**
     * 요구사항 정의서 단건 조회
     *
     * @param docMngNo 문서관리번호 (예: DOC-2026-0001)
     * @return HTTP 200 + 요구사항 정의서 상세 정보
     */
    @GetMapping("/{docMngNo}")
    @Operation(summary = "요구사항 정의서 단건 조회", description = "문서관리번호로 요구사항 정의서를 조회합니다.")
    public ResponseEntity<ServiceRequestDocDto.Response> getDocument(@PathVariable("docMngNo") String docMngNo) {
        return ResponseEntity.ok(serviceRequestDocService.getDocument(docMngNo));
    }

    /**
     * 요구사항 정의서 생성
     *
     * <p>
     * 채번 규칙: {@code DOC-{연도}-{4자리 시퀀스}} (예: DOC-2026-0001)
     * {@code docMngNo}를 미입력 시 자동 채번됩니다.
     * </p>
     *
     * @param request 요구사항 정의서 생성 요청
     * @return HTTP 201 Created + 생성된 문서관리번호 (Location 헤더 포함)
     */
    @PostMapping
    @Operation(summary = "요구사항 정의서 생성", description = "신규 요구사항 정의서를 생성합니다. docMngNo 미입력 시 자동 채번됩니다.")
    public ResponseEntity<String> createDocument(@RequestBody ServiceRequestDocDto.CreateRequest request) {
        String docMngNo = serviceRequestDocService.createDocument(request);
        return ResponseEntity.created(URI.create("/api/documents/" + docMngNo)).body(docMngNo);
    }

    /**
     * 요구사항 정의서 수정
     *
     * @param docMngNo 수정할 문서관리번호
     * @param request  수정 요청 데이터
     * @return HTTP 200 + 수정된 문서관리번호
     */
    @PutMapping("/{docMngNo}")
    @Operation(summary = "요구사항 정의서 수정", description = "요구사항 정의서 정보를 수정합니다.")
    public ResponseEntity<String> updateDocument(
            @PathVariable("docMngNo") String docMngNo,
            @RequestBody ServiceRequestDocDto.UpdateRequest request) {
        return ResponseEntity.ok(serviceRequestDocService.updateDocument(docMngNo, request));
    }

    /**
     * 요구사항 정의서 삭제 (Soft Delete)
     *
     * <p>
     * DEL_YN='Y'로 논리 삭제합니다. 물리 삭제는 수행하지 않습니다.
     * </p>
     *
     * @param docMngNo 삭제할 문서관리번호
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{docMngNo}")
    @Operation(summary = "요구사항 정의서 삭제", description = "요구사항 정의서를 논리 삭제합니다 (DEL_YN='Y').")
    public ResponseEntity<Void> deleteDocument(@PathVariable("docMngNo") String docMngNo) {
        serviceRequestDocService.deleteDocument(docMngNo);
        return ResponseEntity.noContent().build();
    }
}
