package com.beautysalon.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * Conversor universal de String para Integer que higieniza separadores de milhar e espaços.
 */
@Component
public class StringToIntegerConverter implements Converter<String, Integer> {

    @Override
    @Nullable
    public Integer convert(String source) {
        if (source == null) {
            return null;
        }

        String clean = source.trim();
        if (clean.isEmpty()) {
            return null;
        }

        // Se o usuário digitou "10,00" ou "10.00" para quantidade inteira, extrai a parte inteira
        if (clean.contains(",")) {
            clean = clean.split(",")[0].trim();
        } else if (clean.contains(".")) {
            // Se for ex: "1.000", remove o ponto de milhar; se for "10.0", pega 10
            if (clean.endsWith(".0") || clean.endsWith(".00")) {
                clean = clean.substring(0, clean.indexOf("."));
            } else {
                clean = clean.replace(".", "");
            }
        }

        clean = clean.replaceAll("[^0-9-]", "");
        if (clean.isEmpty() || "-".equals(clean)) {
            return null;
        }

        try {
            return Integer.valueOf(clean);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
