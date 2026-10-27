package com.beautysalon.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utilitário central de normalização inteligente de textos e entradas de usuários.
 * Garante padronização elegante de nomes (Title Case), e-mails, telefones e documentos.
 */
public final class TextNormalizationUtil {

    private static final Set<String> LOWERCASE_WORDS = new HashSet<>(Arrays.asList(
            "de", "da", "do", "das", "dos", "e", "em", "com", "para", "por", "a", "o", "as", "os"
    ));

    private static final Pattern MULTI_SPACE_PATTERN = Pattern.compile("\\s+");

    private TextNormalizationUtil() {
    }

    /**
     * Converte um nome ou título para formato Title Case inteligente em português.
     * Ex: "MARIA CLARA DA SILVA" -> "Maria Clara da Silva"
     * Ex: "corte e escova progressiva" -> "Corte e Escova Progressiva"
     */
    public static String capitalizeTitle(String text) {
        if (text == null) {
            return null;
        }
        String clean = MULTI_SPACE_PATTERN.matcher(text.trim()).replaceAll(" ");
        if (clean.isEmpty()) {
            return clean;
        }

        String[] words = clean.split(" ");
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            if (word.isEmpty()) {
                continue;
            }

            String lower = word.toLowerCase();
            // A primeira palavra é sempre maiúscula. As demais mantêm preposições minúsculas.
            if (i > 0 && LOWERCASE_WORDS.contains(lower)) {
                sb.append(lower);
            } else {
                sb.append(Character.toUpperCase(lower.charAt(0)));
                if (lower.length() > 1) {
                    sb.append(lower.substring(1));
                }
            }

            if (i < words.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * Normaliza e-mail removendo espaços e convertendo para minúsculo.
     * Ex: " Maria.Silva@Gmail.Com " -> "maria.silva@gmail.com"
     */
    public static String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String clean = email.trim().toLowerCase();
        return clean.isEmpty() ? null : clean;
    }

    /**
     * Extrai apenas dígitos numéricos de documentos ou telefones.
     * Ex: "(11) 98765-4321" -> "11987654321"
     * Ex: "123.456.789-00" -> "12345678900"
     */
    public static String cleanDigits(String text) {
        if (text == null) {
            return null;
        }
        String digits = text.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    /**
     * Normaliza telefone para formato numérico limpo (removendo DDI 55 acidental se presente).
     * Ex: "(11) 98765-4321" -> "11987654321"
     */
    public static String normalizePhone(String phone) {
        if (phone == null) {
            return null;
        }
        String digits = cleanDigits(phone);
        if (digits == null) {
            return null;
        }
        if (digits.startsWith("55") && (digits.length() == 12 || digits.length() == 13)) {
            digits = digits.substring(2);
        }
        return digits;
    }

    /**
     * Normaliza códigos, siglas, cupons e chaves (maiúsculas e sem espaços extras).
     * Ex: " cupom10 " -> "CUPOM10"
     */
    public static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String clean = code.trim().replaceAll("\\s+", "").toUpperCase();
        return clean.isEmpty() ? null : clean;
    }
}
