package com.truvis.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider, 
            TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Request Header에서 JWT 토큰 추출
        String token = resolveToken(request);
        
        log.debug("🔍 JWT 필터 실행: uri={}, token={}", 
                request.getRequestURI(), 
                token != null ? "있음" : "없음");
        
        // 2. 토큰 유효성 검증 (유효 + 블랙리스트 아님)
        if (token != null) {
            boolean isValid = jwtTokenProvider.validateToken(token);
            boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);
            
            log.debug("🔍 토큰 검증: valid={}, blacklisted={}", isValid, isBlacklisted);
            
            if (isValid && !isBlacklisted) {
                // 3. 토큰에서 사용자 userId 추출
                Long userId = jwtTokenProvider.getUserId(token);
                
                // 4. Authentication 객체 생성
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,                   // principal (주체) - userId로 변경
                                null,                     // credentials (비밀번호는 불필요)
                                Collections.emptyList()   // authorities (권한 목록)
                        );
                
                // 5. 요청 세부정보 설정
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                
                // 6. SecurityContext에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // 🎯 7. MDC에 userId 즉시 설정 (비동기 작업에서도 추적 가능하도록)
                MDC.put("userId", userId.toString());
                
                log.debug("✅ JWT 인증 성공: userId={}", userId);
            } else {
                log.debug("❌ JWT 인증 실패: valid={}, blacklisted={}", isValid, isBlacklisted);
            }
        }
        
        // 8. 다음 필터로 전달
        filterChain.doFilter(request, response);
    }

    /**
     * Request Header에서 토큰 추출
     * Authorization: Bearer {token}
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // "Bearer " 제거
        }

        return null;
    }
}
