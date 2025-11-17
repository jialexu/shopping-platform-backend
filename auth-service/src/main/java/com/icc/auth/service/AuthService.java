package com.icc.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.icc.auth.client.AccountClient;
import com.icc.auth.dto.LoginRequest;
import com.icc.auth.dto.TokenResponse;
import com.icc.auth.exception.InvalidCredentialsException;
import com.icc.auth.security.JwtService;

@Service
public class AuthService {

    private final AccountClient accountClient;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AccountClient accountClient,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.accountClient = accountClient;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenResponse login(LoginRequest request) {
        var account = accountClient.findByEmailForAuth(
                new AccountClient.AuthAccountRequest(request.email()));

        if (account == null) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.password(), account.passwordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(account.id(), account.email());
        return new TokenResponse(token);
    }

    public boolean validateToken(String token) {
        try {
            jwtService.parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
