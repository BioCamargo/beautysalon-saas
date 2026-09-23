package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Caixa diário da empresa (abertura, movimentação e fechamento).
 */
@Entity
@Table(name = "caixas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "operadorAbertura", "operadorFechamento", "comandas"})
@EqualsAndHashCode(of = "id")
public class Caixa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dataAbertura;

    private LocalDateTime dataFechamento;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal saldoInicial = BigDecimal.ZERO; // Fundo de troco inicial

    @Builder.Default
    private BigDecimal totalEntradas = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalSaidas = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal saldoFinalEsperado = BigDecimal.ZERO;

    private BigDecimal saldoFinalContado; // Informado no fechamento

    private BigDecimal diferencaFechamento; // Divergência (sobra ou falta)

    @Column(nullable = false)
    @Builder.Default
    private String status = "ABERTO"; // ABERTO, FECHADO

    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_abertura_id")
    private User operadorAbertura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operador_fechamento_id")
    private User operadorFechamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"caixas"})
    private Empresa empresa;

    @OneToMany(mappedBy = "caixa", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Comanda> comandas = new ArrayList<>();
}
