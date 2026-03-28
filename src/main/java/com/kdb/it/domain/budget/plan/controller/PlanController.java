package com.kdb.it.domain.budget.plan.controller;

import com.kdb.it.domain.budget.plan.dto.PlanDto;
import com.kdb.it.domain.budget.plan.service.PlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * 정보기술부문 계획 컨트롤러
 *
 * <p>
 * 정보기술부문 계획(TAAABB_BPLANM) 등록, 조회, 삭제 API를 제공합니다.
 * </p>
 *
 * <p>
 * 엔드포인트 목록:
 * </p>
 * <ul>
 * <li>GET  /api/plans         - 전체 계획 목록 조회</li>
 * <li>GET  /api/plans/{id}    - 단건 계획 상세 조회</li>
 * <li>POST /api/plans         - 계획 등록</li>
 * <li>DELETE /api/plans/{id}  - 계획 논리 삭제</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "정보기술부문 계획", description = "IT 부문 연도별 계획 관리 API")
public class PlanController {

    private final PlanService planService;

    /**
     * 전체 계획 목록 조회
     *
     * <p>
     * 삭제되지 않은 전체 계획을 등록일시 내림차순으로 반환합니다.
     * </p>
     *
     * @return 계획 목록 (200 OK)
     */
    @GetMapping
    @Operation(summary = "계획 목록 조회", description = "등록된 전체 IT 부문 계획 목록을 조회합니다.")
    public ResponseEntity<List<PlanDto.ListResponse>> getPlans() {
        return ResponseEntity.ok(planService.getPlans());
    }

    /**
     * 단건 계획 상세 조회
     *
     * <p>
     * 계획관리번호로 단건 계획과 JSON 스냅샷, 연결된 프로젝트 목록을 조회합니다.
     * </p>
     *
     * @param plnMngNo 계획관리번호 (예: PLN-2026-0001)
     * @return 계획 상세 정보 (200 OK) 또는 404 Not Found
     */
    @GetMapping("/{plnMngNo}")
    @Operation(summary = "계획 상세 조회", description = "계획관리번호로 계획 상세 정보를 조회합니다.")
    public ResponseEntity<PlanDto.DetailResponse> getPlan(@PathVariable("plnMngNo") String plnMngNo) {
        return ResponseEntity.ok(planService.getPlan(plnMngNo));
    }

    /**
     * 계획 등록
     *
     * <p>
     * 대상년도, 계획구분, 대상 프로젝트 목록을 받아 계획을 등록합니다.
     * 대상 프로젝트의 예산 합계와 JSON 스냅샷을 자동으로 생성하여 저장합니다.
     * </p>
     *
     * @param request 계획 생성 요청 DTO
     * @param uriBuilder URI 빌더 (Location 헤더 생성용)
     * @return 201 Created (Location: /api/plans/{plnMngNo})
     */
    @PostMapping
    @Operation(summary = "계획 등록", description = "IT 부문 계획을 등록합니다. 대상 프로젝트의 예산이 자동으로 집계됩니다.")
    public ResponseEntity<Void> createPlan(@RequestBody PlanDto.CreateRequest request,
                                           UriComponentsBuilder uriBuilder) {
        String plnMngNo = planService.createPlan(request);
        URI location = uriBuilder.path("/api/plans/{plnMngNo}").buildAndExpand(plnMngNo).toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * 계획 논리 삭제
     *
     * <p>
     * 계획 엔티티의 DEL_YN을 'Y'로 변경합니다.
     * 연결된 프로젝트관계(BPROJA) 레코드도 함께 논리 삭제됩니다.
     * </p>
     *
     * @param plnMngNo 계획관리번호
     * @return 204 No Content 또는 404 Not Found
     */
    @DeleteMapping("/{plnMngNo}")
    @Operation(summary = "계획 삭제", description = "계획을 논리 삭제합니다. (DEL_YN='Y')")
    public ResponseEntity<Void> deletePlan(@PathVariable("plnMngNo") String plnMngNo) {
        planService.deletePlan(plnMngNo);
        return ResponseEntity.noContent().build();
    }
}
