package com.icc.account.service;

import com.icc.account.domain.User;
import com.icc.account.domain.UserRepository;
import com.icc.account.dto.RegisterRequest;
import com.icc.account.dto.UpdateRequest;
import com.icc.account.exception.UserAlreadyExistsException;
import com.icc.account.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);
    
    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    public User register(RegisterRequest req) {
        log.info("Attempting to register user with email: {}", req.email());
        
        // Check if user already exists
        if (repo.findByEmail(req.email()).isPresent()) {
            log.warn("Registration failed: User already exists with email: {}", req.email());
            throw new UserAlreadyExistsException("User already exists with email: " + req.email());
        }
        
        User user = new User();
        user.setEmail(req.email());
        user.setUsername(req.username());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setShippingAddress(req.shippingAddress());
        user.setBillingAddress(req.billingAddress());
        user.setPaymentMethod(req.paymentMethod());
        
        User saved = repo.save(user);
        log.info("Successfully registered user with id: {}", saved.getId());
        return saved;
    }

    public User update(Long id, UpdateRequest req) {
        log.info("Attempting to update user with id: {}", id);
        
        User user = repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        if (req.username() != null && !req.username().isBlank()) {
            user.setUsername(req.username());
        }
        if (req.shippingAddress() != null) {
            user.setShippingAddress(req.shippingAddress());
        }
        if (req.billingAddress() != null) {
            user.setBillingAddress(req.billingAddress());
        }
        if (req.paymentMethod() != null) {
            user.setPaymentMethod(req.paymentMethod());
        }
        
        User updated = repo.save(user);
        log.info("Successfully updated user with id: {}", id);
        return updated;
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        log.debug("Finding user by id: {}", id);
        return repo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return repo.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
    }
}
