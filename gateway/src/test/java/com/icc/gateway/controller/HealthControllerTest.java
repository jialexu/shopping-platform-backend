package com.icc.gateway.controller;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class HealthControllerTest {

    @Autowired
    private HealthController healthController;

    @Test
    void shouldReturnHealthStatus() {
        ResponseEntity<Map<String, Object>> response = healthController.health();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("UP", body.get("status"));
        assertEquals("API Gateway", body.get("service"));
        assertEquals("1.0.0", body.get("version"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void shouldReturnRootInformation() {
        ResponseEntity<Map<String, String>> response = healthController.root();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Chuwa Shopping Platform API Gateway", body.get("message"));
        assertEquals("1.0.0", body.get("version"));
        assertEquals("Running", body.get("status"));
    }
}