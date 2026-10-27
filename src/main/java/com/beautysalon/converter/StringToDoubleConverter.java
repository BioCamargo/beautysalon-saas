package com.beautysalon.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Conversor universal de String para Double com suporte a vírgulas decimais brasileiras.
 */
@Component
public class StringToDoubleConverter implements Converter<String, Double> {

    private final StringToBigDecimalConverter bigDecimalConverter = new StringToBigDecimalConverter();

    @Override
    @Nullable
    public Double convert(String source) {
        BigDecimal bd = bigDecimalConverter.convert(source);
        return bd != null ? bd.doubleValue() : null;
    }
}
