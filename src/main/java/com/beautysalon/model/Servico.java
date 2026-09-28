package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Serviço oferecido por um salão. Sempre isolado por empresa (tenant).
 */
@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "agendamentos"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class Servico extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    private BigDecimal preco;

    private String imagem;

    @Builder.Default
    private Integer duracaoMinutos = 30; // Duração estimada do serviço

    @Builder.Default
    private Integer diasCicloRetorno = 30; // Intervalo ideal estimado para retorno/manutenção (ex: 15 manicure, 30 corte, 45 coloração)

    private BigDecimal percentualComissao; // Se nulo, usa o percentual padrão do profissional

    /**
     * Empresa (tenant) à qual este serviço pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"servicos"})
    private Empresa empresa;

    @ManyToMany(mappedBy = "servicos")
    @JsonIgnore
    private List<Agendamento> agendamentos;
}
