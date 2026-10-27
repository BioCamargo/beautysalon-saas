package com.beautysalon.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro corporativo imutável da trilha de auditoria (Audit Trail).
 * Armazena eventos de alta sensibilidade (financeiro, comandas, logins, modificações cadastrais).
 */
@Entity
@Table(name = "auditoria_evento")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "empresa_id")
    private Long empresaId;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "nome_usuario", length = 150)
    private String nomeUsuario;

    @Column(name = "ip_origem", length = 45)
    private String ipOrigem;

    @Column(name = "tipo_acao", nullable = false, length = 40)
    private String tipoAcao;

    @Column(name = "tabela_nome", length = 60)
    private String tabelaNome;

    @Column(name = "registro_id", length = 50)
    private String registroId;

    @Column(columnDefinition = "TEXT")
    private String detalhes;

    @Column(name = "dados_anteriores", columnDefinition = "TEXT")
    private String dadosAnteriores;

    @Column(name = "dados_novos", columnDefinition = "TEXT")
    private String dadosNovos;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    public void prePersist() {
        if (this.criadoEm == null) {
            this.criadoEm = LocalDateTime.now();
        }
    }
}
