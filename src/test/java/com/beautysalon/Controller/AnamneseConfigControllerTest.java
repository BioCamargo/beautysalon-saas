package com.beautysalon.Controller;

import com.beautysalon.model.AnamneseCampoCustomizado;
import com.beautysalon.model.TipoCampoCustomizado;
import com.beautysalon.service.AnamneseFormBuilderService;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnamneseConfigControllerTest {

    private static final Long EMPRESA_ID = 1L;
    private static final String SLUG = "studio-vip";

    @Mock
    private AnamneseFormBuilderService formBuilderService;

    @InjectMocks
    private AnamneseConfigController controller;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(SLUG, EMPRESA_ID, "Studio VIP");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve renderizar página do Form Builder com lista de campos")
    void deveRenderizarPaginaFormBuilder() {
        AnamneseCampoCustomizado campo = AnamneseCampoCustomizado.builder()
                .id(1L)
                .rotulo("Possui alergias?")
                .tipoCampo(TipoCampoCustomizado.SIM_NAO)
                .build();

        when(formBuilderService.listarTodosDoTenant()).thenReturn(List.of(campo));

        Model model = new ConcurrentModel();
        String view = controller.index(SLUG, model);

        assertEquals("configuracoes/anamnese-builder", view);
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertTrue(model.containsAttribute("campos"));
        assertTrue(model.containsAttribute("tiposCampo"));
    }

    @Test
    @DisplayName("Deve salvar novo campo customizado com sucesso")
    void deveSalvarNovoCampo() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.salvarCampo(
                SLUG,
                null,
                "Usa lentes de contato?",
                TipoCampoCustomizado.SIM_NAO,
                null,
                "Selecione sim ou não",
                true,
                1,
                redirectAttributes
        );

        assertEquals("redirect:/studio-vip/configuracoes/anamnese", view);
        verify(formBuilderService, times(1)).criarOuAtualizarCampo(
                isNull(),
                eq("Usa lentes de contato?"),
                eq(TipoCampoCustomizado.SIM_NAO),
                isNull(),
                eq("Selecione sim ou não"),
                eq(true),
                eq(1)
        );
        assertEquals("Campo da ficha de anamnese salvo com sucesso!", redirectAttributes.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("Deve alternar status de ativação do campo")
    void deveAlternarStatusCampo() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.alternarStatus(SLUG, 5L, redirectAttributes);

        assertEquals("redirect:/studio-vip/configuracoes/anamnese", view);
        verify(formBuilderService, times(1)).alternarStatus(5L);
        assertEquals("Status do campo atualizado com sucesso!", redirectAttributes.getFlashAttributes().get("mensagemSucesso"));
    }

    @Test
    @DisplayName("Deve excluir campo customizado")
    void deveExcluirCampo() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.excluirCampo(SLUG, 5L, redirectAttributes);

        assertEquals("redirect:/studio-vip/configuracoes/anamnese", view);
        verify(formBuilderService, times(1)).excluirCampo(5L);
        assertEquals("Campo excluído com sucesso!", redirectAttributes.getFlashAttributes().get("mensagemSucesso"));
    }
}
