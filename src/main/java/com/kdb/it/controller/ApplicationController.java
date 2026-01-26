package com.kdb.it.controller;

import java.net.URI;

import com.kdb.it.dto.ApplicationDto;
import com.kdb.it.service.ApplicationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "Application", description = "신청서 API")
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    @Operation(summary = "전체 신청서 조회", description = "모든 신청서 정보를 조회합니다.")
    public ResponseEntity<java.util.List<ApplicationDto.Response>> getApplications() {
        return ResponseEntity.ok(applicationService.getApplications());
    }

    @GetMapping("/{apfMngNo}")
    @Operation(summary = "특정 신청서 조회", description = "특정 신청서를 조회합니다.")
    public ResponseEntity<ApplicationDto.Response> getApplication(@PathVariable("apfMngNo") String apfMngNo) {
        ApplicationDto.Response response = applicationService.getApplication(apfMngNo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk-get")
    @Operation(summary = "신청서 일괄 조회", description = "여러 개의 신청서를 한 번에 조회합니다. 존재하지 않는 신청서는 결과에서 제외됩니다.")
    public ResponseEntity<java.util.List<ApplicationDto.Response>> bulkGetApplications(
            @RequestBody ApplicationDto.BulkGetRequest request) {
        java.util.List<ApplicationDto.Response> responses = applicationService.getApplicationsByIds(request);
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "신규 신청서 생성", description = "신규 신청서를 생성합니다.")
    public ResponseEntity<String> submit(@RequestBody ApplicationDto.CreateRequest request) {
        String apfMngNo = applicationService.submit(request);
        return ResponseEntity.created(URI.create("/api/applications/" + apfMngNo)).body(apfMngNo);
    }

    @PostMapping("/{apfMngNo}/approve")
    @Operation(summary = "신청서 승인", description = "신청서를 승인합니다.")
    public ResponseEntity<Void> approve(@PathVariable("apfMngNo") String apfMngNo,
            @RequestBody ApplicationDto.ApproveRequest request) {
        applicationService.approve(apfMngNo, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk-approve")
    @Operation(summary = "신청서 일괄 승인", description = "여러 개의 신청서를 한 번에 승인합니다. 전체를 하나의 트랜잭션으로 처리하며, 하나라도 실패하면 전체 롤백됩니다.")
    public ResponseEntity<ApplicationDto.BulkApproveResponse> bulkApprove(
            @RequestBody ApplicationDto.BulkApproveRequest request) {
        ApplicationDto.BulkApproveResponse response = applicationService.bulkApprove(request);
        return ResponseEntity.ok(response);
    }

}
