package com.icc.account.web;

import com.icc.account.domain.User;
import com.icc.account.dto.*;
import com.icc.account.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account Management", description = "APIs for user account management")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);
    
    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(summary = "Register a new user account", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Account created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody RegisterRequest req) {
        log.info("Creating new account for email: {}", req.email());
        User user = service.register(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.from(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user account", description = "Updates an existing user account information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<AccountResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRequest req) {
        log.info("Updating account for id: {}", id);
        User user = service.update(id, req);
        return ResponseEntity.ok(AccountResponse.from(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user account", description = "Retrieves user account information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AccountResponse> get(@PathVariable Long id) {
        log.info("Getting account for id: {}", id);
        User user = service.findById(id);
        return ResponseEntity.ok(AccountResponse.from(user));
    }

    // ==== Internal endpoint for Auth Service ====
    @PostMapping("/_internal/auth")
    @Operation(summary = "Internal: Get user auth details", 
               description = "Internal endpoint for Auth Service to retrieve user authentication details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AuthAccountDto> findByEmailForAuth(@Valid @RequestBody AuthAccountRequest req) {
        log.info("Auth service requesting user details for email: {}", req.email());
        User user = service.findByEmail(req.email());
        AuthAccountDto dto = new AuthAccountDto(user.getId(), user.getEmail(), user.getPasswordHash());
        return ResponseEntity.ok(dto);
    }
}
