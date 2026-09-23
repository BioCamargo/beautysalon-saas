package com.beautysalon.dto.rest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProdutoRestDTO {

    public record Request(
            @NotBlank(message = "O nome do produto é obrigatório")
            String nome,

            String codigoBarras,

            String categoria,

            String tipo, // REVENDA ou CONSUMO_INTERNO

            @NotNull(message = "O preço de custo é obrigatório")
            @DecimalMin(value = "0.0", inclusive = true, message = "O custo não pode ser negativo")
            BigDecimal precoCusto,

            @NotNull(message = "O preço de venda é obrigatório")
            @DecimalMin(value = "0.0", inclusive = true, message = "O preço de venda não pode ser negativo")
            BigDecimal precoVenda,

            @NotNull(message = "A quantidade em estoque é obrigatória")
            BigDecimal quantidadeEstoque,

            BigDecimal estoqueMinimo,

            String unidadeMedida
    ) {}

    public record Response(
            Long id,
            String nome,
            String codigoBarras,
            String categoria,
            String tipo,
            BigDecimal precoCusto,
            BigDecimal precoVenda,
            BigDecimal quantidadeEstoque,
            BigDecimal estoqueMinimo,
            String unidadeMedida,
            boolean estoqueBaixo
    ) {}

    public record MovimentacaoRequest(
            @NotNull(message = "O ID do produto é obrigatório")
            Long produtoId,

            @NotNull(message = "O tipo de movimentação é obrigatório (ENTRADA, SAIDA_AVULSA, CONSUMO_INTERNO, AJUSTE_BALANCO)")
            String tipo,

            @NotNull(message = "A quantidade é obrigatória")
            @DecimalMin(value = "0.001", message = "A quantidade deve ser maior que zero")
            BigDecimal quantidade,

            String motivo
    ) {}
}
