package com.beautysalon.config;

import com.beautysalon.converter.StringToBigDecimalConverter;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Desserializador Jackson global para BigDecimal.
 * Garante que qualquer requisição REST em formato JSON contendo decimais em string
 * brasileira (ex: "6,90") seja automaticamente convertida sem erro HTTP 400.
 */
@JsonComponent
public class BigDecimalJsonDeserializer extends JsonDeserializer<BigDecimal> {

    private final StringToBigDecimalConverter converter = new StringToBigDecimalConverter();

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        return converter.convert(text);
    }
}
