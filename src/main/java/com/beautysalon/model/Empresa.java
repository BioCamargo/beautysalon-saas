package com.beautysalon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Representa um tenant (salão/empresa) no sistema SaaS.
 * Cada empresa tem seu próprio espaço isolado de dados.
 */
@Entity
@Table(name = "empresas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /**
     * Identificador único na URL: /meu-salao/clientes
     * Deve ser lowercase, sem espaços, ex: "studio-lumora"
     */
    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    private String cnpj;

    private String telefone;

    private String email;

    private String logoUrl;

    private String chavePix; // CPF, CNPJ, Telefone, E-mail ou Aleatória

    private String cidade; // Cidade para geração do BR Code Pix

    @Enumerated(EnumType.STRING)
    @Column(name = "segmento", length = 50)
    @Builder.Default
    private SegmentoEmpresa segmento = SegmentoEmpresa.SALAO_BELEZA;

    @Builder.Default
    private boolean ativo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime criadoEm;
}
