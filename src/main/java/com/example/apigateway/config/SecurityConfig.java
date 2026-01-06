package com.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Flux;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/auth/**", "/fallback", "/actuator/**")
                .permitAll()
                .pathMatchers("/simulation/**").authenticated()
                //                        .hasAuthority("ROLE_ADMIN")

                .pathMatchers("/analytics/**")
                .hasAnyAuthority("ROLE_ADMIN", "ROLE_PORTFOLIO_MANAGER")
                .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2
                        -> oauth2.jwt(jwtSpec
                        -> jwtSpec.jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
                )
                //                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter delegate
                = new JwtGrantedAuthoritiesConverter();

        delegate.setAuthoritiesClaimName("roles");
        delegate.setAuthorityPrefix("ROLE_");

        Converter<Jwt, Flux<GrantedAuthority>> authoritiesConverter
                = jwt -> Flux.fromIterable(delegate.convert(jwt));

        ReactiveJwtAuthenticationConverter converter
                = new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }

}

