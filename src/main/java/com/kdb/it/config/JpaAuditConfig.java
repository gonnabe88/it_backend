package com.kdb.it.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                // 로그인되지 않은 경우 null 또는 기본값 반환
                // 필요시 "SYSTEM" 등의 기본값을 설정할 수 있습니다.
                return Optional.empty(); 
            }
            // principal이 String(사용자 ID)이라고 가정하거나, UserDetails를 구현한 객체라면getUsername()을 사용
            // 여기서는 단순히 name을 가져옵니다.
            return Optional.ofNullable(authentication.getName());
        };
    }
}
