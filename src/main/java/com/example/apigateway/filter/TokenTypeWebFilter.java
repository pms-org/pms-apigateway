package com.example.apigateway.filter;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;

@Component
public class TokenTypeWebFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        return exchange.getPrincipal()
                .cast(JwtAuthenticationToken.class)
                .flatMap(auth -> {

                    String tokenType =
                            auth.getToken().getClaimAsString("token_type");

                    String path =
                            exchange.getRequest().getURI().getPath();

                    // 🔒 INTERNAL APIs → SERVICE token ONLY
                    if (path.startsWith("/simulation")
                            && !"SERVICE".equals(tokenType)) {
                        return forbidden(exchange);
                    }

                    // 🔒 USER APIs → USER token ONLY
                    if (path.startsWith("/analytics")
                            && !"USER".equals(tokenType)) {
                        return forbidden(exchange);
                    }

                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
