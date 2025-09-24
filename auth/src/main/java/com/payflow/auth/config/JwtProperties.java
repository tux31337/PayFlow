package com.payflow.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret = "3cddc837e05c558ab36bf843ab78fbcf";
    private long expiration = 86400000; // 24시간 (밀리초)
    private long refreshExpiration = 604800000; // 7일 (밀리초)
}
