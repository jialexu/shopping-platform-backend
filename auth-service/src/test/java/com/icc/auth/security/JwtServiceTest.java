package com.icc.auth.security;

import javax.crypto.SecretKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "test-secret-key-that-is-long-enough-for-hs256-algorithm-minimum-256-bits";

    @BeforeEach
    void setUp() {
        // 生成符合 RFC 7518 的安全密钥（自动满足 256-bit 要求）
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        jwtService = new JwtService(
            Encoders.BASE64.encode(key.getEncoded()), 
            3600000L
        );
    }


    @Test
    void generateToken_ShouldReturnValidToken() {
        // Arrange
        Long userId = 1L;
        String email = "test@example.com";

        // Act
        String token = jwtService.generateToken(userId, email);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void parse_WithValidToken_ShouldReturnClaims() {
        // Arrange
        Long userId = 1L;
        String email = "test@example.com";
        String token = jwtService.generateToken(userId, email);

        // Act
        Jws<Claims> parsedToken = jwtService.parse(token);

        // Assert
        assertNotNull(parsedToken);
        assertEquals(String.valueOf(userId), parsedToken.getBody().getSubject());
        assertEquals(email, parsedToken.getBody().get("email"));
    }

    @Test
    void parse_WithInvalidToken_ShouldThrowException() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.parse(invalidToken));
    }

    @Test
    void generateToken_WithDifferentUsers_ShouldGenerateDifferentTokens() {
        // Arrange
        Long userId1 = 1L;
        String email1 = "user1@example.com";
        Long userId2 = 2L;
        String email2 = "user2@example.com";

        // Act
        String token1 = jwtService.generateToken(userId1, email1);
        String token2 = jwtService.generateToken(userId2, email2);

        // Assert
        assertNotEquals(token1, token2);
    }

    @Test
    void parse_TokenGeneratedByService_ShouldExtractCorrectUserId() {
        // Arrange
        Long expectedUserId = 123L;
        String email = "user@example.com";
        String token = jwtService.generateToken(expectedUserId, email);

        // Act
        Jws<Claims> parsedToken = jwtService.parse(token);
        Long actualUserId = Long.valueOf(parsedToken.getBody().getSubject());

        // Assert
        assertEquals(expectedUserId, actualUserId);
    }
}
