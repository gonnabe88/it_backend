package com.kdb.it.controller;

import com.kdb.it.dto.OrganizationDto;
import com.kdb.it.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
@Tag(name = "Organization", description = "조직 관리 API")
public class OrganizationController {

    private final OrganizationService organizationService;

    @GetMapping
    @Operation(summary = "전체 조직 조회", description = "모든 조직 정보를 조회합니다.")
    public ResponseEntity<List<OrganizationDto.Response>> getOrganizations() {
        return ResponseEntity.ok(organizationService.getOrganizations());
    }
}
