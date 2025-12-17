package com.example.apigateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {

    @RequestMapping("/fallback")
    public Mono<String> fallback() {
        return Mono.just("{\"status\":\"failed\",\"message\":\"Portfolio Service is unavailable. Please try later.\"}");
    }
}

