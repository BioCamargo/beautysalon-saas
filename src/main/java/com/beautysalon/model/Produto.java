package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Produto para Revenda ou Consumo interno no Salão.
 */
@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa"})
@EqualsAndHashCode(of = "id")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String codigoBarras;

    private String marca;

    private String categoria;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TipoProduto tipo = TipoProduto.REVENDA;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal precoCusto = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal precoVenda = BigDecimal.ZERO; // Para produtos de revenda

    @Builder.Default
    private Integer quantidadeEstoque = 0;

    @Builder.Default
    private Integer estoqueMinimo = 5; // Limite para alerta de estoque baixo

    private String unidadeMedida; // UN, ML, G, CX, etc.

    @Builder.Default
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"produtos"})
    private Empresa empresa;

    /**
     * Calcula se o produto está com estoque crítico ou abaixo do mínimo.
     */
    public boolean isEstoqueBaixo() {
        return quantidadeEstoque != null && estoqueMinimo != null && quantidadeEstoque <= estoqueMinimo;
    }

    /**
     * Valor financeiro total parado deste item em estoque (Base de custo).
     */
    public BigDecimal getValorParadoEstoque() {
        if (precoCusto == null || quantidadeEstoque == null) return BigDecimal.ZERO;
        return precoCusto.multiply(BigDecimal.valueOf(quantidadeEstoque));
    }
}
