package com.truvis.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

public class JwtTokenProvider {

    private final Key key;
    private final long validityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;


    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long validityInMilliseconds,
            @Value("${jwt.refresh-expiration}") long refreshTokenValidityInMilliseconds
    ) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        this.validityInMilliseconds = validityInMilliseconds;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds;  // 추가!
    }

    // 1. AccessToken 생성
    public String createToken(Long userId) {
        return createToken(userId, validityInMilliseconds);
    }

    // 2. RefreshToken 생성
    public String createRefreshToken(Long userId) {
        return createToken(userId, refreshTokenValidityInMilliseconds);
    }
    
    // 3. 공통 토큰 생성 로직 (private)
    private String createToken(Long userId, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(userId.toString())  // JWT 표준: subject에 userId 저장
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. 토큰에서 userId 추출
    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    // 호환성을 위해 남겨둠 (기존 코드에서 사용 중인 경우)
    @Deprecated
    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    // 4. 토큰 만료시간 추출
    public Date getExpirationDate(String token) {
        return getClaims(token).getExpiration();
    }

    // 5. 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Claims claims = getClaims(token);
            Date expiration = claims.getExpiration();
            return !expiration.before(new Date());  // 만료 시간이 현재보다 이후인지 확인
        } catch (Exception e) {
            System.err.println("🔴 JWT 검증 실패: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return false;  // 파싱 실패 = 유효하지 않은 토큰
        }
    }

    // 6. 토큰 파싱 (
    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
