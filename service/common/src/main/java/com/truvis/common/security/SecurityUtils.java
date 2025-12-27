package com.truvis.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 관련 유틸리티 클래스
 * 
 * SecurityContextHolder에서 인증된 사용자 정보를 쉽게 추출할 수 있도록 제공
 */
public final class SecurityUtils {

    private SecurityUtils() {
        // 유틸리티 클래스 - 인스턴스 생성 방지
    }

    /**
     * 현재 인증된 사용자의 ID 조회
     * 
     * @return 사용자 ID
     * @throws IllegalStateException 인증되지 않은 경우
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Long) {
            return (Long) principal;
        }

        if (principal instanceof String) {
            // "anonymousUser" 같은 경우
            throw new IllegalStateException("인증되지 않은 사용자입니다");
        }

        throw new IllegalStateException("알 수 없는 인증 타입입니다: " + principal.getClass().getName());
    }

    /**
     * 현재 인증된 사용자의 ID 조회 (null 허용)
     * 
     * @return 사용자 ID 또는 null (인증되지 않은 경우)
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            return getCurrentUserId();
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * 현재 사용자가 인증되어 있는지 확인
     * 
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        return principal instanceof Long;
    }

    /**
     * 현재 사용자가 특정 사용자인지 확인
     * 
     * @param userId 확인할 사용자 ID
     * @return 일치 여부
     */
    public static boolean isCurrentUser(Long userId) {
        Long currentUserId = getCurrentUserIdOrNull();
        return currentUserId != null && currentUserId.equals(userId);
    }
}

