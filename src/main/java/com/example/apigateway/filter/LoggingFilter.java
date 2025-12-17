package com.example.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String correlationId =
                request.getHeaders().getFirst(CorrelationIdFilter.CORRELATION_ID);

        log.info(
                "Incoming Request → method={}, path={}, correlationId={}",
                request.getMethod(),
                request.getURI().getPath(),
                correlationId
        );

        return chain.filter(exchange)
                .doOnSuccess(aVoid -> log.info(
                        "Outgoing Response → status={}, path={}, correlationId={}",
                        exchange.getResponse().getStatusCode(),
                        request.getURI().getPath(),
                        correlationId
                ));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
