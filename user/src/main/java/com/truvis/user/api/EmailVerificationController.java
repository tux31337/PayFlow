package com.truvis.user.api;

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
        
        String email = request.getEmail();
        log.info("이메일 인증번호 발송 요청: {}", email);
        
        emailVerificationService.requestEmailVerificationCode(email);
        
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
        
        String email = request.getEmail();
        String code = request.getCode();
        log.info("인증번호 검증 요청: email={}, code={}", email, code);
        
        String verifiedEmail = emailVerificationService.verifyEmailCode(email, code);
        
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
        
        String email = request.getEmail();
        log.info("이메일 인증번호 재발송 요청: {}", email);
        
        // 예외 발생시 GlobalExceptionHandler가 자동으로 처리! 🎉
        emailVerificationService.resendVerificationCode(email);
        
        return ResponseEntity.ok(
            ApiResponse.success("인증번호가 재발송되었습니다. 이메일을 확인해주세요.")
        );
    }
}
