package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Movimentações de Entrada e Saída do Caixa (sangrias, reforços, despesas operacionais).
 */
@Entity
@Table(name = "movimentacoes_financeiras")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"caixa", "empresa", "usuario"})
@EqualsAndHashCode(of = "id")
public class MovimentacaoFinanceira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caixa_id", nullable = false)
    private Caixa caixa;

    @Column(nullable = false)
    private String tipo; // ENTRADA_COMANDA, REFORCO, SANGRIA, DESPESA_AVULSA

    @Column(nullable = false)
    private BigDecimal valor;

    private String categoria; // Despesa Fixa, Insumos, Retirada, Venda Balcão, etc.

    private String descricao;

    private String formaPagamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"movimentacoesFinanceiras"})
    private Empresa empresa;

    @CreationTimestamp
    private LocalDateTime dataHora;
}
