package com.kdb.it.controller;

import com.kdb.it.dto.UserDto;
import com.kdb.it.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "사용자 관리 API")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "조직별 사용자 조회", description = "특정 조직코드에 해당하는 사용자 목록을 조회합니다.")
    public ResponseEntity<List<UserDto.ListResponse>> getUsersByOrganization(@RequestParam("orgCode") String orgCode) {
        return ResponseEntity.ok(userService.getUsersByOrganization(orgCode));
    }

    @GetMapping("/{eno}")
    @Operation(summary = "사용자 상세 조회", description = "행번으로 사용자 상세 정보를 조회합니다.")
    public ResponseEntity<UserDto.DetailResponse> getUser(@PathVariable("eno") String eno) {
        return ResponseEntity.ok(userService.getUser(eno));
    }
}
