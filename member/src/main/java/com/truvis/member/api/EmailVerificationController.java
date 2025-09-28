package com.truvis.member.api;

import com.truvis.member.application.EmailVerificationService;
import com.truvis.member.model.EmailVerificationRequest;
import com.truvis.member.model.EmailVerificationResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class EmailVerificationController {

    EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증 요청
     * @return
     */
    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerificationResponse> requestVerification(
            @Valid @RequestBody EmailVerificationRequest request
            ) {
        String token = emailVerificationService.requestEmailVerification(request.getEmail());

        return null;
    }

    /**
     * 인증 링크 요청
     * @return
     */
    @GetMapping("/email/verify/{token}")
    public ResponseEntity<?> verifyEmail() {

        return null;
    }

}
