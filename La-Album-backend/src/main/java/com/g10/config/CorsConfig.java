package com.g10.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // ✅ 使用 allowedOriginPatterns 代替 allowedOrigins
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:8080", "https://your-frontend.com"));

        // ✅ 允许携带 Cookie
        config.setAllowCredentials(true);

        // ✅ 允许所有常见 HTTP 方法
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // ✅ 允许所有 Headers
        config.setAllowedHeaders(Arrays.asList("*"));

        // ✅ 允许前端访问这些 Header（可选）
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
