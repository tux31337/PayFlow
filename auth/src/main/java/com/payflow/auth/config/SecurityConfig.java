package com.payflow.auth.config;

import com.payflow.auth.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // 인증 관련 엔드포인트는 허용
                        .requestMatchers("/api/auth/**").permitAll()
                        // 헬스체크 엔드포인트 허용
                        .requestMatchers("/api/health").permitAll()
                        // H2 콘솔 허용 (개발용)
                        .requestMatchers("/h2-console/**").permitAll()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // H2 콘솔 접근을 위한 설정 (최신 방식)
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );
        
        return http.build();
    }
}
