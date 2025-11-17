package com.icc.gateway.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsConfigurationSource;

@SpringBootTest
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Test
    void shouldLoadCorsConfiguration() {
        assertNotNull(corsConfigurationSource);
    }

    @Test
    void contextLoads() {
        // This test ensures that the security context loads successfully
    }
}