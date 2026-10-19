package com.beautysalon.service;

import com.beautysalon.DTO.billing.AsaasWebhookDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.StatusAssinatura;
import com.beautysalon.repository.EmpresaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private BillingService billingService;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("Studio Elegance")
                .slug("studio-elegance")
                .statusAssinatura(StatusAssinatura.TRIAL)
                .dataExpiracaoTrial(LocalDateTime.now().plusDays(2))
                .build();
    }

    @Test
    @DisplayName("Deve confirmar pagamento e ativar assinatura estendendo prazo em 30 dias")
    void deveConfirmarPagamentoEAtivarAssinatura() {
        // Arrange
        String subId = "sub_987654";
        String cusId = "cus_123456";

        when(empresaRepository.findByAsaasSubscriptionId(subId)).thenReturn(Optional.of(empresa));

        AsaasWebhookDTO webhook = AsaasWebhookDTO.builder()
                .event("PAYMENT_CONFIRMED")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .subscription(subId)
                        .customer(cusId)
                        .value(new BigDecimal("149.00"))
                        .invoiceUrl("https://asaas.com/i/abc1234")
                        .build())
                .build();

        // Act
        boolean processado = billingService.processarWebhook(null, webhook);

        // Assert
        assertTrue(processado);
        assertEquals(StatusAssinatura.ATIVA, empresa.getStatusAssinatura());
        assertNotNull(empresa.getDataFimAssinatura());
        assertTrue(empresa.getDataFimAssinatura().isAfter(LocalDateTime.now().plusDays(28)));
        assertEquals("https://asaas.com/i/abc1234", empresa.getLinkFaturaAtual());
        assertEquals(cusId, empresa.getAsaasCustomerId());
        verify(empresaRepository, times(1)).save(empresa);
    }

    @Test
    @DisplayName("Deve marcar empresa como INADIMPLENTE em caso de PAYMENT_OVERDUE")
    void deveMarcarInadimplenteEmPaymentOverdue() {
        // Arrange
        String cusId = "cus_123456";
        empresa.setAsaasCustomerId(cusId);
        empresa.setStatusAssinatura(StatusAssinatura.ATIVA);

        when(empresaRepository.findByAsaasCustomerId(cusId)).thenReturn(Optional.of(empresa));

        AsaasWebhookDTO webhook = AsaasWebhookDTO.builder()
                .event("PAYMENT_OVERDUE")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .customer(cusId)
                        .value(new BigDecimal("149.00"))
                        .build())
                .build();

        // Act
        boolean processado = billingService.processarWebhook(null, webhook);

        // Assert
        assertTrue(processado);
        assertEquals(StatusAssinatura.INADIMPLENTE, empresa.getStatusAssinatura());
        verify(empresaRepository, times(1)).save(empresa);
    }

    @Test
    @DisplayName("Deve suspender assinatura em caso de estorno ou cancelamento de pagamento")
    void deveSuspenderEmEstorno() {
        // Arrange
        String cusId = "cus_123456";
        when(empresaRepository.findByAsaasCustomerId(cusId)).thenReturn(Optional.of(empresa));

        AsaasWebhookDTO webhook = AsaasWebhookDTO.builder()
                .event("PAYMENT_REFUNDED")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .customer(cusId)
                        .build())
                .build();

        // Act
        boolean processado = billingService.processarWebhook(null, webhook);

        // Assert
        assertTrue(processado);
        assertEquals(StatusAssinatura.CANCELADA, empresa.getStatusAssinatura());
        verify(empresaRepository, times(1)).save(empresa);
    }

    @Test
    @DisplayName("Deve retornar false se empresa não for localizada pelo gateway")
    void deveRetornarFalseSeEmpresaNaoEncontrada() {
        // Arrange
        when(empresaRepository.findByAsaasCustomerId("cus_inexistente")).thenReturn(Optional.empty());

        AsaasWebhookDTO webhook = AsaasWebhookDTO.builder()
                .event("PAYMENT_CONFIRMED")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .customer("cus_inexistente")
                        .build())
                .build();

        // Act
        boolean processado = billingService.processarWebhook(null, webhook);

        // Assert
        assertFalse(processado);
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar SecurityException se token de webhook for inválido")
    void deveLancarSecurityExceptionSeTokenInvalido() {
        // Arrange
        ReflectionTestUtils.setField(billingService, "configuredWebhookToken", "segredo-forte-123");

        AsaasWebhookDTO webhook = AsaasWebhookDTO.builder()
                .event("PAYMENT_CONFIRMED")
                .payment(AsaasWebhookDTO.AsaasPaymentDTO.builder()
                        .customer("cus_123456")
                        .build())
                .build();

        // Act & Assert
        assertThrows(SecurityException.class, () -> billingService.processarWebhook("token-incorreto", webhook));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve vincular dados da assinatura à empresa")
    void deveVincularAssinatura() {
        // Arrange
        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        // Act
        billingService.vincularAssinatura(1L, "cus_abc", "sub_xyz", "https://asaas.com/i/1");

        // Assert
        assertEquals("cus_abc", empresa.getAsaasCustomerId());
        assertEquals("sub_xyz", empresa.getAsaasSubscriptionId());
        assertEquals("https://asaas.com/i/1", empresa.getLinkFaturaAtual());
        verify(empresaRepository, times(1)).save(empresa);
    }
}
