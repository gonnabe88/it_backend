package com.kdb.it.controller;

import com.kdb.it.dto.AuthDto;
import com.kdb.it.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody AuthDto.SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@RequestBody AuthDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEno(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String eno = request.getEno();
        // Fetch user name for response
        String empNm = authService.getUserName(eno);
        // Better to just plain use repo here for simplicity or add a method in
        // AuthService.
        // Let's use repo directly by injecting it or adding a method in service.
        // Actually, let's inject Repo here for speed or change AuthService.

        return ResponseEntity.ok(new AuthDto.LoginResponse(eno, empNm));
        // DB call or Principal parsing
    }
}
