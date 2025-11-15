package com.truvis.user.application;

import com.truvis.common.exception.MemberException;
import com.truvis.common.security.JwtTokenProvider;
import com.truvis.common.security.TokenBlacklistService;
import com.truvis.user.domain.User;
import com.truvis.user.model.LoginRequest;
import com.truvis.user.model.LoginResponse;
import com.truvis.user.model.TokenResponse;
import com.truvis.user.repository.UserRepository;
import com.truvis.user.repository.RedisRefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@Slf4j
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider jwtTokenProvider,
            RedisRefreshTokenRepository refreshTokenRepository,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> MemberException.userNotFoundByEmail(request.email()));

        // 2. 이메일 로그인 사용자인지 검증 (도메인 로직)
        user.validateEmailLoginUser();

        // 3. 비밀번호 검증
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw MemberException.invalidPassword();
        }

        // 4. AccessToken 생성
        String accessToken = jwtTokenProvider.createToken(user.getId());
        Date accessTokenExpiresAt = jwtTokenProvider.getExpirationDate(accessToken);
        
        // 5. RefreshToken 생성
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());
        Date refreshTokenExpiresAt = jwtTokenProvider.getExpirationDate(refreshToken);
        
        // 6. RefreshToken을 Redis에 저장
        refreshTokenRepository.save(user.getId(), refreshToken);

        // 7. 응답 반환
        return LoginResponse.of(
            accessToken,
            refreshToken,
            toLocalDateTime(accessTokenExpiresAt),
            toLocalDateTime(refreshTokenExpiresAt)
        );
    }
    
    /**
     * 토큰 갱신
     */
    public TokenResponse refresh(String refreshToken) {
        // 1. RefreshToken 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw MemberException.invalidRefreshToken();
        }
        
        // 2. 토큰에서 userId 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        
        // 3. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> MemberException.userNotFound(userId));
        
        // 4. Redis에 저장된 RefreshToken과 비교
        String savedToken = refreshTokenRepository.findByUserId(user.getId());
        if (savedToken == null || !savedToken.equals(refreshToken)) {
            throw MemberException.invalidRefreshToken();
        }
        
        // 5. 새로운 AccessToken 발급
        String newAccessToken = jwtTokenProvider.createToken(user.getId());
        Date expiresAt = jwtTokenProvider.getExpirationDate(newAccessToken);
        
        return TokenResponse.of(newAccessToken, toLocalDateTime(expiresAt));
    }
    
    /**
     * 로그아웃
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {

        // 1. AccessToken 블랙리스트에 추가
        if (jwtTokenProvider.validateToken(accessToken)) {
            Duration remainingTime = calculateRemainingTime(accessToken);
            tokenBlacklistService.addToBlacklist(accessToken, remainingTime);
        }

        // 2. RefreshToken 검증 및 사용자 조회
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.debug("로그아웃 시 RefreshToken 유효하지 않음");
            return;
        }
        
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = userRepository.findById(userId).orElse(null);
        
        if (user == null) {
            log.debug("로그아웃 시 사용자를 찾을 수 없음: userId={}", userId);
            return;
        }
        
        // 3. Redis에서 RefreshToken 삭제
        refreshTokenRepository.delete(user.getId());
        log.info("로그아웃 완료: userId={}", userId);
    }
    
    /**
     * 토큰의 남은 유효 시간 계산
     */
    private Duration calculateRemainingTime(String token) {
        Date expirationDate = jwtTokenProvider.getExpirationDate(token);
        Date now = new Date();
        
        long remainingMillis = expirationDate.getTime() - now.getTime();
        
        // 음수면 0으로 (이미 만료된 토큰)
        if (remainingMillis < 0) {
            return Duration.ZERO;
        }
        
        return Duration.ofMillis(remainingMillis);
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
