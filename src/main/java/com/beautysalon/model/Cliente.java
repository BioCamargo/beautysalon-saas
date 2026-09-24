package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Cliente de um salão. Sempre isolado por empresa (tenant).
 */
@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "agendamentos"})
@EqualsAndHashCode(of = "id")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String email;

    private String telefone;

    private java.time.LocalDate dataNascimento;

    // Ficha de Anamnese e Histórico Técnico
    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String tipoCabeloPele;

    @Column(columnDefinition = "TEXT")
    private String historicoQuimico;

    @Column(columnDefinition = "TEXT")
    private String observacoesTecnicas;

    /**
     * Empresa (tenant) à qual este cliente pertence.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"clientes"})
    private Empresa empresa;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"cliente"})
    private List<Agendamento> agendamentos;
}