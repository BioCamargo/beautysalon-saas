package com.beautysalon.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Conversor universal de String para BigDecimal.
 * Suporta formatos brasileiros (ex: "6,90", "1.250,50", "R$ 6,90")
 * e formatos internacionais (ex: "6.90", "1250.50"), eliminando erros de conversão de decimais.
 */
@Component
public class StringToBigDecimalConverter implements Converter<String, BigDecimal> {

    @Override
    @Nullable
    public BigDecimal convert(String source) {
        if (source == null) {
            return null;
        }

        String clean = source.trim();
        if (clean.isEmpty()) {
            return null;
        }

        // Remove símbolos de moeda, espaços normais e non-breaking spaces
        clean = clean.replace("R$", "")
                     .replace("r$", "")
                     .replace("$", "")
                     .replace("%", "")
                     .replace(" ", "")
                     .replace("\u00A0", "")
                     .trim();

        if (clean.isEmpty()) {
            return null;
        }

        // Caso 1: Formato brasileiro com separador de milhar e decimal (ex: 1.250,50)
        if (clean.contains(".") && clean.contains(",")) {
            clean = clean.replace(".", "").replace(",", ".");
        }
        // Caso 2: Formato com vírgula decimal (ex: 6,90 ou 1250,50)
        else if (clean.contains(",")) {
            clean = clean.replace(",", ".");
        }
        // Caso 3: Formato com ponto já decimal (ex: 6.90) ou número inteiro (ex: 10) -> mantém

        try {
            return new BigDecimal(clean).setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            // Filtra caracteres residuais não numéricos
            String digitsOnly = clean.replaceAll("[^0-9.-]", "");
            if (digitsOnly.isEmpty() || "-".equals(digitsOnly) || ".".equals(digitsOnly)) {
                return null;
            }
            try {
                return new BigDecimal(digitsOnly).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
