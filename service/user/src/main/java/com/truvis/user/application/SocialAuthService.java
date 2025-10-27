package com.truvis.user.application;

import com.truvis.common.exception.MemberException;
import com.truvis.common.security.JwtTokenProvider;
import com.truvis.user.domain.*;
import com.truvis.user.repository.UserRepository;
import com.truvis.user.repository.RedisRefreshTokenRepository;
import com.truvis.user.infrastructure.oauth.OAuth2ClientProvider;
import com.truvis.user.model.LoginResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@Slf4j
@Transactional(readOnly = true)
public class SocialAuthService {

    private final OAuth2ClientProvider oAuth2ClientProvider;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRefreshTokenRepository refreshTokenRepository;

    public SocialAuthService(
            OAuth2ClientProvider oAuth2ClientProvider,
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            RedisRefreshTokenRepository refreshTokenRepository
    ) {
        this.oAuth2ClientProvider = oAuth2ClientProvider;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * 소셜 로그인 (카카오/네이버/구글 통합)
     */
    @Transactional
    public LoginResponse socialLogin(SignUpType provider, String authorizationCode) {
        // 1. Authorization Code로 Access Token 받기
        OAuth2Client client = oAuth2ClientProvider.getClient(provider);

        // 2. Access Token으로 사용자 정보 받기
        String accessToken = client.getAccessToken(authorizationCode);
        SocialUserInfo socialUserInfo = client.getUserInfo(accessToken);

        // 3. 회원 조회 또는 생성
        User user = findOrCreateUser(socialUserInfo);

        // 4. JWT 토큰 발급
        return generateTokenResponse(user);
    }

    /**
     * 기존 회원 조회 또는 신규 회원 생성
     */
    private User findOrCreateUser(SocialUserInfo socialUserInfo) {
        Optional<User> existingUser = userRepository.findByEmail(socialUserInfo.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();

            // 동일 이메일이지만 다른 가입 방식인 경우 예외 발생
            if (!user.isSocialProviderUser(socialUserInfo.getProvider())) {
                throw MemberException.emailAlreadyExistsWithDifferentSignUpType(
                        socialUserInfo.getEmail(),
                        user.getSignUpType().getDescription()
                );
            }

            return user;
        }

        // 신규 회원 생성
        User newUser = User.createSocialUser(
                socialUserInfo.getEmail(),
                socialUserInfo.getName(),
                socialUserInfo.getProvider(),
                socialUserInfo.getSocialId()
        );

        return userRepository.save(newUser);
    }

    /**
     * JWT 토큰 생성 및 응답 반환
     */
    private LoginResponse generateTokenResponse(User user) {
        // AccessToken 생성
        String accessToken = jwtTokenProvider.createToken(user.getId());
        Date accessTokenExpiresAt = jwtTokenProvider.getExpirationDate(accessToken);

        // RefreshToken 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        Date refreshTokenExpiresAt = jwtTokenProvider.getExpirationDate(refreshToken);

        // RefreshToken을 Redis에 저장
        refreshTokenRepository.save(user.getId(), refreshToken);

        return LoginResponse.of(
                accessToken,
                refreshToken,
                toLocalDateTime(accessTokenExpiresAt),
                toLocalDateTime(refreshTokenExpiresAt)
        );
    }

    /**
     * Date를 LocalDateTime으로 변환
     */
    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}