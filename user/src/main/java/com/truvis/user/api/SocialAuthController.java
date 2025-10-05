package com.truvis.user.api;

import com.truvis.user.application.SocialAuthService;
import com.truvis.user.model.LoginResponse;
import com.truvis.user.model.SocialLoginRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth/social")
public class SocialAuthController {

    private final SocialAuthService socialAuthService;

    public SocialAuthController(SocialAuthService socialAuthService) {
        this.socialAuthService = socialAuthService;
    }

    /**
     * 소셜 로그인 (카카오/네이버/구글 통합)
     * POST /api/auth/social/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> socialLogin(@Valid @RequestBody SocialLoginRequest request) {
        log.info("소셜 로그인 요청: provider={}, code={}", request.provider(), request.code());
        LoginResponse response = socialAuthService.socialLogin(request.provider(), request.code());
        return ResponseEntity.ok(response);
    }
}