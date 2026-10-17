package com.beautysalon.Controller;

import com.beautysalon.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de Webhook para recepção de eventos e respostas interativas do WhatsApp.
 * Suporta Evolution API / Z-API e Meta Cloud API Oficial.
 */
@RestController
@RequestMapping("/api/v1/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppService whatsAppService;

    public WhatsAppWebhookController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    /**
     * Endpoint GET para verificação de webhook da Meta Cloud API (Handshake do Facebook).
     */
    @GetMapping
    public ResponseEntity<String> verificarMetaWebhook(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode) && challenge != null) {
            logger.info("Handshake do Webhook da Meta realizado com sucesso!");
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.ok("NEXI PRO WhatsApp Webhook Endpoint Ativo.");
    }

    /**
     * Endpoint POST para recebimento de mensagens e respostas de clientes via Evolution API e Meta API.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> receberEventoWebhook(@RequestBody Map<String, Object> payload) {
        logger.debug("Webhook WhatsApp recebido: {}", payload);

        try {
            // 1. Processamento para Evolution API
            String instanceName = payload.get("instance") != null ? String.valueOf(payload.get("instance")) : null;
            Object dataObj = payload.get("data");

            if (dataObj instanceof Map<?, ?> dataMap) {
                Object keyObj = dataMap.get("key");
                if (keyObj instanceof Map<?, ?> keyMap) {
                    Object fromMe = keyMap.get("fromMe");
                    if (Boolean.TRUE.equals(fromMe)) {
                        // Ignora mensagens enviadas pelo próprio salão
                        return ResponseEntity.ok(Map.of("status", "IGNORED_FROM_ME"));
                    }

                    String remoteJid = keyMap.get("remoteJid") != null ? String.valueOf(keyMap.get("remoteJid")) : "";
                    String telefone = remoteJid.split("@")[0];

                    // Extrai o texto da mensagem
                    String texto = extrairTextoEvolution(dataMap);
                    if (texto != null && !texto.isBlank()) {
                        logger.info("Mensagem recebida no Webhook de {} (Instância {}): {}", telefone, instanceName, texto);
                        boolean processado = whatsAppService.processarRespostaCliente(telefone, texto, instanceName);
                        return ResponseEntity.ok(Map.of("status", "SUCCESS", "processado", processado));
                    }
                }
            }

            // 2. Processamento para Meta Cloud API (se vier no padrão Meta)
            if (payload.containsKey("entry")) {
                // Estrutura Meta Webhook
                logger.debug("Processando evento Meta Cloud API...");
            }

        } catch (Exception e) {
            logger.error("Erro ao processar Webhook WhatsApp: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok(Map.of("status", "RECEIVED"));
    }

    private String extrairTextoEvolution(Map<?, ?> dataMap) {
        Object messageObj = dataMap.get("message");
        if (!(messageObj instanceof Map<?, ?> msgMap)) return null;

        // Texto simples
        if (msgMap.get("conversation") != null) {
            return String.valueOf(msgMap.get("conversation"));
        }

        // Texto estendido (com link ou formatação)
        if (msgMap.get("extendedTextMessage") instanceof Map<?, ?> extMap) {
            if (extMap.get("text") != null) {
                return String.valueOf(extMap.get("text"));
            }
        }

        // Resposta de Botão
        if (msgMap.get("buttonsResponseMessage") instanceof Map<?, ?> btnMap) {
            if (btnMap.get("selectedButtonId") != null) {
                return String.valueOf(btnMap.get("selectedButtonId"));
            }
            if (btnMap.get("selectedDisplayText") != null) {
                return String.valueOf(btnMap.get("selectedDisplayText"));
            }
        }

        // Resposta de Lista Interativa
        if (msgMap.get("listResponseMessage") instanceof Map<?, ?> listMap) {
            if (listMap.get("singleSelectReply") instanceof Map<?, ?> selectMap) {
                if (selectMap.get("selectedRowId") != null) {
                    return String.valueOf(selectMap.get("selectedRowId"));
                }
            }
        }

        return null;
    }
}
