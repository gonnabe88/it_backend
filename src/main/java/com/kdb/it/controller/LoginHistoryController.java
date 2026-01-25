package com.kdb.it.controller;

import com.kdb.it.dto.LoginHistoryDto;
import com.kdb.it.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/login-history")
@RequiredArgsConstructor
@Tag(name = "LoginHistory", description = "로그인 이력 API")
public class LoginHistoryController {

    private final LoginHistoryService loginHistoryService;

    @GetMapping
    @Operation(summary = "본인 로그인 이력 조회", description = "본인의 로그인 이력을 조회합니다.")
    public ResponseEntity<List<LoginHistoryDto.Response>> getMyLoginHistory() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String eno = authentication.getName();
        
        List<LoginHistoryDto.Response> history = loginHistoryService.getLoginHistory(eno);
        return ResponseEntity.ok(history);
    }
}
