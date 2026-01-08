package com.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

                        // 🔒 SERVICE tokens only
                        .pathMatchers("/simulation/**")
                        .access(tokenType("SERVICE"))

                        // 🔒 USER tokens only
                        .pathMatchers("/analytics/**")
//                        .getAuthorities().map(authorities ->
//                                authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))
//                        )
                        .access(tokenType("USER"))

                        .anyExchange().authenticated()
                )

                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                )
                .build();
    }

    private ReactiveAuthorizationManager<AuthorizationContext> tokenType(String expected) {
        return (authentication, context) ->
                authentication
                        .filter(auth -> auth instanceof JwtAuthenticationToken)
                        .cast(JwtAuthenticationToken.class)
                        .map(jwt ->
                                expected.equals(jwt.getToken().getClaimAsString("token_type"))
                        )
                        .map(AuthorizationDecision::new)
                        .defaultIfEmpty(new AuthorizationDecision(false));
    }

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter delegate = new JwtGrantedAuthoritiesConverter();
        delegate.setAuthoritiesClaimName("roles");
        delegate.setAuthorityPrefix("");

        Converter<Jwt, Flux<GrantedAuthority>> authoritiesConverter =
                jwt -> Flux.fromIterable(delegate.convert(jwt));

        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return converter;
    }
}

