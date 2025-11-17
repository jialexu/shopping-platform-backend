package com.icc.account.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icc.account.TestSecurityConfig;
import com.icc.account.domain.User;
import com.icc.account.dto.AuthAccountRequest;
import com.icc.account.dto.RegisterRequest;
import com.icc.account.dto.UpdateRequest;
import com.icc.account.exception.UserAlreadyExistsException;
import com.icc.account.exception.UserNotFoundException;
import com.icc.account.service.AccountService;

@WebMvcTest(AccountController.class)
@Import(TestSecurityConfig.class)
@DisplayName("AccountController Unit Tests")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
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
    @DisplayName("POST /api/accounts - Should create user successfully")
    void testCreateAccountSuccess() throws Exception {
        // Arrange
        when(accountService.register(any(RegisterRequest.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(accountService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/accounts - Should return 409 when user already exists")
    void testCreateAccountConflict() throws Exception {
        // Arrange
        when(accountService.register(any(RegisterRequest.class)))
                .thenThrow(UserAlreadyExistsException.withEmail("test@example.com"));

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());

        verify(accountService, times(1)).register(any(RegisterRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/accounts/{id} - Should update user successfully")
    void testUpdateAccountSuccess() throws Exception {
        // Arrange
        testUser.setUsername("updateduser");
        when(accountService.update(eq(1L), any(UpdateRequest.class))).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(put("/api/accounts/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("updateduser"));

        verify(accountService, times(1)).update(eq(1L), any(UpdateRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/accounts/{id} - Should return 404 when user not found")
    void testUpdateAccountNotFound() throws Exception {
        // Arrange
        when(accountService.update(eq(999L), any(UpdateRequest.class)))
                .thenThrow(new UserNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(put("/api/accounts/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).update(eq(999L), any(UpdateRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/accounts/{id} - Should get user successfully")
    void testGetAccountSuccess() throws Exception {
        // Arrange
        when(accountService.findById(1L)).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(get("/api/accounts/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(accountService, times(1)).findById(1L);
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/accounts/{id} - Should return 404 when user not found")
    void testGetAccountNotFound() throws Exception {
        // Arrange
        when(accountService.findById(999L)).thenThrow(new UserNotFoundException(999L));

        // Act & Assert
        mockMvc.perform(get("/api/accounts/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(accountService, times(1)).findById(999L);
    }

    @Test
    @DisplayName("POST /api/accounts/_internal/auth - Should get auth details successfully")
    void testGetAuthDetailsSuccess() throws Exception {
        // Arrange
        AuthAccountRequest authRequest = new AuthAccountRequest("test@example.com");
        when(accountService.findByEmail("test@example.com")).thenReturn(testUser);

        // Act & Assert
        mockMvc.perform(post("/api/accounts/_internal/auth")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.passwordHash").value("encodedPassword"));

        verify(accountService, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("POST /api/accounts - Should return 400 for invalid email")
    void testCreateAccountInvalidEmail() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "invalid-email",
                "testuser",
                "password123",
                "123 Test St",
                "456 Bill St",
                "Credit Card"
        );

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("POST /api/accounts - Should return 400 for short password")
    void testCreateAccountShortPassword() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest(
                "test@example.com",
                "testuser",
                "short",
                "123 Test St",
                "456 Bill St",
                "Credit Card"
        );

        // Act & Assert
        mockMvc.perform(post("/api/accounts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(accountService, never()).register(any(RegisterRequest.class));
    }
}
