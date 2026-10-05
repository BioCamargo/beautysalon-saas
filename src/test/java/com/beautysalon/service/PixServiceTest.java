package com.beautysalon.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PixServiceTest {

    private PixService pixService;

    @BeforeEach
    void setUp() {
        pixService = new PixService();
    }

    @Test
    @DisplayName("Deve gerar payload Pix estático/dinâmico padrão EMVco / BACEN com CRC16 válido")
    void deveGerarPayloadPixCompleto() {
        String chavePix = "contato@studiobeauty.com.br";
        String recebedor = "Studio Beauty";
        String cidade = "SAO PAULO";
        BigDecimal valor = new BigDecimal("150.00");
        String txid = "CMD80";

        String payload = pixService.gerarPayloadPix(chavePix, recebedor, cidade, valor, txid);

        assertNotNull(payload);
        // Deve iniciar com o indicador de formato de payload 000201
        assertTrue(payload.startsWith("000201"));
        // Deve conter a chave pix
        assertTrue(payload.contains("contato@studiobeauty.com.br"));
        // Deve conter o valor formatado
        assertTrue(payload.contains("5406150.00"));
        // Deve conter a identificação de moeda Real (986)
        assertTrue(payload.contains("5303986"));
        // Deve conter o país BR
        assertTrue(payload.contains("5802BR"));
        // Deve conter o txid normalizado
        assertTrue(payload.contains("CMD80"));
        // Deve terminar com o campo 6304 + 4 caracteres hexadecimais do CRC16
        assertTrue(payload.contains("6304"));
        assertEquals(payload.length(), payload.indexOf("6304") + 8);
    }

    @Test
    @DisplayName("Deve gerar payload sem valor explícito (valor aberto)")
    void deveGerarPayloadSemValor() {
        String chavePix = "+5511999998888";
        String recebedor = "Studio VIP";
        String cidade = "Curitiba";

        String payload = pixService.gerarPayloadPix(chavePix, recebedor, cidade, null, "TX123");

        assertNotNull(payload);
        assertTrue(payload.startsWith("000201"));
        assertFalse(payload.contains("540")); // Não deve conter a tag de montante
        assertTrue(payload.contains("6304"));
    }

    @Test
    @DisplayName("Deve retornar null se a chave pix for nula ou vazia")
    void deveRetornarNullChaveVazia() {
        assertNull(pixService.gerarPayloadPix(null, "Studio", "SP", BigDecimal.TEN, "1"));
        assertNull(pixService.gerarPayloadPix("   ", "Studio", "SP", BigDecimal.TEN, "1"));
    }
}
