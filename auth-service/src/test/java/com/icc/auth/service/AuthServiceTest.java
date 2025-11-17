package com.icc.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.icc.auth.client.AccountClient;
import com.icc.auth.dto.LoginRequest;
import com.icc.auth.dto.TokenResponse;
import com.icc.auth.exception.InvalidCredentialsException;
import com.icc.auth.security.JwtService;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AccountClient accountClient;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private LoginRequest loginRequest;
    private AccountClient.AuthAccountDto accountDto;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("test@example.com", "password123");
        accountDto = new AccountClient.AuthAccountDto(1L, "test@example.com", "$2a$10$hashedPassword");
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() {
        // Arrange
        when(accountClient.findByEmailForAuth(any(AccountClient.AuthAccountRequest.class)))
                .thenReturn(accountDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("jwt-token");

        // Act
        TokenResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(accountClient).findByEmailForAuth(any(AccountClient.AuthAccountRequest.class));
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService).generateToken(1L, "test@example.com");
    }

    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        // Arrange
        when(accountClient.findByEmailForAuth(any(AccountClient.AuthAccountRequest.class)))
                .thenReturn(null);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(accountClient).findByEmailForAuth(any(AccountClient.AuthAccountRequest.class));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        // Arrange
        when(accountClient.findByEmailForAuth(any(AccountClient.AuthAccountRequest.class)))
                .thenReturn(accountDto);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
        verify(accountClient).findByEmailForAuth(any(AccountClient.AuthAccountRequest.class));
        verify(passwordEncoder).matches("password123", "$2a$10$hashedPassword");
        verify(jwtService, never()).generateToken(anyLong(), anyString());
    }

    @Test
    void validateToken_WithValidToken_ShouldReturnTrue() {
        // Arrange
        String token = "valid-token";
        when(jwtService.parse(token)).thenReturn(null); // Just need to not throw exception

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertTrue(result);
        verify(jwtService).parse(token);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldReturnFalse() {
        // Arrange
        String token = "invalid-token";
        when(jwtService.parse(token)).thenThrow(new RuntimeException("Invalid token"));

        // Act
        boolean result = authService.validateToken(token);

        // Assert
        assertFalse(result);
        verify(jwtService).parse(token);
    }
}
