package com.icc.gateway.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;

import com.icc.gateway.util.JwtUtil;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    private AuthenticationFilter authenticationFilter;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        authenticationFilter = new AuthenticationFilter();
        ReflectionTestUtils.setField(authenticationFilter, "jwtUtil", jwtUtil);
    }

    @Test
    void shouldAllowRequestWithValidToken() {
        // Arrange
        String token = "valid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUsername(token)).thenReturn("testuser");
        when(jwtUtil.extractUserId(token)).thenReturn("123");
        when(chain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config())
                .filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(jwtUtil).validateToken(token);
        verify(chain).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldRejectRequestWithoutAuthorizationHeader() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config())
                .filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldRejectRequestWithInvalidTokenFormat() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "InvalidFormat token")
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config())
                .filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldRejectRequestWithInvalidToken() {
        // Arrange
        String token = "invalid.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtil.validateToken(token)).thenReturn(false);

        // Act
        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config())
                .filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil).validateToken(token);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }

    @Test
    void shouldHandleJwtValidationException() {
        // Arrange
        String token = "malformed.jwt.token";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        
        ServerWebExchange exchange = MockServerWebExchange.from(request);
        
        when(jwtUtil.validateToken(token)).thenThrow(new RuntimeException("JWT parsing error"));

        // Act
        Mono<Void> result = authenticationFilter.apply(new AuthenticationFilter.Config())
                .filter(exchange, chain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(jwtUtil).validateToken(token);
        verify(chain, never()).filter(any(ServerWebExchange.class));
    }
}