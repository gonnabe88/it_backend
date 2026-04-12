package com.kdb.it.domain.budget.status.controller;

import com.kdb.it.domain.budget.status.dto.BudgetStatusDto;
import com.kdb.it.domain.budget.status.service.BudgetStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 예산 현황 컨트롤러
 *
 * <p>
 * 3개 탭(정보화사업/전산업무비/경상사업)의 예산 현황 조회 API를 제공합니다.
 * 각 탭은 편성요청 금액과 조정(편성) 금액을 병렬로 포함합니다.
 * </p>
 *
 * // Design Ref: §3.6 — BudgetStatusController 설계
 */
@RestController
@RequestMapping("/api/budget/status")
@RequiredArgsConstructor
@Tag(name = "BudgetStatus", description = "예산 현황 API")
public class BudgetStatusController {

    private final BudgetStatusService budgetStatusService;

    /**
     * 정보화사업 예산 현황 조회
     *
     * @param bgYy 예산년도 (예: 2026)
     * @return 정보화사업별 편성요청/조정 금액 목록
     */
    @Operation(summary = "정보화사업 예산 현황 조회")
    @GetMapping("/projects")
    public ResponseEntity<List<BudgetStatusDto.ProjectResponse>> getProjects(
            @Parameter(description = "예산년도", example = "2026")
            @RequestParam("bgYy") String bgYy) {
        return ResponseEntity.ok(budgetStatusService.getProjectStatus(bgYy));
    }

    /**
     * 전산업무비 예산 현황 조회
     *
     * @param bgYy 예산년도 (예: 2026)
     * @return 전산업무비별 편성요청/조정 금액 목록
     */
    @Operation(summary = "전산업무비 예산 현황 조회")
    @GetMapping("/costs")
    public ResponseEntity<List<BudgetStatusDto.CostResponse>> getCosts(
            @Parameter(description = "예산년도", example = "2026")
            @RequestParam("bgYy") String bgYy) {
        return ResponseEntity.ok(budgetStatusService.getCostStatus(bgYy));
    }

    /**
     * 경상사업 예산 현황 조회
     *
     * @param bgYy 예산년도 (예: 2026)
     * @return 경상사업별 기계장치/기타무형자산 상세 목록
     */
    @Operation(summary = "경상사업 예산 현황 조회")
    @GetMapping("/ordinary")
    public ResponseEntity<List<BudgetStatusDto.OrdinaryResponse>> getOrdinary(
            @Parameter(description = "예산년도", example = "2026")
            @RequestParam("bgYy") String bgYy) {
        return ResponseEntity.ok(budgetStatusService.getOrdinaryStatus(bgYy));
    }
}
