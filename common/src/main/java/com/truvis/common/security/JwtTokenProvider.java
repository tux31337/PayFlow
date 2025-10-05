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
    public String createToken(String email) {
        return createToken(email, validityInMilliseconds);
    }

    // 2. RefreshToken 생성
    public String createRefreshToken(String email) {
        return createToken(email, refreshTokenValidityInMilliseconds);
    }
    
    // 3. 공통 토큰 생성 로직 (private)
    private String createToken(String email, long validityInMilliseconds) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. 토큰에서 이메일 추출
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
