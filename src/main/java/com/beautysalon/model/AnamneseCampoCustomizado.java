package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Campo customizado para a Ficha de Anamnese definido pelo próprio salão (Form Builder).
 */
@Entity
@Table(name = "anamnese_campos_customizados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "empresa")
@EqualsAndHashCode(callSuper = false, of = "id")
public class AnamneseCampoCustomizado extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"anamneses", "camposAnamnese"})
    private Empresa empresa;

    @Column(nullable = false, length = 150)
    private String rotulo; // Ex: "Fuma ou consome álcool frequentemente?", "Usa lentes de contato?"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCampoCustomizado tipoCampo; // TEXTO, SIM_NAO, SELECAO, MULTIPLA_ESCOLHA, TEXTO_LONGO

    @Column(length = 500)
    private String opcoes; // Opções separadas por vírgula para campos de SELECAO (ex: "Opção A, Opção B, Opção C")

    @Column(length = 200)
    private String placeholder; // Dica de preenchimento

    @Builder.Default
    private boolean obrigatorio = false;

    @Builder.Default
    private boolean ativo = true;

    @Builder.Default
    private Integer ordem = 0;
}
