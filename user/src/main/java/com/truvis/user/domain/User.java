package com.truvis.user.domain;

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
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false, length = 50)
    private String name;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SignUpType signUpType;
    
    // 일반 가입용 - 소셜 가입시 null
    @Column(length = 100)
    private String password;
    
    // 소셜 가입용 - 일반 가입시 null
    @Column(length = 100)
    private String socialId;
    
    @Column(length = 20)
    private String socialProvider;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Builder
    private User(String email, String name, SignUpType signUpType, 
                String password, String socialId, String socialProvider) {
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
    public static User createEmailUser(String email, String name, String password) {
        return User.builder()
                .email(email)
                .name(name)
                .signUpType(SignUpType.EMAIL)
                .password(password)
                .build();
    }
    
    // 소셜 가입용 팩토리 메서드
    public static User createSocialUser(String email, String name, SignUpType signUpType, 
                                       String socialId, String socialProvider) {
        return User.builder()
                .email(email)
                .name(name)
                .signUpType(signUpType)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .build();
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
}
