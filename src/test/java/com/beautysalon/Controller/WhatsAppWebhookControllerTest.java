package com.beautysalon.Controller;

import com.beautysalon.service.WhatsAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppWebhookControllerTest {

    @Mock
    private WhatsAppService whatsAppService;

    @InjectMocks
    private WhatsAppWebhookController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Deve responder ao handshake do Webhook da Meta com o challenge enviado")
    void deveResponderAoHandshakeMeta() {
        ResponseEntity<String> response = controller.verificarMetaWebhook("subscribe", "token123", "challenge_abc_123");

        assertEquals(200, response.getStatusCode().value());
        assertEquals("challenge_abc_123", response.getBody());
    }

    @Test
    @DisplayName("Deve processar mensagem de texto '1' (Confirmação) recebida via Evolution API")
    void deveProcessarMensagemTextoEvolution() {
        when(whatsAppService.processarRespostaCliente(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, Object> payload = Map.of(
                "event", "messages.upsert",
                "instance", "studio-principal",
                "data", Map.of(
                        "key", Map.of(
                                "remoteJid", "5511999998888@s.whatsapp.net",
                                "fromMe", false
                        ),
                        "message", Map.of(
                                "conversation", "1"
                        )
                )
        );

        ResponseEntity<Map<String, Object>> response = controller.receberEventoWebhook(payload);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("SUCCESS", response.getBody().get("status"));
        assertEquals(true, response.getBody().get("processado"));

        verify(whatsAppService, times(1)).processarRespostaCliente("5511999998888", "1", "studio-principal");
    }

    @Test
    @DisplayName("Deve ignorar mensagens enviadas pelo próprio salão (fromMe == true)")
    void deveIgnorarMensagensFromMe() {
        Map<String, Object> payload = Map.of(
                "event", "messages.upsert",
                "instance", "studio-principal",
                "data", Map.of(
                        "key", Map.of(
                                "remoteJid", "5511999998888@s.whatsapp.net",
                                "fromMe", true
                        ),
                        "message", Map.of(
                                "conversation", "Lembrete enviado"
                        )
                )
        );

        ResponseEntity<Map<String, Object>> response = controller.receberEventoWebhook(payload);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("IGNORED_FROM_ME", response.getBody().get("status"));
        verify(whatsAppService, never()).processarRespostaCliente(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Deve processar clique em botão interativo da Evolution API")
    void deveProcessarBotaoInterativoEvolution() {
        when(whatsAppService.processarRespostaCliente(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, Object> payload = Map.of(
                "event", "messages.upsert",
                "instance", "studio-vip",
                "data", Map.of(
                        "key", Map.of(
                                "remoteJid", "5511988887777@s.whatsapp.net",
                                "fromMe", false
                        ),
                        "message", Map.of(
                                "buttonsResponseMessage", Map.of(
                                        "selectedButtonId", "1",
                                        "selectedDisplayText", "Confirmar Presença"
                                )
                        )
                )
        );

        ResponseEntity<Map<String, Object>> response = controller.receberEventoWebhook(payload);

        assertEquals(200, response.getStatusCode().value());
        verify(whatsAppService, times(1)).processarRespostaCliente("5511988887777", "1", "studio-vip");
    }
}
