package com.beautysalon.Controller;

import com.beautysalon.DTO.billing.AsaasWebhookDTO;
import com.beautysalon.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de Webhook para recepção de eventos de faturamento e assinaturas SaaS (Asaas Gateway).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks/billing")
@RequiredArgsConstructor
public class BillingWebhookController {

    private final BillingService billingService;

    @GetMapping
    public ResponseEntity<String> status() {
        return ResponseEntity.ok("NEXI PRO SaaS Billing Webhook Ativo.");
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> receberWebhookAsaas(
            @RequestHeader(value = "asaas-access-token", required = false) String accessToken,
            @RequestBody AsaasWebhookDTO webhook) {

        try {
            boolean processado = billingService.processarWebhook(accessToken, webhook);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "processed", processado
            ));
        } catch (SecurityException se) {
            log.warn("Tentativa de acesso não autorizada ao webhook Asaas: {}", se.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "status", "ERROR",
                    "message", "Token de webhook não autorizado"
            ));
        } catch (Exception e) {
            log.error("Erro interno ao processar webhook Asaas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage() != null ? e.getMessage() : "Erro desconhecido"
            ));
        }
    }
}
