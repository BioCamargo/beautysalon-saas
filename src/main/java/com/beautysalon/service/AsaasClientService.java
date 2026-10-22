package com.beautysalon.service;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.PlanoSaaS;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Cliente de integração com a API v3 do Asaas (Cobranças e Assinaturas Recorrentes SaaS).
 */
@Slf4j
@Service
public class AsaasClientService {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${nexipro.saas.billing.api-url:https://api.asaas.com/v3}")
    private String apiUrl;

    @Value("${nexipro.saas.billing.api-key:}")
    private String apiKey;

    @Value("${nexipro.saas.billing.enabled:true}")
    private boolean billingEnabled;

    public AsaasClientService(EmpresaRepository empresaRepository, UserRepository userRepository) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.objectMapper = new ObjectMapper();

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(6000);
        factory.setReadTimeout(12000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * Cria ou localiza o cliente no Asaas vinculado à empresa SaaS.
     */
    @Transactional
    public String obterOuCriarCliente(Empresa empresa) {
        if (!billingEnabled || apiKey == null || apiKey.isBlank()) {
            log.warn("Integração Asaas desativada ou API Key não configurada.");
            return null;
        }

        if (empresa.getAsaasCustomerId() != null && !empresa.getAsaasCustomerId().isBlank()) {
            return empresa.getAsaasCustomerId();
        }

        // Resolve e-mail da empresa ou do usuário proprietário
        String email = empresa.getEmail();
        if (email == null || email.isBlank()) {
            List<User> usuarios = userRepository.findAllByEmpresaId(empresa.getId());
            for (User u : usuarios) {
                if (u.getEmail() != null && !u.getEmail().isBlank()) {
                    email = u.getEmail().trim();
                    empresa.setEmail(email);
                    break;
                }
            }
        }
        if (email == null || email.isBlank()) {
            email = "contato." + empresa.getSlug() + "@nexipro.com.br";
        }

        String nome = empresa.getNome() != null && !empresa.getNome().isBlank() 
                ? empresa.getNome() 
                : "Empresa " + empresa.getSlug();

        try {
            // 1. Tenta buscar por e-mail no Asaas se disponível
            String searchUrl = apiUrl + "/customers?email=" + email.trim();
            ResponseEntity<String> searchResp = restTemplate.exchange(
                    searchUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(criarHeadersAutenticados()),
                    String.class
            );

            if (searchResp.getStatusCode().is2xxSuccessful() && searchResp.getBody() != null) {
                JsonNode root = objectMapper.readTree(searchResp.getBody());
                JsonNode data = root.path("data");
                if (data.isArray() && !data.isEmpty()) {
                    String customerId = data.get(0).path("id").asText();
                    empresa.setAsaasCustomerId(customerId);
                    empresaRepository.save(empresa);
                    log.info("Cliente Asaas existente localizado por e-mail: {}", customerId);
                    return customerId;
                }
            }

            // 2. Cria novo cliente no Asaas
            Map<String, Object> body = new HashMap<>();
            body.put("name", nome);
            body.put("email", email.trim());
            
            if (empresa.getCnpj() != null && !empresa.getCnpj().isBlank()) {
                String documentoLimpo = empresa.getCnpj().replaceAll("\\D", "");
                if (documentoLimpo.length() == 11 || documentoLimpo.length() == 14) {
                    body.put("cpfCnpj", documentoLimpo);
                }
            }
            if (empresa.getTelefone() != null && !empresa.getTelefone().isBlank()) {
                String foneLimpo = empresa.getTelefone().replaceAll("\\D", "");
                if (foneLimpo.length() >= 10) {
                    body.put("phone", foneLimpo);
                }
            }
            body.put("externalReference", "empresa_" + empresa.getId());
            body.put("notificationDisabled", false);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, criarHeadersAutenticados());
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "/customers", request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String novoCustomerId = root.path("id").asText();
                empresa.setAsaasCustomerId(novoCustomerId);
                empresaRepository.save(empresa);
                log.info("Novo cliente Asaas criado com sucesso: ID={} para Empresa ID={}", novoCustomerId, empresa.getId());
                return novoCustomerId;
            }

        } catch (HttpStatusCodeException hse) {
            log.error("Erro HTTP ao criar cliente no Asaas: status={}, body={}", hse.getStatusCode(), hse.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Erro ao comunicar com API de clientes do Asaas: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * Cria ou atualiza a assinatura mensal recorrente da empresa no Asaas e obtém o link de pagamento.
     */
    @Transactional
    public Optional<String> criarOuAtualizarAssinatura(Empresa empresa, PlanoSaaS novoPlano) {
        if (!billingEnabled || apiKey == null || apiKey.isBlank()) {
            log.warn("Integração Asaas desativada ou API Key não configurada.");
            return Optional.empty();
        }

        String customerId = obterOuCriarCliente(empresa);
        if (customerId == null || customerId.isBlank()) {
            log.error("Não foi possível gerar assinatura no Asaas: customerId nulo");
            return Optional.empty();
        }

        try {
            BigDecimal valor = novoPlano.getPrecoMensal();
            String subscriptionId = empresa.getAsaasSubscriptionId();

            // Caso já possua assinatura ativa, atualiza o valor do plano
            if (subscriptionId != null && !subscriptionId.isBlank()) {
                try {
                    Map<String, Object> updateBody = new HashMap<>();
                    updateBody.put("value", valor);
                    updateBody.put("description", "Assinatura Mensal NEXI PRO - " + novoPlano.getNomeExibicao());

                    HttpEntity<Map<String, Object>> updateReq = new HttpEntity<>(updateBody, criarHeadersAutenticados());
                    restTemplate.postForEntity(apiUrl + "/subscriptions/" + subscriptionId, updateReq, String.class);
                    log.info("Assinatura Asaas ID={} atualizada para novo plano {}", subscriptionId, novoPlano);
                } catch (Exception e) {
                    log.warn("Falha ao atualizar assinatura existente {}, tentando criar nova: {}", subscriptionId, e.getMessage());
                    subscriptionId = null;
                }
            }

            // Cria nova assinatura se não existia ou falhou a atualização
            if (subscriptionId == null || subscriptionId.isBlank()) {
                Map<String, Object> subBody = new HashMap<>();
                subBody.put("customer", customerId);
                subBody.put("billingType", "UNDEFINED"); // Permite cliente pagar via Pix, Cartão ou Boleto
                subBody.put("value", valor);
                subBody.put("nextDueDate", LocalDate.now().plusDays(1).toString());
                subBody.put("cycle", "MONTHLY");
                subBody.put("description", "Assinatura Mensal NEXI PRO - " + novoPlano.getNomeExibicao());
                subBody.put("externalReference", "empresa_" + empresa.getId());

                HttpEntity<Map<String, Object>> subReq = new HttpEntity<>(subBody, criarHeadersAutenticados());
                ResponseEntity<String> subResp = restTemplate.postForEntity(apiUrl + "/subscriptions", subReq, String.class);

                if (subResp.getStatusCode().is2xxSuccessful() && subResp.getBody() != null) {
                    JsonNode subJson = objectMapper.readTree(subResp.getBody());
                    subscriptionId = subJson.path("id").asText();
                    empresa.setAsaasSubscriptionId(subscriptionId);
                    log.info("Nova Assinatura Asaas criada: ID={} para empresa ID={}", subscriptionId, empresa.getId());
                }
            }

            // Atualiza o plano na empresa
            empresa.setPlano(novoPlano);

            // Busca a fatura / link de pagamento mais recente da assinatura
            if (subscriptionId != null && !subscriptionId.isBlank()) {
                String linkFatura = buscarLinkFaturaMaisRecente(subscriptionId);
                if (linkFatura != null && !linkFatura.isBlank()) {
                    empresa.setLinkFaturaAtual(linkFatura);
                }
            }

            empresaRepository.save(empresa);
            return Optional.ofNullable(empresa.getLinkFaturaAtual());

        } catch (HttpStatusCodeException hse) {
            log.error("Erro HTTP ao criar assinatura no Asaas: status={}, body={}", hse.getStatusCode(), hse.getResponseBodyAsString());
            return Optional.empty();
        } catch (Exception e) {
            log.error("Erro ao processar assinatura no Asaas: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Gera uma cobrança pontual de R$ 1,00 para validação real ponta a ponta do fluxo de pagamento Pix.
     */
    @Transactional
    public Optional<String> criarCobrancaTesteUmReal(Empresa empresa) {
        if (!billingEnabled || apiKey == null || apiKey.isBlank()) {
            log.warn("Integração Asaas desativada ou API Key não configurada.");
            return Optional.empty();
        }

        String customerId = obterOuCriarCliente(empresa);
        if (customerId == null || customerId.isBlank()) {
            log.error("Não foi possível gerar cobrança de teste: customerId nulo");
            return Optional.empty();
        }

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("customer", customerId);
            body.put("billingType", "UNDEFINED"); // Aceita Pix ou Cartão
            body.put("value", new BigDecimal("1.00"));
            body.put("dueDate", LocalDate.now().plusDays(1).toString());
            body.put("description", "Teste de Validação NEXI PRO - R$ 1,00");
            body.put("externalReference", "empresa_" + empresa.getId());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, criarHeadersAutenticados());
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "/payments", request, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                String invoiceUrl = root.path("invoiceUrl").asText(null);
                if (invoiceUrl != null && !invoiceUrl.isBlank()) {
                    empresa.setLinkFaturaAtual(invoiceUrl);
                    empresaRepository.save(empresa);
                    log.info("Fatura de teste R$ 1,00 gerada com sucesso para empresa ID={}: {}", empresa.getId(), invoiceUrl);
                    return Optional.of(invoiceUrl);
                }
            }
        } catch (HttpStatusCodeException hse) {
            log.error("Erro HTTP ao gerar cobrança de R$ 1,00 no Asaas: status={}, body={}", 
                    hse.getStatusCode(), hse.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Erro ao gerar cobrança de teste no Asaas: {}", e.getMessage(), e);
        }

        return Optional.empty();
    }

    /**
     * Busca o link da fatura mais recente gerada para a assinatura no Asaas.
     */
    public String buscarLinkFaturaMaisRecente(String subscriptionId) {
        try {
            String url = apiUrl + "/subscriptions/" + subscriptionId + "/payments";
            ResponseEntity<String> resp = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(criarHeadersAutenticados()),
                    String.class
            );

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode root = objectMapper.readTree(resp.getBody());
                JsonNode data = root.path("data");
                if (data.isArray() && !data.isEmpty()) {
                    for (JsonNode payment : data) {
                        String status = payment.path("status").asText();
                        if ("PENDING".equalsIgnoreCase(status) || "OVERDUE".equalsIgnoreCase(status)) {
                            String invoiceUrl = payment.path("invoiceUrl").asText(null);
                            if (invoiceUrl != null && !invoiceUrl.isBlank()) {
                                return invoiceUrl;
                            }
                            String bankSlipUrl = payment.path("bankSlipUrl").asText(null);
                            if (bankSlipUrl != null && !bankSlipUrl.isBlank()) {
                                return bankSlipUrl;
                            }
                        }
                    }
                    return data.get(0).path("invoiceUrl").asText(null);
                }
            }
        } catch (Exception e) {
            log.warn("Não foi possível obter pagamentos da assinatura {}: {}", subscriptionId, e.getMessage());
        }
        return null;
    }

    private HttpHeaders criarHeadersAutenticados() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access_token", apiKey != null ? apiKey.trim() : "");
        headers.set("User-Agent", "NEXIPRO-SaaS/1.0");
        return headers;
    }
}
