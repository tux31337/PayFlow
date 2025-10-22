package com.truvis.user.domain;

import com.truvis.common.exception.MemberException;
import com.truvis.common.model.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot<Long> {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Embedded
    private Email email;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignUpType signUpType;

    // 비밀번호 getter - Application 계층에서 검증용
    // 일반 가입용 - 소셜 가입시 null
    @Column(length = 100)
    private String password;
    
    // 소셜 가입용 - 일반 가입시 null
    @Column(length = 100)
    private String socialId;
    
    @Embedded
    private SocialProvider socialProvider;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    private User(Email email, String name, SignUpType signUpType,
                String password, String socialId, SocialProvider socialProvider) {
        this.email = email;
        this.name = name;
        this.signUpType = signUpType;
        this.password = password;
        this.socialId = socialId;
        this.socialProvider = socialProvider;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 일반 이메일 가입용 팩토리 메서드
    public static User createEmailUser(String emailValue, String name, String password) {
        return User.builder()
                .email(Email.of(emailValue))
                .name(name)
                .signUpType(SignUpType.EMAIL)
                .password(password)
                .build();
    }
    
    // 소셜 가입용 팩토리 메서드
    public static User createSocialUser(String emailValue, String name, SignUpType signUpType, 
                                       String socialId) {
        return User.builder()
                .email(Email.of(emailValue))
                .name(name)
                .signUpType(signUpType)
                .socialId(socialId)
                .socialProvider(SocialProvider.of(signUpType))
                .build();
    }
    
    // 이메일 값을 가져오는 편의 메서드
    public String getEmailValue() {
        return this.email.getValue();
    }
    
    // 도메인 메서드
    public boolean isEmailUser() {
        return this.signUpType == SignUpType.EMAIL;
    }
    
    public boolean isSocialUser() {
        return this.signUpType != SignUpType.EMAIL;
    }
    
    @PreUpdate
    private void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }

    // 이메일 로그인 사용자인지 검증 (도메인 로직)
    public void validateEmailLoginUser() {
        if (this.isSocialUser()) {
            throw MemberException.socialUserCannotUsePasswordLogin();
        }

        if (this.password == null) {
            throw MemberException.passwordNotSet();
        }
    }

    // 특정 프로바이더의 소셜 계정인지 확인
    public boolean isSocialProviderUser(SignUpType signUpType) {
        if (this.socialProvider == null) {
            return false;
        }
        return this.socialProvider.getValue().equals(signUpType.name());
    }

    // 소셜 사용자 검증
    public void validateSocialUser() {
        if (!this.isSocialUser()) {
            throw MemberException.emailUserCannotHaveSocialProvider();
        }

        if (this.socialId == null || this.socialProvider == null) {
            throw MemberException.invalidSocialAccount();
        }
    }
}
