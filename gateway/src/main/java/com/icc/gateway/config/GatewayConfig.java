package com.icc.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.icc.gateway.filter.AuthenticationFilter;

@Configuration
public class GatewayConfig {

    @Autowired
    private AuthenticationFilter authenticationFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Auth service - no authentication required
                .route("auth", r -> r.path("/api/auth/**")
                        .uri("http://auth-service:9000"))
                
                // Account service - authentication required for certain endpoints
                .route("account-public", r -> r.path("/api/accounts/_internal/**")
                        .or().path("/api/accounts").and().method("POST")
                        .uri("http://account-service:9001"))
                .route("account-protected", r -> r.path("/api/accounts/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://account-service:9001"))
                
                // Item service - read operations public, write operations protected
                .route("item-public", r -> r.path("/api/items/**")
                        .and().method("GET")
                        .uri("http://item-service:9002"))
                .route("item-protected", r -> r.path("/api/items/**")
                        .and().method("POST", "PUT", "DELETE")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://item-service:9002"))
                
                // Inventory service - authentication required
                .route("inventory", r -> r.path("/api/inventory/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://inventory-service:9003"))
                
                // Order service - authentication required
                .route("order", r -> r.path("/api/orders/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://order-service:9004"))
                
                // Payment service - authentication required
                .route("payment", r -> r.path("/api/payments/**")
                        .filters(f -> f.filter(authenticationFilter.apply(new AuthenticationFilter.Config())))
                        .uri("http://payment-service:9005"))
                
                .build();
    }
}