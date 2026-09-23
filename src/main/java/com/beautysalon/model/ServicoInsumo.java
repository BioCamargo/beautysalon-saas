package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Ficha técnica / Insumos consumidos por um serviço.
 * Permite baixar estoque automaticamente ao concluir o atendimento.
 */
@Entity
@Table(name = "servico_insumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"servico", "produto", "empresa"})
@EqualsAndHashCode(of = "id")
public class ServicoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    @ManyToOne
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantidadeGasta = 1; // Quantidade de unidades ou doses consumidas por execução

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"servicoInsumos"})
    private Empresa empresa;

    @CreationTimestamp
    private LocalDateTime criadoEm;
}
