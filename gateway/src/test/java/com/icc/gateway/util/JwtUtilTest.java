package com.icc.gateway.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKey123456789012345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void shouldGenerateToken() {
        String username = "testuser";
        String userId = "123";
        
        String token = jwtUtil.generateToken(username, userId);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void shouldExtractUsername() {
        String username = "testuser";
        String userId = "123";
        String token = jwtUtil.generateToken(username, userId);
        
        String extractedUsername = jwtUtil.extractUsername(token);
        
        assertEquals(username, extractedUsername);
    }

    @Test
    void shouldExtractUserId() {
        String username = "testuser";
        String userId = "123";
        String token = jwtUtil.generateToken(username, userId);
        
        String extractedUserId = jwtUtil.extractUserId(token);
        
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldValidateValidToken() {
        String username = "testuser";
        String userId = "123";
        String token = jwtUtil.generateToken(username, userId);
        
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";
        
        assertFalse(jwtUtil.validateToken(invalidToken));
    }

    @Test
    void shouldRejectExpiredToken() {
        // Set a very short expiration for testing
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1L);
        
        String username = "testuser";
        String userId = "123";
        String token = jwtUtil.generateToken(username, userId);
        
        // Reset expiration to normal
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
        
        assertFalse(jwtUtil.validateToken(token));
    }
}