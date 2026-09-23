package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Histórico de movimentações de estoque (Entrada, Saída de Venda, Consumo por Serviço, Ajuste Manual).
 */
@Entity
@Table(name = "movimentacoes_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"produto", "empresa", "usuario"})
@EqualsAndHashCode(of = "id")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private String tipo; // ENTRADA, SAIDA_VENDA, CONSUMO_SERVICO, AJUSTE_MANUAL

    @Column(nullable = false)
    private Integer quantidade; // Quantidade movimentada (positiva)

    private Integer saldoAnterior;

    private Integer saldoAtual;

    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private User usuario; // Quem realizou a movimentação

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"movimentacoesEstoque"})
    private Empresa empresa;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dataHora;
}
