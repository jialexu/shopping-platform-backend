package com.icc.account.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.icc.account.domain.User;
import com.icc.account.domain.UserRepository;
import com.icc.account.dto.RegisterRequest;
import com.icc.account.dto.UpdateRequest;
import com.icc.account.exception.UserAlreadyExistsException;
import com.icc.account.exception.UserNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountService Unit Tests")
class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private RegisterRequest registerRequest;
    private UpdateRequest updateRequest;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setPasswordHash("encodedPassword");
        testUser.setShippingAddress("123 Test St");
        testUser.setBillingAddress("456 Bill St");
        testUser.setPaymentMethod("Credit Card");

        registerRequest = new RegisterRequest(
                "test@example.com",
                "testuser",
                "password123",
                "123 Test St",
                "456 Bill St",
                "Credit Card"
        );

        updateRequest = new UpdateRequest(
                "updateduser",
                "789 New St",
                "789 New Bill St",
                "Debit Card"
        );
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = accountService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when user already exists")
    void testRegisterUserAlreadyExists() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act & Assert
        UserAlreadyExistsException ex = assertThrows(UserAlreadyExistsException.class, () -> accountService.register(registerRequest));
        assertNotNull(ex);
        verify(userRepository, times(1)).findByEmail("test@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully")
    void testUpdateSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = accountService.update(1L, updateRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent user")
    void testUpdateUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> accountService.update(999L, updateRequest));
        assertNotNull(ex);
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should find user by id successfully")
    void testFindByIdSuccess() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        User result = accountService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found by id")
    void testFindByIdNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> accountService.findById(999L));
        assertNotNull(ex);
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    @DisplayName("Should find user by email successfully")
    void testFindByEmailSuccess() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // Act
        User result = accountService.findByEmail("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found by email")
    void testFindByEmailNotFound() {
        // Arrange
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, () -> accountService.findByEmail("notfound@example.com"));
        assertNotNull(ex);
        verify(userRepository, times(1)).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should handle partial update request")
    void testPartialUpdate() {
        // Arrange
        UpdateRequest partialRequest = new UpdateRequest(
                "newusername",
                null,
                null,
                null
        );
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        User result = accountService.update(1L, partialRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(testUser);
    }
}
