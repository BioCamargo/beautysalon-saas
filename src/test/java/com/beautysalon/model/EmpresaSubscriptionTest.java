package com.beautysalon.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmpresaSubscriptionTest {

    @Test
    @DisplayName("Deve validar assinatura ativa com data futura")
    void deveValidarAssinaturaAtivaComDataFutura() {
        Empresa empresa = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.ATIVA)
                .dataFimAssinatura(LocalDateTime.now().plusDays(15))
                .build();

        assertTrue(empresa.isAssinaturaValida());
        assertFalse(empresa.isInadimplente());
        assertFalse(empresa.isBloqueioSuaveAtivo());
        assertFalse(empresa.isToleranciaInadimplenteAtiva());
    }

    @Test
    @DisplayName("Deve permitir acesso em período de carência (até 3 dias de tolerância) para inadimplentes")
    void devePermitirAcessoDuranteToleranciaInadimplente() {
        // Vencido há 1 dia (dentro da carência de 3 dias)
        Empresa empresa = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.INADIMPLENTE)
                .dataFimAssinatura(LocalDateTime.now().minusDays(1))
                .build();

        assertTrue(empresa.isInadimplente());
        assertTrue(empresa.isToleranciaInadimplenteAtiva());
        assertFalse(empresa.isBloqueioSuaveAtivo());
        assertTrue(empresa.getDiasRestantesTolerancia() > 0);
    }

    @Test
    @DisplayName("Deve ativar bloqueio suave após expirar a carência de 3 dias de inadimplência")
    void deveAtivarBloqueioSuaveAposToleranciaInadimplente() {
        // Vencido há 4 dias (passou da carência de 3 dias)
        Empresa empresa = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.INADIMPLENTE)
                .dataFimAssinatura(LocalDateTime.now().minusDays(4))
                .build();

        assertTrue(empresa.isInadimplente());
        assertFalse(empresa.isToleranciaInadimplenteAtiva());
        assertTrue(empresa.isBloqueioSuaveAtivo());
        assertEquals(0, empresa.getDiasRestantesTolerancia());
    }

    @Test
    @DisplayName("Deve ativar bloqueio suave para assinaturas canceladas ou expiradas")
    void deveAtivarBloqueioSuaveParaCanceladaOuExpirada() {
        Empresa cancelada = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.CANCELADA)
                .build();

        Empresa expirada = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.EXPIRADA)
                .build();

        assertTrue(cancelada.isBloqueioSuaveAtivo());
        assertTrue(expirada.isBloqueioSuaveAtivo());
    }

    @Test
    @DisplayName("Deve ativar bloqueio suave se o período de teste grátis (trial) estiver expirado")
    void deveAtivarBloqueioSuaveSeTrialExpirado() {
        Empresa empresa = Empresa.builder()
                .ativo(true)
                .statusAssinatura(StatusAssinatura.TRIAL)
                .dataExpiracaoTrial(LocalDateTime.now().minusHours(2))
                .build();

        assertFalse(empresa.isTrialAtivo());
        assertTrue(empresa.isBloqueioSuaveAtivo());
    }
}
