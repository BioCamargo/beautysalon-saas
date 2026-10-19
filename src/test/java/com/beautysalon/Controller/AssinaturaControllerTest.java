package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.PlanoSaaS;
import com.beautysalon.model.StatusAssinatura;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssinaturaControllerTest {

    private static final Long EMPRESA_ID = 1L;
    private static final String SLUG = "studio-elegance";

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private AssinaturaController controller;

    private Empresa empresa;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(SLUG, EMPRESA_ID, "Studio Elegance");
        empresa = Empresa.builder()
                .id(EMPRESA_ID)
                .nome("Studio Elegance")
                .slug(SLUG)
                .plano(PlanoSaaS.STUDIO)
                .statusAssinatura(StatusAssinatura.ATIVA)
                .dataFimAssinatura(LocalDateTime.now().plusDays(25))
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve exibir página de gestão da assinatura com planos e dados da empresa")
    void deveExibirGestaoAssinatura() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));

        Model model = new ConcurrentModel();
        String view = controller.exibirGestaoAssinatura(SLUG, model);

        assertEquals("configuracoes/assinatura", view);
        assertEquals(empresa, model.getAttribute("empresa"));
        assertEquals("assinatura", model.getAttribute("activePage"));
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertNotNull(model.getAttribute("planos"));
    }

    @Test
    @DisplayName("Deve alterar plano do estúdio e redirecionar com mensagem de sucesso")
    void deveAlterarPlanoComSucesso() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.alterarPlano(SLUG, PlanoSaaS.VIP, redirectAttributes);

        assertEquals("redirect:/studio-elegance/assinatura", view);
        assertEquals(PlanoSaaS.VIP, empresa.getPlano());
        verify(empresaRepository, times(1)).save(empresa);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("sucesso"));
    }

    @Test
    @DisplayName("Deve atualizar identificadores de gateway Asaas da empresa")
    void deveAtualizarIdentificadoresGateway() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresa));

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();
        String view = controller.atualizarDadosGateway(
                SLUG,
                "cus_999888",
                "sub_777666",
                "https://asaas.com/i/xyz",
                redirectAttributes
        );

        assertEquals("redirect:/studio-elegance/assinatura", view);
        assertEquals("cus_999888", empresa.getAsaasCustomerId());
        assertEquals("sub_777666", empresa.getAsaasSubscriptionId());
        assertEquals("https://asaas.com/i/xyz", empresa.getLinkFaturaAtual());
        verify(empresaRepository, times(1)).save(empresa);
        assertTrue(redirectAttributes.getFlashAttributes().containsKey("sucesso"));
    }
}
