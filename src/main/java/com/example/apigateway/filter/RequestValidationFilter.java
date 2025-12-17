package com.example.apigateway.filter;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RequestValidationFilter implements GlobalFilter, Ordered {

    private static final String API_KEY = "secret123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //Validate API Key header
        String apiKey = exchange.getRequest().getHeaders().getFirst("X-API-KEY");
        if (apiKey == null || !apiKey.equals(API_KEY)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        //Validate Content-Type
        String contentType = exchange.getRequest().getHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.contains("application/json")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
            return exchange.getResponse().setComplete();
        }



        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -3;
    }
}
