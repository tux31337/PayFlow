package com.truvis.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailCodeVerificationRequest {
    
    @NotBlank(message = "이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "인증번호는 필수입니다")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다")
    private String code;
    
    public EmailCodeVerificationRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }
}
