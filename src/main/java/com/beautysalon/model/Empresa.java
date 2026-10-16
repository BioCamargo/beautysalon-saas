package com.beautysalon.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
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

    @Column(name = "logo_url", columnDefinition = "TEXT")
    private String logoUrl;

    private String chavePix; // CPF, CNPJ, Telefone, E-mail ou Aleatória

    private String cidade; // Cidade para geração do BR Code Pix

    @Column(name = "taxa_cartao_estimada", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaCartaoEstimada = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "segmento", length = 50)
    @Builder.Default
    private SegmentoEmpresa segmento = SegmentoEmpresa.SALAO_BELEZA;

    @Enumerated(EnumType.STRING)
    @Column(name = "plano", length = 30)
    @Builder.Default
    private PlanoSaaS plano = PlanoSaaS.STUDIO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_assinatura", length = 30)
    @Builder.Default
    private StatusAssinatura statusAssinatura = StatusAssinatura.TRIAL;

    @Column(name = "data_expiracao_trial")
    private LocalDateTime dataExpiracaoTrial;

    @Column(name = "data_fim_assinatura")
    private LocalDateTime dataFimAssinatura;

    @Builder.Default
    private boolean ativo = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime criadoEm;

    /**
     * Retorna se a empresa está em período de teste válido.
     */
    public boolean isTrialAtivo() {
        return statusAssinatura == StatusAssinatura.TRIAL
                && dataExpiracaoTrial != null
                && dataExpiracaoTrial.isAfter(LocalDateTime.now());
    }

    /**
     * Retorna a quantidade de dias restantes de teste grátis (arredondado para cima).
     */
    public long getDiasRestantesTrial() {
        if (dataExpiracaoTrial == null) return 0;
        long horas = java.time.Duration.between(LocalDateTime.now(), dataExpiracaoTrial).toHours();
        if (horas <= 0) return 0;
        return (horas + 23) / 24;
    }

    /**
     * Retorna se a conta possui acesso regular ao SaaS (Trial ativo ou Assinatura paga).
     */
    public boolean isAssinaturaValida() {
        if (!ativo) return false;
        if (statusAssinatura == StatusAssinatura.ATIVA) return true;
        return isTrialAtivo();
    }
}
