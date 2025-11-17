package com.icc.gateway.filter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String method = request.getMethod().toString();
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String clientIp = getClientIpAddress(request);
        
        logger.info("[{}] {} {} {} from {}", 
                timestamp, method, path, query != null ? "?" + query : "", clientIp);
        
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    logger.info("[{}] Response: {} for {} {}", 
                            timestamp, response.getStatusCode(), method, path);
                })
        );
    }

    @SuppressWarnings("null")
    private String getClientIpAddress(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        try {
            if (request.getRemoteAddress() != null) {
                var address = request.getRemoteAddress().getAddress();
                if (address != null) {
                    return address.getHostAddress();
                }
            }
        } catch (Exception e) {
            // Ignore and return unknown
        }
        
        return "unknown";
    }

    @Override
    public int getOrder() {
        return -1; // High priority
    }
}