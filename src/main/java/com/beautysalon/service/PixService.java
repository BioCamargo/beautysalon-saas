package com.beautysalon.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Utilitário para geração de Payload Pix Estático/Dinâmico (BR Code / Padrão Banco Central do Brasil).
 * Não necessita de bibliotecas externas, garantindo alta performance e compatibilidade imediata.
 */
@Service
public class PixService {

    private static final String ID_PAYLOAD_FORMAT_INDICATOR = "00";
    private static final String ID_POINT_OF_INITIATION_METHOD = "01";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION = "26";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_GUI = "00";
    private static final String ID_MERCHANT_ACCOUNT_INFORMATION_KEY = "01";
    private static final String ID_MERCHANT_CATEGORY_CODE = "52";
    private static final String ID_TRANSACTION_CURRENCY = "53";
    private static final String ID_TRANSACTION_AMOUNT = "54";
    private static final String ID_COUNTRY_CODE = "58";
    private static final String ID_MERCHANT_NAME = "59";
    private static final String ID_MERCHANT_CITY = "60";
    private static final String ID_ADDITIONAL_DATA_FIELD_TEMPLATE = "62";
    private static final String ID_ADDITIONAL_DATA_FIELD_TEMPLATE_TXID = "05";
    private static final String ID_CRC16 = "63";

    /**
     * Gera a chave Pix Copia e Cola (Payload padrão EMVco / BACEN).
     *
     * @param chavePix     Chave Pix da empresa (CPF, CNPJ, E-mail, Telefone ou Aleatória)
     * @param nomeRecebedor Nome do Salão / Beneficiário (máx 25 caracteres)
     * @param cidade        Cidade do Salão (máx 15 caracteres)
     * @param valor         Valor da cobrança (opcional ou comanda.valorTotal)
     * @param txid          Identificador da transação (ex: comanda ID)
     * @return String contendo o código Pix Copia-e-Cola
     */
    public String gerarPayloadPix(String chavePix, String nomeRecebedor, String cidade, BigDecimal valor, String txid) {
        if (chavePix == null || chavePix.isBlank()) {
            return null;
        }

        String chaveNormalizada = chavePix.trim();
        String nomeNormalizado = formatarTexto(nomeRecebedor != null ? nomeRecebedor : "BEAUTY SALON", 25);
        String cidadeNormalizada = formatarTexto(cidade != null ? cidade : "BRASIL", 15);
        String txidNormalizado = (txid != null && !txid.isBlank()) ? txid.replaceAll("[^a-zA-Z0-9]", "") : "***";
        if (txidNormalizado.length() > 25) {
            txidNormalizado = txidNormalizado.substring(0, 25);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(formatarCampo(ID_PAYLOAD_FORMAT_INDICATOR, "01"));
        sb.append(formatarCampo(ID_POINT_OF_INITIATION_METHOD, "12")); // 12 = Dinâmico/Reutilizável ou 11 = Estático

        // Merchant Account Information (GUI + Chave)
        String gui = formatarCampo(ID_MERCHANT_ACCOUNT_INFORMATION_GUI, "br.gov.bcb.pix");
        String key = formatarCampo(ID_MERCHANT_ACCOUNT_INFORMATION_KEY, chaveNormalizada);
        sb.append(formatarCampo(ID_MERCHANT_ACCOUNT_INFORMATION, gui + key));

        sb.append(formatarCampo(ID_MERCHANT_CATEGORY_CODE, "0000"));
        sb.append(formatarCampo(ID_TRANSACTION_CURRENCY, "986")); // Real BRL

        if (valor != null && valor.compareTo(BigDecimal.ZERO) > 0) {
            sb.append(formatarCampo(ID_TRANSACTION_AMOUNT, String.format(java.util.Locale.US, "%.2f", valor)));
        }

        sb.append(formatarCampo(ID_COUNTRY_CODE, "BR"));
        sb.append(formatarCampo(ID_MERCHANT_NAME, nomeNormalizado));
        sb.append(formatarCampo(ID_MERCHANT_CITY, cidadeNormalizada));

        // Additional Data Field (TXID)
        String additionalData = formatarCampo(ID_ADDITIONAL_DATA_FIELD_TEMPLATE_TXID, txidNormalizado);
        sb.append(formatarCampo(ID_ADDITIONAL_DATA_FIELD_TEMPLATE, additionalData));

        // CRC16 Checksum
        sb.append(ID_CRC16).append("04");
        String crc = calcularCRC16(sb.toString());
        sb.append(crc);

        return sb.toString();
    }

    private String formatarCampo(String id, String valor) {
        return id + String.format("%02d", valor.length()) + valor;
    }

    private String formatarTexto(String texto, int maxLen) {
        String semAcentos = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        semAcentos = semAcentos.replaceAll("[^a-zA-Z0-9 ]", "").toUpperCase();
        if (semAcentos.length() > maxLen) {
            return semAcentos.substring(0, maxLen);
        }
        return semAcentos.isBlank() ? "STUDIO" : semAcentos;
    }

    private String calcularCRC16(String payload) {
        int crc = 0xFFFF;
        int polynomial = 0x1021;
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);

        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }
        crc &= 0xFFFF;
        return String.format("%04X", crc);
    }
}
