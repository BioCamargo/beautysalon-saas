package com.beautysalon.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StringToLocalDateConverter implements Converter<String, LocalDate> {

    private static final DateTimeFormatter BR_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter BR_SHORT_FORMATTER = DateTimeFormatter.ofPattern("d/M/yyyy");

    @Override
    public LocalDate convert(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        String clean = source.trim();
        // Formato ISO padrão de <input type="date"> (yyyy-MM-dd)
        if (clean.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return LocalDate.parse(clean, DateTimeFormatter.ISO_LOCAL_DATE);
        }
        // Formato brasileiro (dd/MM/yyyy ou d/M/yyyy)
        if (clean.contains("/")) {
            try {
                return LocalDate.parse(clean, BR_FORMATTER);
            } catch (Exception e) {
                return LocalDate.parse(clean, BR_SHORT_FORMATTER);
            }
        }
        return LocalDate.parse(clean);
    }
}
