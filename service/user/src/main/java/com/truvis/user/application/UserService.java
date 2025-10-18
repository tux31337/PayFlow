package com.truvis.user.application;

import com.truvis.common.exception.MemberException;
import com.truvis.user.domain.User;
import com.truvis.user.model.SignUpRequest;
import com.truvis.user.model.SignUpResponse;
import com.truvis.user.domain.UserRepository;
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

    public UserService(
            UserRepository userRepository,
            EmailVerificationService emailVerificationService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 회원가입
     */
    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        log.info("회원가입 시작: email={}", request.getEmail());

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

        log.info("회원가입 완료: userId={}, email={}", savedUser.getId(), savedUser.getEmailValue());

        // 8. 응답 생성
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
