package com.beautysalon.service;

import com.beautysalon.DTO.billing.AsaasWebhookDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.StatusAssinatura;
import com.beautysalon.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final EmpresaRepository empresaRepository;

    @Value("${nexipro.saas.billing.webhook-token:}")
    private String configuredWebhookToken;

    /**
     * Processa eventos recebidos do webhook do Asaas (pagamentos, renovações e inadimplência).
     */
    @Transactional
    public boolean processarWebhook(String requestTokenHeader, AsaasWebhookDTO webhook) {
        validarTokenWebhook(requestTokenHeader);

        if (webhook == null || webhook.getPayment() == null) {
            log.warn("Payload de webhook Asaas nulo ou sem dados de pagamento");
            return false;
        }

        AsaasWebhookDTO.AsaasPaymentDTO payment = webhook.getPayment();
        String customerId = payment.getCustomer();
        String subscriptionId = payment.getSubscription();
        String event = webhook.getEvent();

        log.info("Recebido webhook Asaas: evento={}, customer={}, subscription={}", event, customerId, subscriptionId);

        Optional<Empresa> empresaOpt = buscarEmpresaPorGateway(customerId, subscriptionId);
        if (empresaOpt.isEmpty()) {
            log.warn("Nenhuma empresa encontrada para customerId={} ou subscriptionId={}", customerId, subscriptionId);
            return false;
        }

        Empresa empresa = empresaOpt.get();

        // Vincula identificadores do gateway caso ainda não estejam salvos
        if (customerId != null && (empresa.getAsaasCustomerId() == null || empresa.getAsaasCustomerId().isBlank())) {
            empresa.setAsaasCustomerId(customerId);
        }
        if (subscriptionId != null && (empresa.getAsaasSubscriptionId() == null || empresa.getAsaasSubscriptionId().isBlank())) {
            empresa.setAsaasSubscriptionId(subscriptionId);
        }
        if (payment.getInvoiceUrl() != null && !payment.getInvoiceUrl().isBlank()) {
            empresa.setLinkFaturaAtual(payment.getInvoiceUrl());
        }

        if (event == null) {
            empresaRepository.save(empresa);
            return true;
        }

        switch (event.toUpperCase()) {
            case "PAYMENT_CONFIRMED":
            case "PAYMENT_RECEIVED":
                aplicarRenovacaoAssinatura(empresa);
                break;

            case "PAYMENT_OVERDUE":
                log.warn("Assinatura inadimplente registrada para empresa ID={} ({})", empresa.getId(), empresa.getSlug());
                empresa.setStatusAssinatura(StatusAssinatura.INADIMPLENTE);
                break;

            case "PAYMENT_REFUNDED":
            case "PAYMENT_DELETED":
                log.warn("Pagamento estornado/cancelado para empresa ID={} ({})", empresa.getId(), empresa.getSlug());
                empresa.setStatusAssinatura(StatusAssinatura.CANCELADA);
                break;

            default:
                log.info("Evento Asaas ignorado para transição de status: {}", event);
                break;
        }

        empresaRepository.save(empresa);
        return true;
    }

    private void validarTokenWebhook(String token) {
        if (configuredWebhookToken != null && !configuredWebhookToken.isBlank()) {
            if (token == null || !token.trim().equals(configuredWebhookToken.trim())) {
                log.error("Tentativa de acesso ao webhook de billing com token inválido");
                throw new SecurityException("Token de webhook inválido");
            }
        }
    }

    private Optional<Empresa> buscarEmpresaPorGateway(String customerId, String subscriptionId) {
        if (subscriptionId != null && !subscriptionId.isBlank()) {
            Optional<Empresa> porSub = empresaRepository.findByAsaasSubscriptionId(subscriptionId);
            if (porSub.isPresent()) return porSub;
        }
        if (customerId != null && !customerId.isBlank()) {
            return empresaRepository.findByAsaasCustomerId(customerId);
        }
        return Optional.empty();
    }

    private void aplicarRenovacaoAssinatura(Empresa empresa) {
        empresa.setStatusAssinatura(StatusAssinatura.ATIVA);
        LocalDateTime agora = LocalDateTime.now();

        if (empresa.getDataFimAssinatura() == null || empresa.getDataFimAssinatura().isBefore(agora)) {
            empresa.setDataFimAssinatura(agora.plusDays(30));
        } else {
            empresa.setDataFimAssinatura(empresa.getDataFimAssinatura().plusDays(30));
        }

        log.info("Assinatura da empresa ID={} ({}) ativada/renovada até {}",
                empresa.getId(), empresa.getSlug(), empresa.getDataFimAssinatura());
    }

    @Transactional
    public void vincularAssinatura(Long empresaId, String asaasCustomerId, String asaasSubscriptionId, String linkFatura) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada com ID: " + empresaId));

        if (asaasCustomerId != null) empresa.setAsaasCustomerId(asaasCustomerId);
        if (asaasSubscriptionId != null) empresa.setAsaasSubscriptionId(asaasSubscriptionId);
        if (linkFatura != null) empresa.setLinkFaturaAtual(linkFatura);

        empresaRepository.save(empresa);
    }
}
