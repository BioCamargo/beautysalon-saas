package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Pacotes e Combos promocionais de serviços (ex: Cabelo + Barba + Hidratação com desconto).
 */
@Entity
@Table(name = "pacotes_combos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "servicos"})
@EqualsAndHashCode(of = "id")
public class PacoteCombo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome; // Ex: Combo Noivo Completo, Pacote Spa Day

    private String descricao;

    @Column(nullable = false)
    private BigDecimal precoPromocional; // Preço fechado do combo

    private BigDecimal precoOriginalSoma; // Soma dos preços originais dos itens

    @ManyToMany
    @JoinTable(
            name = "combo_servicos",
            joinColumns = @JoinColumn(name = "combo_id"),
            inverseJoinColumns = @JoinColumn(name = "servico_id")
    )
    @Builder.Default
    private List<Servico> servicos = new ArrayList<>();

    @Builder.Default
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"combos"})
    private Empresa empresa;

    public BigDecimal getEconomia() {
        if (precoOriginalSoma == null || precoPromocional == null) return BigDecimal.ZERO;
        BigDecimal diff = precoOriginalSoma.subtract(precoPromocional);
        return diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO;
    }
}
