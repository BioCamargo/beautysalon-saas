package com.beautysalon.Controller;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.model.Cliente;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.AnamneseService;
import com.beautysalon.service.SmsService;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private static final Long EMPRESA_ID = 1L;
    private static final String SLUG = "studio-vip";

    @Mock
    private ClienteService clienteService;

    @Mock
    private SmsService smsService;

    @Mock
    private AnamneseService anamneseService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteController controller;

    private Cliente clienteMock;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(SLUG, EMPRESA_ID, "Studio VIP");
        clienteMock = new Cliente();
        clienteMock.setId(10L);
        clienteMock.setNome("Juliana Paes");
        clienteMock.setTelefone("11988887777");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve listar todos os clientes do tenant")
    void deveListarClientes() {
        when(clienteService.listarTodos()).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.listar(SLUG, model);

        assertEquals("clientes/list", view);
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertTrue(model.containsAttribute("clientes"));
    }

    @Test
    @DisplayName("Deve renderizar formulário para novo cliente")
    void deveRenderizarFormularioNovoCliente() {
        Model model = new ConcurrentModel();
        String view = controller.novoCliente(SLUG, model);

        assertEquals("clientes/form", view);
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertTrue(model.containsAttribute("cliente"));
    }

    @Test
    @DisplayName("Deve renderizar tela de ficha de anamnese do cliente")
    void deveRenderizarTelaAnamnese() {
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(clienteMock));
        when(anamneseService.buscarHistoricoPorCliente(10L)).thenReturn(Collections.emptyList());
        when(userRepository.findAllByEmpresaIdAndAtivoTrue(EMPRESA_ID)).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.viewAnamnese(SLUG, 10L, model);

        assertEquals("clientes/anamnese", view);
        assertEquals(clienteMock, model.getAttribute("cliente"));
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertTrue(model.containsAttribute("historicoAnamnese"));
        assertTrue(model.containsAttribute("profissionais"));
    }

    @Test
    @DisplayName("Deve salvar novo registro de anamnese com sucesso")
    void deveSalvarAnamnese() {
        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.salvarAnamnese(
                SLUG,
                10L,
                "Luzes e Matização",
                "50g Pó + 100ml OX 30v",
                "Sem sensibilidade",
                "Manter hidratação",
                "http://foto-antes.jpg",
                "http://foto-depois.jpg",
                "data:image/png;base64,assinaturaSimulada",
                2L,
                redirectAttributes
        );

        assertEquals("redirect:/studio-vip/clientes/10/anamnese", view);
        verify(anamneseService, times(1)).salvarAnamnese(
                eq(10L),
                eq("Luzes e Matização"),
                eq("50g Pó + 100ml OX 30v"),
                eq("Sem sensibilidade"),
                eq("Manter hidratação"),
                eq("http://foto-antes.jpg"),
                eq("http://foto-depois.jpg"),
                eq("data:image/png;base64,assinaturaSimulada"),
                eq(2L)
        );
        assertEquals("Ficha química e fotos registradas com sucesso!", redirectAttributes.getFlashAttributes().get("mensagemSucesso"));
    }
}
