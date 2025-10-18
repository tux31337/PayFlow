package com.truvis.user.infrastructure.oauth;

import com.truvis.common.exception.MemberException;
import com.truvis.user.domain.OAuth2Client;
import com.truvis.user.domain.SignUpType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OAuth2ClientProvider {

    private final Map<SignUpType, OAuth2Client> clients;

    public OAuth2ClientProvider(KakaoOAuth2Client kakaoClient) {
        this.clients = Map.of(
                SignUpType.KAKAO, kakaoClient
                // 나중에 네이버, 구글 추가
        );
    }

    public OAuth2Client getClient(SignUpType provider) {
        OAuth2Client client = clients.get(provider);

        if (client == null) {
            throw MemberException.unsupportedSocialProvider(provider.name());
        }

        return client;
    }
}