package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Usuário do sistema, sempre vinculado a uma empresa (tenant).
 * O mesmo email/username pode existir em empresas diferentes.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"empresa"})
@EqualsAndHashCode(of = "id")
@Table(name = "\"user\"",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "empresa_id"}))
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String email;

    private String password;

    @Builder.Default
    private boolean ativo = true;

    @Column(name = "image")
    private String imagem;

    /**
     * Percentual padrão de comissão do profissional (ex: 30.00 para 30%)
     */
    @Builder.Default
    private java.math.BigDecimal percentualComissao = java.math.BigDecimal.ZERO;

    private String especialidade; // Ex: Cabeleireiro(a), Barbeiro(a), Manicure, Esteticista

    private String telefone;

    @Builder.Default
    private String corAgenda = "#d4af37"; // Cor para identificar na agenda multiprofissional

    /**
     * Papel do usuário dentro da empresa.
     * OWNER > ADMIN > FUNCIONARIO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tenant_role", nullable = false)
    @Builder.Default
    private TenantRole tenantRole = TenantRole.FUNCIONARIO;

    /**
     * Empresa à qual este usuário pertence.
     * Todo usuário DEVE pertencer a uma empresa.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    @JsonIgnoreProperties({"users"})
    private Empresa empresa;

    public void setEnabled(boolean b) {
        this.ativo = b;
    }
}
