package com.icc.auth.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "account-service",
        url = "http://account-service:9001",
        path = "/api/accounts"
)
public interface AccountClient {

    @PostMapping("/_internal/auth")
    AuthAccountDto findByEmailForAuth(@RequestBody AuthAccountRequest request);

    record AuthAccountRequest(String email) {}
    record AuthAccountDto(Long id, String email, String passwordHash) {}
}
