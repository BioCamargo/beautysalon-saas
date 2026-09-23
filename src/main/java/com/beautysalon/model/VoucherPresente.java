package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Cartão Presente / Vale Presente digital ou físico.
 */
@Entity
@Table(name = "vouchers_presente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "clienteComprador"})
@EqualsAndHashCode(of = "id")
public class VoucherPresente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo; // Ex: GIFT-883921

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprador_id")
    private Cliente clienteComprador;

    private String nomeBeneficiario;

    private String telefoneBeneficiario;

    @Column(nullable = false)
    private BigDecimal valorOriginal;

    @Column(nullable = false)
    private BigDecimal saldoRestante;

    private LocalDate validade;

    @Column(nullable = false)
    @Builder.Default
    private String status = "ATIVO"; // ATIVO, TOTALMENTE_UTILIZADO, EXPIRADO, CANCELADO

    private String mensagemPresente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"vouchers"})
    private Empresa empresa;

    public void abaterSaldo(BigDecimal valorAbatido) {
        if (valorAbatido == null) return;
        this.saldoRestante = this.saldoRestante.subtract(valorAbatido);
        if (this.saldoRestante.compareTo(BigDecimal.ZERO) <= 0) {
            this.saldoRestante = BigDecimal.ZERO;
            this.status = "TOTALMENTE_UTILIZADO";
        }
    }
}
