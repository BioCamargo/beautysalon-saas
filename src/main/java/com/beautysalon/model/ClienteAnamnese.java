package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Ficha de Anamnese, Histórico Químico e Galeria de Fotos Antes/Depois do Cliente.
 */
@Entity
@Table(name = "cliente_anamneses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"cliente", "empresa", "profissional", "comanda"})
@EqualsAndHashCode(callSuper = false, of = "id")
public class ClienteAnamnese extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @JsonIgnoreProperties({"anamneses", "agendamentos"})
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id")
    private User profissional;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comanda_id")
    private Comanda comanda;

    private String procedimentoRealizado; // Ex: "Mechas Loiras Peroladas", "Progressiva Orgânica"

    @Column(columnDefinition = "TEXT")
    private String formulaQuimica; // Ex: "Pó descolorante 50g + OX 30vol (1:2), Tonalizante 9.89 com OX 10vol por 20min"

    @Column(columnDefinition = "TEXT")
    private String historicoCapilarAlergias; // Alergias declaradas, teste de mecha, elasticidade

    @Column(columnDefinition = "TEXT")
    private String observacoesTecnicas;

    private String fotoAntesUrl; // Caminho/URL da foto antes

    private String fotoDepoisUrl; // Caminho/URL da foto depois

    @Column(columnDefinition = "TEXT")
    private String assinaturaDigitalBase64; // Assinatura digital do cliente coletada na tela touch

    @Builder.Default
    private boolean termoConsentimentoAceito = true;

    @Builder.Default
    private LocalDateTime dataRegistro = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"anamneses"})
    private Empresa empresa;
}
