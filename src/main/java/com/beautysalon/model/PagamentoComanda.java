package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representa uma parcela/fração de pagamento associada a uma comanda (Split de Pagamento).
 */
@Entity
@Table(name = "comanda_pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"comanda", "empresa"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class PagamentoComanda extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comanda_id", nullable = false)
    @JsonIgnoreProperties({"pagamentos", "itens"})
    private Comanda comanda;

    @Column(nullable = false)
    private String formaPagamento; // PIX, CARTAO_CREDITO, CARTAO_DEBITO, DINHEIRO, VOUCHER, OUTRO

    @Column(nullable = false)
    private BigDecimal valor;

    private String observacao; // Ex: "2x sem juros Visa", "Transferência via Nubank"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"comandas", "caixas"})
    private Empresa empresa;

    @Builder.Default
    private LocalDateTime dataHora = LocalDateTime.now();
}
