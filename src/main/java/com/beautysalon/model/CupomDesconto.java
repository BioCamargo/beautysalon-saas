package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cupons promocionais de desconto para fidelização.
 */
@Entity
@Table(name = "cupons_desconto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa"})
@EqualsAndHashCode(of = "id")
public class CupomDesconto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo; // Ex: PROMO10, ANIVERSARIO20, VOLTEI15

    private String descricao;

    @Column(nullable = false)
    private String tipoDesconto; // PERCENTUAL ou VALOR_FIXO

    @Column(nullable = false)
    private BigDecimal valorDesconto; // Ex: 15.00 (%) ou 20.00 (R$)

    private BigDecimal valorMinimoPedido;

    private LocalDate validadeInicio;

    private LocalDate validadeFim;

    @Builder.Default
    private Integer limiteUsos = 100;

    @Builder.Default
    private Integer usosAtuais = 0;

    @Builder.Default
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"cupons"})
    private Empresa empresa;

    public boolean isValido() {
        if (!ativo) return false;
        LocalDate hoje = LocalDate.now();
        if (validadeInicio != null && hoje.isBefore(validadeInicio)) return false;
        if (validadeFim != null && hoje.isAfter(validadeFim)) return false;
        if (limiteUsos != null && usosAtuais >= limiteUsos) return false;
        return true;
    }
}
