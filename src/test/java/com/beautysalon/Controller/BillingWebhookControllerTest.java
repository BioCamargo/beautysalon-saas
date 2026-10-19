package com.beautysalon.Controller;

import com.beautysalon.DTO.billing.AsaasWebhookDTO;
import com.beautysalon.service.BillingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingWebhookControllerTest {

    @Mock
    private BillingService billingService;

    @InjectMocks
    private BillingWebhookController controller;

    @Test
    @DisplayName("Deve responder GET no status do webhook com HTTP 200")
    void deveResponderStatusGetComSucesso() {
        ResponseEntity<String> response = controller.status();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("NEXI PRO SaaS Billing Webhook Ativo"));
    }

    @Test
    @DisplayName("Deve processar webhook POST com sucesso e retornar HTTP 200")
    void deveProcessarWebhookPostComSucesso() {
        AsaasWebhookDTO dto = AsaasWebhookDTO.builder()
                .event("PAYMENT_CONFIRMED")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .customer("cus_001")
                        .build())
                .build();

        when(billingService.processarWebhook(eq("token-valido"), any(AsaasWebhookDTO.class))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = controller.receberWebhookAsaas("token-valido", dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertEquals(true, response.getBody().get("processed"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 401 UNAUTHORIZED quando token de webhook for inválido")
    void deveRetornar401QuandoTokenInvalido() {
        AsaasWebhookDTO dto = AsaasWebhookDTO.builder().event("PAYMENT_CONFIRMED").build();

        when(billingService.processarWebhook(eq("token-invalido"), any(AsaasWebhookDTO.class)))
                .thenThrow(new SecurityException("Token inválido"));

        ResponseEntity<Map<String, Object>> response = controller.receberWebhookAsaas("token-invalido", dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("ERROR", response.getBody().get("status"));
    }

    @Test
    @DisplayName("Deve retornar HTTP 500 INTERNAL_SERVER_ERROR quando ocorrer exceção inesperada")
    void deveRetornar500QuandoErroInterno() {
        AsaasWebhookDTO dto = AsaasWebhookDTO.builder().event("PAYMENT_CONFIRMED").build();

        when(billingService.processarWebhook(any(), any()))
                .thenThrow(new RuntimeException("Falha no banco de dados"));

        ResponseEntity<Map<String, Object>> response = controller.receberWebhookAsaas(null, dto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("ERROR", response.getBody().get("status"));
    }
}
