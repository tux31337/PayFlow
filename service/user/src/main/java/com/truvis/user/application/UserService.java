package com.truvis.user.application;

import com.truvis.common.exception.MemberException;
import com.truvis.user.domain.User;
import com.truvis.user.model.SignUpRequest;
import com.truvis.user.model.SignUpResponse;
import com.truvis.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final WelcomeEmailService welcomeEmailService;

    public UserService(
            UserRepository userRepository,
            EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder,
            WelcomeEmailService welcomeEmailService) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.welcomeEmailService = welcomeEmailService;

    }

    /**
     * 회원가입
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        // 1. 비밀번호 확인 검증
        validatePasswordConfirm(request.getPassword(), request.getPasswordConfirm());

        // 2. 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw MemberException.emailAlreadyExists(request.getEmail());
        }

        // 3. 이메일 인증 완료 확인
        if (!emailVerificationService.isEmailVerified(request.getEmail())) {
            throw MemberException.emailNotVerified();
        }

        // 4. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 5. User 생성
        User user = User.createEmailUser(
                request.getEmail(),
                request.getName(),
                encodedPassword
        );

        // 6. 저장
        User savedUser = userRepository.save(user);

        // 7. 이메일 인증 정보 정리
        emailVerificationService.clearVerifiedEmail(request.getEmail());

        // 8. 환영 메일 발송 - 비동기 (성공 신경 안씀)
        welcomeEmailService.sendWelcomeEmail(
                savedUser.getEmailValue(),
                savedUser.getName()
        );

        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmailValue());

        // 9. 응답 생성
        return SignUpResponse.of(
                savedUser.getId(),
                savedUser.getEmailValue(),
                savedUser.getName()
        );
    }

    /**
     * 비밀번호 확인 검증
     */
    private void validatePasswordConfirm(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw MemberException.passwordNotMatched();
        }
    }
}
