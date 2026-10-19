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
@AllArgsConstructor
@Builder
@ToString
@EqualsAndHashCode(of = "id")
public class Empresa {

    public Empresa() {
        this.taxaCartaoEstimada = BigDecimal.ZERO;
        this.segmento = SegmentoEmpresa.SALAO_BELEZA;
        this.plano = PlanoSaaS.STUDIO;
        this.statusAssinatura = StatusAssinatura.TRIAL;
        this.ativo = true;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    /**
     * Identificador único na URL: /meu-salao/clientes
     * Deve ser lowercase, sem espaços, ex: "studio-nexipro"
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

    @Column(name = "asaas_customer_id", length = 50)
    private String asaasCustomerId;

    @Column(name = "asaas_subscription_id", length = 50)
    private String asaasSubscriptionId;

    @Column(name = "link_fatura_atual")
    private String linkFaturaAtual;

    @Column(name = "meta_pixel_id", length = 50)
    private String metaPixelId;

    @Column(name = "google_ads_id", length = 50)
    private String googleAdsId;

    @Column(name = "google_analytics_id", length = 50)
    private String googleAnalyticsId;

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

    /**
     * Retorna se a empresa está com fatura em atraso.
     */
    public boolean isInadimplente() {
        return statusAssinatura == StatusAssinatura.INADIMPLENTE;
    }

    /**
     * Retorna se o bloqueio suave está ativo.
     * Ativo quando:
     * 1. Status CANCELADA ou EXPIRADA
     * 2. Ou status INADIMPLENTE com vencimento da assinatura há mais de 3 dias de tolerância
     * 3. Ou TRIAL expirado (sem dias restantes)
     */
    public boolean isBloqueioSuaveAtivo() {
        if (!ativo) return true;
        if (statusAssinatura == StatusAssinatura.CANCELADA || statusAssinatura == StatusAssinatura.EXPIRADA) {
            return true;
        }
        if (statusAssinatura == StatusAssinatura.TRIAL) {
            return !isTrialAtivo();
        }
        if (statusAssinatura == StatusAssinatura.INADIMPLENTE) {
            if (dataFimAssinatura == null) return true;
            return dataFimAssinatura.plusDays(3).isBefore(LocalDateTime.now());
        }
        return false;
    }

    /**
     * Retorna se a empresa está em período de tolerância/carência por inadimplência (até 3 dias).
     */
    public boolean isToleranciaInadimplenteAtiva() {
        return statusAssinatura == StatusAssinatura.INADIMPLENTE && !isBloqueioSuaveAtivo();
    }

    /**
     * Retorna a quantidade de dias restantes de tolerância por inadimplência.
     */
    public long getDiasRestantesTolerancia() {
        if (statusAssinatura != StatusAssinatura.INADIMPLENTE || dataFimAssinatura == null) return 0;
        LocalDateTime limite = dataFimAssinatura.plusDays(3);
        long horas = java.time.Duration.between(LocalDateTime.now(), limite).toHours();
        if (horas <= 0) return 0;
        return (horas + 23) / 24;
    }
}
