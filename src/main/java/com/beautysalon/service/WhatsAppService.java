package com.beautysalon.service;

import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import com.beautysalon.tenant.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class WhatsAppService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsAppService.class);

    private final WhatsAppConfigRepository configRepository;
    private final EmpresaRepository empresaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final RestTemplate restTemplate;

    public WhatsAppService(WhatsAppConfigRepository configRepository,
                           EmpresaRepository empresaRepository,
                           AgendamentoRepository agendamentoRepository) {
        this.configRepository = configRepository;
        this.empresaRepository = empresaRepository;
        this.agendamentoRepository = agendamentoRepository;

        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 5 segundos para conexão
        factory.setReadTimeout(10000);  // 10 segundos para resposta
        this.restTemplate = new RestTemplate(factory);
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

        // Templates Customizados
        config.setMensagemAgendamentoCustom(formConfig.getMensagemAgendamentoCustom());
        config.setMensagemLembreteCustom(formConfig.getMensagemLembreteCustom());
        config.setMensagemComandaCustom(formConfig.getMensagemComandaCustom());
        config.setMensagemAniversarioCustom(formConfig.getMensagemAniversarioCustom());

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
        String empresaNome = agendamento.getEmpresa() != null ? agendamento.getEmpresa().getNome() : "NEXI PRO Studio";
        String servicoNome = (agendamento.getServicos() != null && !agendamento.getServicos().isEmpty()) 
                ? agendamento.getServicos().get(0).getNome() : "Atendimento";
        String dataFormatada = agendamento.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String horaFormatada = agendamento.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));

        String mensagem;
        if (config.getMensagemAgendamentoCustom() != null && !config.getMensagemAgendamentoCustom().isBlank()) {
            mensagem = config.getMensagemAgendamentoCustom()
                    .replace("{empresa}", empresaNome)
                    .replace("{cliente}", agendamento.getCliente().getNome())
                    .replace("{servico}", servicoNome)
                    .replace("{data}", dataFormatada)
                    .replace("{hora}", horaFormatada);
        } else {
            mensagem = String.format(
                    "🌸 *%s*\n\n" +
                    "Olá, *%s*! ✨\n" +
                    "Seu agendamento para *%s* foi registrado com sucesso!\n\n" +
                    "📅 *Data:* %s\n" +
                    "⏰ *Horário:* %s\n\n" +
                    "👉 *Por favor, responda a esta mensagem:*\n" +
                    "Digite *1* para *Confirmar sua Presença*\n" +
                    "Digite *2* para *Avisar Remarcação ou Cancelamento*\n\n" +
                    "Estamos preparando tudo para te receber com muito carinho! 💕",
                    empresaNome,
                    agendamento.getCliente().getNome(),
                    servicoNome,
                    dataFormatada,
                    horaFormatada
            );
        }

        return enviarMensagemTexto(config, agendamento.getCliente().getTelefone(), mensagem);
    }

    /**
     * Envia comprovante de pagamento / comanda fechada por WhatsApp, com suporte a chave Pix.
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
        String empresaNome = comanda.getEmpresa() != null ? comanda.getEmpresa().getNome() : "NEXI PRO Studio";
        String totalFormatado = String.format("%.2f", comanda.getValorTotal() != null ? comanda.getValorTotal() : java.math.BigDecimal.ZERO);

        String mensagem;
        if (config.getMensagemComandaCustom() != null && !config.getMensagemComandaCustom().isBlank()) {
            mensagem = config.getMensagemComandaCustom()
                    .replace("{empresa}", empresaNome)
                    .replace("{cliente}", comanda.getCliente().getNome())
                    .replace("{comanda}", comanda.getNumeroComanda())
                    .replace("{total}", totalFormatado)
                    .replace("{formaPagamento}", comanda.getFormaPagamento() != null ? comanda.getFormaPagamento() : "PAGO");
        } else {
            String detalhePix = "";
            if (comanda.getEmpresa() != null && comanda.getEmpresa().getChavePix() != null && !comanda.getEmpresa().getChavePix().isBlank()) {
                detalhePix = String.format("\n🔑 *Chave Pix do Estabelecimento:* `%s`\n", comanda.getEmpresa().getChavePix().trim());
            }

            mensagem = String.format(
                    "🧾 *%s - Comprovante de Atendimento*\n\n" +
                    "Olá, *%s*! Segue o resumo do seu atendimento:\n\n" +
                    "💳 *Comanda:* %s\n" +
                    "💰 *Total:* R$ %s\n" +
                    "💳 *Forma de Pagamento:* %s\n%s\n" +
                    "Muito obrigado pela sua preferência! Foi um enorme prazer te atender. Volte sempre! ✨",
                    empresaNome,
                    comanda.getCliente().getNome(),
                    comanda.getNumeroComanda(),
                    totalFormatado,
                    comanda.getFormaPagamento() != null ? comanda.getFormaPagamento() : "PAGO",
                    detalhePix
            );
        }

        return enviarMensagemTexto(config, comanda.getCliente().getTelefone(), mensagem);
    }

    /**
     * Processa a resposta interativa enviada pelo cliente no WhatsApp (Webhook).
     * Reconhece intenções:
     * - 1 / SIM / CONFIRMO -> Atualiza status para CONFIRMADO
     * - 2 / NÃO / CANCELAR / REMARCAR -> Atualiza status para CANCELADO
     */
    @Transactional
    public boolean processarRespostaCliente(String telefone, String textoRecebido, String instanceName) {
        if (telefone == null || textoRecebido == null) return false;

        String texto = textoRecebido.trim().toLowerCase();
        String telDigitos = telefone.replaceAll("[^0-9]", "");
        String telSufixo = telDigitos.length() >= 8 ? telDigitos.substring(telDigitos.length() - 8) : telDigitos;

        LocalDateTime agora = LocalDateTime.now();

        List<Agendamento> agendamentos;
        Optional<WhatsAppConfig> configOpt = (instanceName != null && !instanceName.isBlank()) 
                ? configRepository.findByInstanceName(instanceName) : Optional.empty();

        if (configOpt.isPresent() && configOpt.get().getEmpresa() != null) {
            agendamentos = agendamentoRepository.findProximosAgendamentosPorEmpresa(configOpt.get().getEmpresa().getId(), agora.minusHours(2));
        } else {
            agendamentos = agendamentoRepository.findProximosAgendamentosGlobais(agora.minusHours(2));
        }

        Optional<Agendamento> agendamentoOpt = agendamentos.stream()
                .filter(a -> a.getCliente() != null && a.getCliente().getTelefone() != null)
                .filter(a -> {
                    String clienteTel = a.getCliente().getTelefone().replaceAll("[^0-9]", "");
                    return clienteTel.endsWith(telSufixo) || telDigitos.endsWith(clienteTel);
                })
                .findFirst();

        if (agendamentoOpt.isEmpty()) {
            logger.info("Nenhum agendamento futuro encontrado para o telefone {}", telefone);
            return false;
        }

        Agendamento ag = agendamentoOpt.get();
        WhatsAppConfig configEmpresa = configRepository.findByEmpresaId(ag.getEmpresa().getId()).orElse(null);
        if (configEmpresa == null || !configEmpresa.isAtivo()) {
            return false;
        }

        String clienteNome = ag.getCliente().getNome();
        String empresaNome = ag.getEmpresa().getNome();
        String horaFormatada = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
        String dataFormatada = ag.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM"));

        // Intenção 1: CONFIRMAR PRESENÇA
        if (texto.equals("1") || texto.contains("sim") || texto.contains("confirmo") || texto.contains("confirmar") || texto.contains("confirmado") || texto.contains("ok")) {
            ag.setStatus("CONFIRMADO");
            agendamentoRepository.save(ag);

            String resposta = String.format(
                    "🎉 *Presença Confirmada!*\n\n" +
                    "Muito obrigado, *%s*! ✨\n" +
                    "Seu atendimento no *%s* está confirmado para *%s* às *%s*.\n\n" +
                    "Nossa equipe já está com tudo pronto para te receber! 💕",
                    clienteNome, empresaNome, dataFormatada, horaFormatada
            );
            enviarMensagemTexto(configEmpresa, telefone, resposta);
            logger.info("Agendamento #{} atualizado para CONFIRMADO via WhatsApp por {}", ag.getId(), telefone);
            return true;
        }

        // Intenção 2: CANCELAR OU REMARCAR
        if (texto.equals("2") || texto.contains("não") || texto.contains("nao") || texto.contains("cancelar") || texto.contains("remarcar") || texto.contains("desmarcar")) {
            ag.setStatus("CANCELADO");
            agendamentoRepository.save(ag);

            String linkAutoatendimento = "https://app.nexipro.com.br/" + ag.getEmpresa().getSlug() + "/agendar";
            String resposta = String.format(
                    "🌸 *Horário Cancelado!*\n\n" +
                    "Tudo bem, *%s*! Compreendemos perfeitamente. Seu horário das *%s* foi cancelado e liberado na agenda.\n\n" +
                    "Quando quiser escolher um novo dia ou horário com comodidade, basta acessar:\n" +
                    "👉 %s\n\n" +
                    "Esperamos te ver em breve! ✨",
                    clienteNome, horaFormatada, linkAutoatendimento
            );
            enviarMensagemTexto(configEmpresa, telefone, resposta);
            logger.info("Agendamento #{} CANCELADO via WhatsApp por {}", ag.getId(), telefone);
            return true;
        }

        return false;
    }

    /**
     * Consulta estado de conexão e gera QR Code para pareamento via Evolution API.
     */
    public Map<String, Object> obterQrCodeInstancia(WhatsAppConfig config) {
        Map<String, Object> resultado = new HashMap<>();
        if (config == null || config.getApiUrl() == null || config.getInstanceName() == null) {
            resultado.put("sucesso", false);
            resultado.put("mensagem", "URL da API ou Nome da Instância não configurados.");
            return resultado;
        }

        String baseUrl = config.getApiUrl().trim();
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        String instance = config.getInstanceName().trim();

        HttpHeaders headers = new HttpHeaders();
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            headers.set("apikey", config.getApiKey().trim());
        }

        try {
            // 1. Verifica estado de conexão atual
            String urlState = String.format("%s/instance/connectionState/%s", baseUrl, instance);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> stateResp = restTemplate.exchange(urlState, HttpMethod.GET, entity, Map.class);
            if (stateResp.getStatusCode().is2xxSuccessful() && stateResp.getBody() != null) {
                Object state = stateResp.getBody().get("state");
                if ("open".equalsIgnoreCase(String.valueOf(state))) {
                    resultado.put("sucesso", true);
                    resultado.put("status", "CONNECTED");
                    resultado.put("mensagem", "WhatsApp conectado e pronto para automações!");
                    return resultado;
                }
            }

            // 2. Se desconectado, solicita o QR Code da instância
            String urlConnect = String.format("%s/instance/connect/%s", baseUrl, instance);
            ResponseEntity<Map> connectResp = restTemplate.exchange(urlConnect, HttpMethod.GET, entity, Map.class);
            if (connectResp.getStatusCode().is2xxSuccessful() && connectResp.getBody() != null) {
                Map body = connectResp.getBody();
                resultado.put("sucesso", true);
                resultado.put("status", "QRCODE");
                resultado.put("qrcode", body.get("base64") != null ? body.get("base64") : body.get("code"));
                resultado.put("pairingCode", body.get("pairingCode"));
                return resultado;
            }
        } catch (Exception e) {
            logger.warn("Instância {} ainda não existe na Evolution API, tentando criar...", instance);
            try {
                String urlCreate = String.format("%s/instance/create", baseUrl);
                headers.setContentType(MediaType.APPLICATION_JSON);
                Map<String, Object> createBody = new HashMap<>();
                createBody.put("instanceName", instance);
                createBody.put("qrcode", true);
                createBody.put("integration", "WHATSAPP-BAILEYS");

                HttpEntity<Map<String, Object>> createEntity = new HttpEntity<>(createBody, headers);
                ResponseEntity<Map> createResp = restTemplate.exchange(urlCreate, HttpMethod.POST, createEntity, Map.class);
                if (createResp.getStatusCode().is2xxSuccessful() && createResp.getBody() != null) {
                    Map qrcodeObj = (Map) createResp.getBody().get("qrcode");
                    resultado.put("sucesso", true);
                    resultado.put("status", "QRCODE");
                    resultado.put("qrcode", qrcodeObj != null ? qrcodeObj.get("base64") : null);
                    return resultado;
                }
            } catch (Exception exCreate) {
                logger.error("Erro ao criar instância Evolution API: {}", exCreate.getMessage());
            }
        }

        resultado.put("sucesso", false);
        resultado.put("mensagem", "Não foi possível conectar à Evolution API. Certifique-se de que o container do docker-compose-evolution.yml está rodando.");
        return resultado;
    }

    /**
     * Registra o webhook do NEXI PRO automaticamente na instância da Evolution API.
     */
    public boolean configurarWebhook(WhatsAppConfig config, String webhookUrl) {
        if (config == null || config.getApiUrl() == null || config.getInstanceName() == null || webhookUrl == null) {
            return false;
        }

        String baseUrl = config.getApiUrl().trim();
        if (baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        String instance = config.getInstanceName().trim();

        String url = String.format("%s/webhook/set/%s", baseUrl, instance);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            headers.set("apikey", config.getApiKey().trim());
        }

        Map<String, Object> body = new HashMap<>();
        body.put("enabled", true);
        body.put("url", webhookUrl);
        body.put("webhook_by_events", false);
        body.put("events", List.of("MESSAGES_UPSERT", "CONNECTION_UPDATE"));

        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("Aviso ao auto-configurar webhook na Evolution API: {}", e.getMessage());
            return false;
        }
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
