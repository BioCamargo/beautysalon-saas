package com.beautysalon.service;

import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import com.beautysalon.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppConfigRepository configRepository;
    private final EmpresaRepository empresaRepository;
    private final RestTemplate restTemplate;

    public WhatsAppService(WhatsAppConfigRepository configRepository,
                           EmpresaRepository empresaRepository) {
        this.configRepository = configRepository;
        this.empresaRepository = empresaRepository;
        this.restTemplate = new RestTemplate();
    }

    public Optional<WhatsAppConfig> obterConfiguracaoAtual() {
        Long empresaId = TenantContext.getEmpresaId();
        if (empresaId == null) return Optional.empty();
        return configRepository.findByEmpresaId(empresaId);
    }

    @Transactional
    public WhatsAppConfig salvarConfiguracao(WhatsAppConfig formConfig) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalStateException("Empresa não encontrada"));

        WhatsAppConfig config = configRepository.findByEmpresaId(empresaId)
                .orElseGet(() -> WhatsAppConfig.builder().empresa(empresa).build());

        config.setAtivo(formConfig.isAtivo());
        config.setProvider(formConfig.getProvider() != null ? formConfig.getProvider() : "META_OFFICIAL");
        
        // Meta
        config.setMetaPhoneNumberId(formConfig.getMetaPhoneNumberId());
        config.setMetaAccessToken(formConfig.getMetaAccessToken());
        config.setMetaBusinessAccountId(formConfig.getMetaBusinessAccountId());

        // Evolution / Z-API
        config.setInstanceName(formConfig.getInstanceName());
        config.setApiUrl(formConfig.getApiUrl());
        config.setApiKey(formConfig.getApiKey());

        // Automações
        config.setNotificarAgendamento(formConfig.isNotificarAgendamento());
        config.setNotificarLembrete(formConfig.isNotificarLembrete());
        config.setNotificarComandaFechada(formConfig.isNotificarComandaFechada());
        config.setNotificarAniversario(formConfig.isNotificarAniversario());

        return configRepository.save(config);
    }

    /**
     * Envia confirmação automática de agendamento se o WhatsApp estiver ativo para a empresa.
     */
    public boolean enviarConfirmacaoAgendamento(Agendamento agendamento) {
        if (agendamento == null || agendamento.getCliente() == null || agendamento.getCliente().getTelefone() == null) {
            return false;
        }

        Long empresaId = agendamento.getEmpresa() != null ? agendamento.getEmpresa().getId() : TenantContext.getEmpresaId();
        if (empresaId == null) return false;

        Optional<WhatsAppConfig> configOpt = configRepository.findByEmpresaId(empresaId);
        if (configOpt.isEmpty() || !configOpt.get().isAtivo() || !configOpt.get().isNotificarAgendamento()) {
            return false;
        }

        WhatsAppConfig config = configOpt.get();
        String empresaNome = agendamento.getEmpresa() != null ? agendamento.getEmpresa().getNome() : "Studio";
        String servicoNome = (agendamento.getServicos() != null && !agendamento.getServicos().isEmpty()) 
                ? agendamento.getServicos().get(0).getNome() : "Atendimento";
        String dataFormatada = agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormatada = agendamento.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));

        String mensagem = String.format(
                "🌸 *%s*\n\nOlá, *%s*! ✨\nSeu agendamento para *%s* foi confirmado com sucesso!\n\n📅 *Data:* %s\n⏰ *Horário:* %s\n\nCaso precise remarcar, entre em contato conosco. Estamos te esperando! 💕",
                empresaNome,
                agendamento.getCliente().getNome(),
                servicoNome,
                dataFormatada,
                horaFormatada
        );

        return enviarMensagemTexto(config, agendamento.getCliente().getTelefone(), mensagem);
    }

    /**
     * Envia comprovante de pagamento / comanda fechada por WhatsApp.
     */
    public boolean enviarReciboComanda(Comanda comanda) {
        if (comanda == null || comanda.getCliente() == null || comanda.getCliente().getTelefone() == null) {
            return false;
        }

        Long empresaId = comanda.getEmpresa() != null ? comanda.getEmpresa().getId() : TenantContext.getEmpresaId();
        if (empresaId == null) return false;

        Optional<WhatsAppConfig> configOpt = configRepository.findByEmpresaId(empresaId);
        if (configOpt.isEmpty() || !configOpt.get().isAtivo() || !configOpt.get().isNotificarComandaFechada()) {
            return false;
        }

        WhatsAppConfig config = configOpt.get();
        String empresaNome = comanda.getEmpresa() != null ? comanda.getEmpresa().getNome() : "Studio";
        String totalFormatado = String.format("%.2f", comanda.getValorTotal() != null ? comanda.getValorTotal() : java.math.BigDecimal.ZERO);

        String mensagem = String.format(
                "🧾 *%s - Comprovante de Atendimento*\n\nOlá, *%s*! Segue o resumo do seu atendimento:\n\n💳 *Comanda:* %s\n💰 *Total:* R$ %s\n💳 *Forma de Pagamento:* %s\n\nMuito obrigado pela sua visita! Volte sempre! ✨",
                empresaNome,
                comanda.getCliente().getNome(),
                comanda.getNumeroComanda(),
                totalFormatado,
                comanda.getFormaPagamento() != null ? comanda.getFormaPagamento() : "PAGO"
        );

        return enviarMensagemTexto(config, comanda.getCliente().getTelefone(), mensagem);
    }

    /**
     * Roteador de envio de acordo com o provedor configurado.
     */
    public boolean enviarMensagemTexto(WhatsAppConfig config, String telefone, String mensagem) {
        if (telefone == null || telefone.isBlank() || mensagem == null || mensagem.isBlank()) {
            return false;
        }

        String telLimpo = telefone.replaceAll("[^0-9]", "");
        if (!telLimpo.startsWith("55") && (telLimpo.length() == 10 || telLimpo.length() == 11)) {
            telLimpo = "55" + telLimpo;
        }

        try {
            if ("META_OFFICIAL".equalsIgnoreCase(config.getProvider())) {
                return enviarViaMetaOficial(config, telLimpo, mensagem);
            } else {
                return enviarViaEvolutionApi(config, telLimpo, mensagem);
            }
        } catch (Exception e) {
            logger.error("Erro ao enviar mensagem WhatsApp para {}: {}", telLimpo, e.getMessage());
            return false;
        }
    }

    private boolean enviarViaMetaOficial(WhatsAppConfig config, String telefone, String mensagem) {
        if (config.getMetaPhoneNumberId() == null || config.getMetaAccessToken() == null) {
            logger.warn("Credenciais da Meta Cloud API incompletas.");
            return false;
        }

        String url = String.format("https://graph.facebook.com/v19.0/%s/messages", config.getMetaPhoneNumberId().trim());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(config.getMetaAccessToken().trim());

        Map<String, Object> body = new HashMap<>();
        body.put("messaging_product", "whatsapp");
        body.put("to", telefone);
        body.put("type", "text");
        
        Map<String, String> textObj = new HashMap<>();
        textObj.put("body", mensagem);
        body.put("text", textObj);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }

    private boolean enviarViaEvolutionApi(WhatsAppConfig config, String telefone, String mensagem) {
        if (config.getApiUrl() == null || config.getInstanceName() == null) {
            logger.warn("Configurações da Evolution API / Z-API incompletas.");
            return false;
        }

        String baseUrl = config.getApiUrl().trim();
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        String url = String.format("%s/message/sendText/%s", baseUrl, config.getInstanceName().trim());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            headers.set("apikey", config.getApiKey().trim());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("number", telefone);
        body.put("text", mensagem);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        return response.getStatusCode().is2xxSuccessful();
    }
}
