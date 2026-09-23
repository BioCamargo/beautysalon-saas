package com.beautysalon.dto.rest;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class FinanceiroRestDTO {

    public record AbrirCaixaRequest(
            @NotNull(message = "O saldo inicial de abertura é obrigatório")
            @DecimalMin(value = "0.0", inclusive = true, message = "O saldo inicial não pode ser negativo")
            BigDecimal saldoInicial,

            String observacoes
    ) {}

    public record FecharCaixaRequest(
            @NotNull(message = "O saldo em dinheiro contado fisicamente é obrigatório")
            @DecimalMin(value = "0.0", inclusive = true, message = "O saldo contado não pode ser negativo")
            BigDecimal saldoDinheiroContado,

            String observacoes
    ) {}

    public record CaixaResponse(
            Long id,
            LocalDateTime dataAbertura,
            LocalDateTime dataFechamento,
            BigDecimal saldoInicial,
            BigDecimal totalEntradas,
            BigDecimal totalSaidas,
            BigDecimal saldoFinalEsperado,
            BigDecimal saldoFinalContado,
            BigDecimal diferenca,
            String status,
            String operadorNome
    ) {}

    public record CriarComandaRequest(
            Long clienteId,
            String observacoes
    ) {}

    public record AdicionarItemComandaRequest(
            Long servicoId,
            Long produtoId,
            Long profissionalId,

            @NotNull(message = "A quantidade é obrigatória")
            @DecimalMin(value = "0.01", message = "A quantidade deve ser positiva")
            BigDecimal quantidade,

            @NotNull(message = "O valor unitário é obrigatório")
            BigDecimal valorUnitario,

            BigDecimal valorDesconto
    ) {}

    public record FecharComandaRequest(
            @NotNull(message = "A forma de pagamento é obrigatória (DINHEIRO, CARTAO_CREDITO, CARTAO_DEBITO, PIX, VOUCHER)")
            String formaPagamento,

            BigDecimal descontoGeral,
            BigDecimal acrescimoGeral,
            String cupomCodigo
    ) {}

    public record ComandaResponse(
            Long id,
            String numeroComanda,
            LocalDateTime dataHoraAbertura,
            LocalDateTime dataHoraFechamento,
            String status,
            String formaPagamento,
            BigDecimal valorSubtotal,
            BigDecimal valorDesconto,
            BigDecimal valorTotal,
            Long clienteId,
            String clienteNome,
            List<ComandaItemResponse> itens
    ) {}

    public record ComandaItemResponse(
            Long id,
            String tipoItem,
            String descricao,
            BigDecimal quantidade,
            BigDecimal valorUnitario,
            BigDecimal valorDesconto,
            BigDecimal valorTotal,
            Long profissionalId,
            String profissionalNome,
            BigDecimal valorComissao
    ) {}
}
