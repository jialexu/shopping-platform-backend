package com.icc.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void passwordEncoder_ShouldReturnBCryptPasswordEncoder() {
        // Arrange
        PasswordConfig config = new PasswordConfig();

        // Act
        PasswordEncoder encoder = config.passwordEncoder();

        // Assert
        assertNotNull(encoder);
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void passwordEncoder_ShouldEncodeAndMatchPassword() {
        // Arrange
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String rawPassword = "testPassword123";

        // Act
        String encodedPassword = encoder.encode(rawPassword);
        boolean matches = encoder.matches(rawPassword, encodedPassword);

        // Assert
        assertNotNull(encodedPassword);
        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(matches);
    }

    @Test
    void passwordEncoder_ShouldNotMatchDifferentPasswords() {
        // Arrange
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        String rawPassword = "testPassword123";
        String wrongPassword = "wrongPassword456";

        // Act
        String encodedPassword = encoder.encode(rawPassword);
        boolean matches = encoder.matches(wrongPassword, encodedPassword);

        // Assert
        assertFalse(matches);
    }
}
