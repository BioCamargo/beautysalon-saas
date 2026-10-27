package com.beautysalon.Controller;

import com.beautysalon.converter.StringToBigDecimalConverter;
import com.beautysalon.converter.StringToIntegerConverter;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;

/**
 * Advice global que intercepta todo binding de formulários e parâmetros HTTP (@ModelAttribute e @RequestParam)
 * para aceitar números decimais com vírgula brasileira (ex: "6,90", "1.250,50", "R$ 6,90")
 * sem gerar erros de MethodArgumentTypeMismatchException ou BindException.
 */
@ControllerAdvice
public class GlobalDataBinderAdvice {

    private final StringToBigDecimalConverter bigDecimalConverter = new StringToBigDecimalConverter();
    private final StringToIntegerConverter integerConverter = new StringToIntegerConverter();

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Trimming automático em todos os campos String e conversão de strings vazias para null
        binder.registerCustomEditor(String.class, new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));

        // Suporte a BigDecimal em formulários e parâmetros
        binder.registerCustomEditor(BigDecimal.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(bigDecimalConverter.convert(text));
            }

            @Override
            public String getAsText() {
                BigDecimal value = (BigDecimal) getValue();
                return value != null ? value.toPlainString() : "";
            }
        });

        // Suporte a Double em formulários e parâmetros
        binder.registerCustomEditor(Double.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                BigDecimal bd = bigDecimalConverter.convert(text);
                setValue(bd != null ? bd.doubleValue() : null);
            }

            @Override
            public String getAsText() {
                Double value = (Double) getValue();
                return value != null ? value.toString() : "";
            }
        });

        // Suporte a Float em formulários e parâmetros
        binder.registerCustomEditor(Float.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                BigDecimal bd = bigDecimalConverter.convert(text);
                setValue(bd != null ? bd.floatValue() : null);
            }

            @Override
            public String getAsText() {
                Float value = (Float) getValue();
                return value != null ? value.toString() : "";
            }
        });

        // Suporte a Integer (quantidades, estoque) higienizado
        binder.registerCustomEditor(Integer.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(integerConverter.convert(text));
            }

            @Override
            public String getAsText() {
                Integer value = (Integer) getValue();
                return value != null ? value.toString() : "";
            }
        });
    }
}
