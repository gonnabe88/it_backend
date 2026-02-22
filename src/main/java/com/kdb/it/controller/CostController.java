package com.kdb.it.controller;

import com.kdb.it.dto.CostDto;
import com.kdb.it.service.CostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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

@RestController
@RequestMapping("/api/cost")
@RequiredArgsConstructor
@Tag(name = "Cost", description = "전산관리비 관리 API")
public class CostController {

        private final CostService costService;

        // 특정 전산관리비 조회
        @Operation(summary = "특정 전산관리비 조회", description = "전산관리비 관리번호(IT_MNGC_NO)로 전산관리비 상세 정보를 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CostDto.Response.class))),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 전산관리비", content = @Content)
        })
        @GetMapping("/{itMngcNo}")
        public ResponseEntity<CostDto.Response> getCost(
                        @Parameter(description = "전산관리비 관리번호", required = true, example = "COST_2026_0001") @PathVariable("itMngcNo") String itMngcNo) {
                return ResponseEntity.ok(costService.getCost(itMngcNo));
        }

        // 전산관리비 수정
        @Operation(summary = "전산관리비 수정", description = "전산관리비 정보를 수정합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "수정 성공 (반환값: IT_MNGC_NO)", content = @Content(schema = @Schema(implementation = String.class))),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 전산관리비", content = @Content)
        })
        @PutMapping("/{itMngcNo}")
        public ResponseEntity<String> updateCost(
                        @Parameter(description = "전산관리비 관리번호", required = true, example = "COST_2026_0001") @PathVariable("itMngcNo") String itMngcNo,
                        @RequestBody CostDto.UpdateRequest request) {
                return ResponseEntity.ok(costService.updateCost(itMngcNo, request));
        }

        // 전산관리비 삭제
        @Operation(summary = "전산관리비 삭제", description = "전산관리비를 삭제(Soft Delete)합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "삭제 성공", content = @Content),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 전산관리비", content = @Content)
        })
        @DeleteMapping("/{itMngcNo}")
        public ResponseEntity<Void> deleteCost(
                        @Parameter(description = "전산관리비 관리번호", required = true, example = "COST_2026_0001") @PathVariable("itMngcNo") String itMngcNo) {
                costService.deleteCost(itMngcNo);
                return ResponseEntity.ok().build();
        }

        // 전체 전산관리비 조회
        @Operation(summary = "전체 전산관리비 조회", description = "등록된 모든 전산관리비 목록을 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CostDto.Response.class)))
        })
        @GetMapping
        public ResponseEntity<List<CostDto.Response>> getCostList() {
                return ResponseEntity.ok(costService.getCostList());
        }

        // 신규 전산관리비 생성
        @Operation(summary = "신규 전산관리비 생성", description = "새로운 전산관리비를 생성합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "생성 성공 (반환값: IT_MNGC_NO)", content = @Content(schema = @Schema(implementation = String.class)))
        })
        @PostMapping
        public ResponseEntity<String> createCost(@RequestBody CostDto.CreateRequest request) {
                return ResponseEntity.ok(costService.createCost(request));
        }

        // 전산관리비 일괄 조회
        @Operation(summary = "전산관리비 일괄 조회", description = "여러 개의 전산관리비 관리번호로 상세 정보를 일괄 조회합니다.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CostDto.Response.class)))
        })
        @PostMapping("/bulk-get")
        public ResponseEntity<List<CostDto.Response>> getCostsByIds(@RequestBody CostDto.BulkGetRequest request) {
                return ResponseEntity.ok(costService.getCostsByIds(request));
        }
}
