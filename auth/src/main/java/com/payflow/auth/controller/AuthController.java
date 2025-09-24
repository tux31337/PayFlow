package com.payflow.auth.controller;

import com.payflow.auth.dto.AuthResponse;
import com.payflow.auth.dto.LoginRequest;
import com.payflow.auth.dto.SignupRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    // TODO: AuthService 의존성 주입 후 구현
    // private final AuthService authService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth module with JWT is working!");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest request) {
        log.info("Signup request received for email: {}", request.getEmail());
        // TODO: AuthService.signup() 구현 후 연동
        return ResponseEntity.ok("Signup endpoint working! Email: " + request.getEmail());
    }
    
    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        // TODO: AuthService.login() 구현 후 연동
        return ResponseEntity.ok("Login endpoint working! Email: " + request.getEmail());
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@RequestHeader("Authorization") String refreshToken) {
        log.info("Refresh token request received");
        // TODO: AuthService.refreshToken() 구현 후 연동
        return ResponseEntity.ok("Refresh token endpoint working!");
    }
}
