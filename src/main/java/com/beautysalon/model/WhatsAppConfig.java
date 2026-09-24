package com.beautysalon.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

/**
 * Configuração de integração de WhatsApp por Empresa (Tenant).
 * Suporta Meta Cloud API Oficial e provedores alternativos (Evolution API, Z-API).
 */
@Entity
@Table(name = "whatsapp_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "empresa")
@EqualsAndHashCode(of = "id")
public class WhatsAppConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"whatsappConfig"})
    private Empresa empresa;

    @Builder.Default
    private boolean ativo = false;

    /**
     * Provedor: META_OFFICIAL, EVOLUTION_API, Z_API
     */
    @Builder.Default
    private String provider = "META_OFFICIAL";

    // --- META CLOUD API (Oficial) ---
    private String metaPhoneNumberId;
    private String metaAccessToken;
    private String metaBusinessAccountId;

    // --- EVOLUTION API / Z-API (QR Code / Endpoint) ---
    private String instanceName;
    private String apiUrl;
    private String apiKey;

    // --- PREFERÊNCIAS DE AUTOMAÇÃO ---
    @Builder.Default
    private boolean notificarAgendamento = true;

    @Builder.Default
    private boolean notificarLembrete = true;

    @Builder.Default
    private boolean notificarComandaFechada = true;

    @Builder.Default
    private boolean notificarAniversario = true;

    // --- TEMPLATES CUSTOMIZADOS (Opcional) ---
    @Column(length = 1000)
    private String mensagemAgendamentoCustom;

    @Column(length = 1000)
    private String mensagemLembreteCustom;
}
