package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Registro de ausências, folgas ou intervalos recorrentes de um profissional.
 */
@Entity
@Table(name = "ausencias_profissionais")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa", "profissional"})
@EqualsAndHashCode(of = "id")
public class AusenciaProfissional {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profissional_id", nullable = false)
    private User profissional;

    @Column(nullable = false)
    private String tipo; // FOLGA_PONTUAL, FOLGA_RECORRENTE, FERIAS, INTERVALO_ALMOCO, CURSO

    private LocalDate dataEspecifica; // Para folgas ou feriados pontuais

    @Enumerated(EnumType.STRING)
    private DayOfWeek diaSemanaRecorrente; // Ex: MONDAY para folga toda segunda-feira

    private LocalTime horaInicio; // Ex: 12:00

    private LocalTime horaFim;    // Ex: 13:30

    private String motivo;

    @Builder.Default
    private boolean ativo = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"ausencias"})
    private Empresa empresa;
}
