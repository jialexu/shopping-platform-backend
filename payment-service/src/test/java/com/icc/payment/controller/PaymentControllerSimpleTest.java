package com.icc.payment.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icc.payment.dto.PaymentRequest;
import com.icc.payment.dto.PaymentResponse;
import com.icc.payment.entity.PaymentStatus;
import com.icc.payment.service.PaymentService;

@WebMvcTest(controllers = PaymentController.class)
@TestPropertySource(properties = {
    "jwt.secret=test-secret-key-for-testing-purposes-only",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"
})
class PaymentControllerSimpleTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    private PaymentResponse paymentResponse;

    @Test
    void createPayment_Success() throws Exception {
        // Given
        paymentResponse = new PaymentResponse();
        paymentResponse.setId(1L);
        paymentResponse.setOrderId("ORDER-001");
        paymentResponse.setAmount(new BigDecimal("100.00"));
        paymentResponse.setStatus(PaymentStatus.PENDING);
        paymentResponse.setPaymentMethod("CREDIT_CARD");
        paymentResponse.setCreatedAt(LocalDateTime.now());
        paymentResponse.setUpdatedAt(LocalDateTime.now());
        
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("ORDER-001");
        request.setAmount(new BigDecimal("100.00"));
        request.setPaymentMethod("CREDIT_CARD");

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        // When & Then
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.orderId").value("ORDER-001"))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createPayment_InvalidRequest() throws Exception {
        // Given
        PaymentRequest request = new PaymentRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}