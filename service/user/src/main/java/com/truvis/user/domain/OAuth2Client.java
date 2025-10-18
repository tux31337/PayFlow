package com.truvis.user.domain;

/**
 * 소셜 로그인 OAuth2 클라이언트 인터페이스
 * Domain이 "소셜 로그인 기능이 필요하다"를 정의
 * 실제 구현은 Infrastructure 레이어
 */
public interface OAuth2Client {

    /**
     * Authorization Code를 Access Token으로 교환
     *
     * @param code 프론트엔드로부터 받은 Authorization Code
     * @return Access Token
     */
    String getAccessToken(String code);

    /**
     * Access Token으로 사용자 정보 조회
     *
     * @param accessToken 소셜 Access Token
     * @return 소셜 사용자 정보
     */
    SocialUserInfo getUserInfo(String accessToken);
}