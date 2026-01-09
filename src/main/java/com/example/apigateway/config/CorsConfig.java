package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins:*}")
    private String allowedOrigins;

    @Value("${app.cors.allow-credentials:false}")
    private boolean allowCredentials;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // allowedOrigins may be comma-separated list or a single '*'
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            if ("*".equals(allowedOrigins.trim())) {
                config.addAllowedOriginPattern("*");
            } else {
                String[] parts = allowedOrigins.split(",");
                for (String p : parts) {
                    config.addAllowedOrigin(p.trim());
                }
            }
        } else {
            config.addAllowedOriginPattern("*");
        }

        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsWebFilter(source);
    }
}

