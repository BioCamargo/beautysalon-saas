package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Item individual pertencente a uma comanda (pode ser Serviço ou Produto de revenda).
 */
@Entity
@Table(name = "comanda_itens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"comanda", "profissional", "servico", "produto"})
@EqualsAndHashCode(of = "id")
public class ComandaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comanda_id", nullable = false)
    private Comanda comanda;

    @Column(nullable = false)
    private String tipo; // SERVICO, PRODUTO

    private String descricaoItem; // Nome do serviço ou produto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id")
    private User profissional; // Profissional que executou ou vendeu

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal precoUnitario = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal percentualComissao = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal valorComissao = BigDecimal.ZERO;

    public void recalcularLinha() {
        if (quantidade == null) quantidade = 1;
        if (precoUnitario == null) precoUnitario = BigDecimal.ZERO;
        this.valorTotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade));

        if (percentualComissao != null && percentualComissao.compareTo(BigDecimal.ZERO) > 0) {
            this.valorComissao = this.valorTotal.multiply(percentualComissao)
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.valorComissao = BigDecimal.ZERO;
        }
    }
}
