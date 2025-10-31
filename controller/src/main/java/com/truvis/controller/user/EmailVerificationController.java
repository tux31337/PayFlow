package com.truvis.controller.user;

import com.truvis.common.response.ApiResponse;
import com.truvis.user.application.EmailVerificationService;
import com.truvis.user.model.EmailCodeVerificationRequest;
import com.truvis.user.model.EmailVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationController {

    private final EmailVerificationService emailVerificationService;

    /**
     * 이메일 인증번호 발송
     * POST /api/member/email/verify
     */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> requestVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request) {
        
        emailVerificationService.requestEmailVerificationCode(request.getEmail());
        
        return ResponseEntity.ok(
            ApiResponse.success("인증번호가 발송되었습니다. 이메일을 확인해서 인증번호를 입력해주세요.")
        );
    }
    
    /**
     * 인증번호 검증
     * POST /api/member/email/verify/confirm
     */
    @PostMapping("/email/verify/confirm")
    public ResponseEntity<ApiResponse<String>> confirmVerificationCode(
            @Valid @RequestBody EmailCodeVerificationRequest request) {
        
        String verifiedEmail = emailVerificationService.verifyEmailCode(
                request.getEmail(), 
                request.getCode()
        );
        
        return ResponseEntity.ok(
                ApiResponse.success(verifiedEmail, "이메일 인증이 완료되었습니다")
        );
    }
    
    /**
     * 인증번호 재발송
     * POST /api/member/email/verify/resend
     */
    @PostMapping("/email/verify/resend")
    public ResponseEntity<ApiResponse<Void>> resendVerificationCode(
            @Valid @RequestBody EmailVerificationRequest request) {
        
        emailVerificationService.resendVerificationCode(request.getEmail());
        
        return ResponseEntity.ok(
            ApiResponse.success("인증번호가 재발송되었습니다. 이메일을 확인해주세요.")
        );
    }
}
