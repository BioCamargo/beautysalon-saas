package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.*;
import com.beautysalon.service.EstoqueService;
import com.beautysalon.service.FidelizacaoService;
import com.beautysalon.service.FinanceiroService;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private FinanceiroService financeiroService;

    @Mock
    private FidelizacaoService fidelizacaoService;

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private ComandaItemRepository comandaItemRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private HomeController controller;

    private static final String SLUG = "studio-nexipro";
    private static final Long EMPRESA_ID = 1L;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve renderizar a Landing Page comercial na rota raiz para visitantes não autenticados")
    void deveRenderizarLandingPageParaVisitanteNaoAutenticado() {
        Model model = new ConcurrentModel();

        String view = controller.root(null, model);

        assertEquals("landing/index", view);
        assertTrue(model.containsAttribute("pageTitle"));
    }

    @Test
    @DisplayName("Deve renderizar a página de Termos de Uso com pageTitle")
    void deveRenderizarPaginaDeTermosDeUso() {
        Model model = new ConcurrentModel();
        String view = controller.termosDeUso(model);

        assertEquals("landing/termos", view);
        assertTrue(model.containsAttribute("pageTitle"));
    }

    @Test
    @DisplayName("Deve renderizar a página de Política de Privacidade & LGPD com pageTitle")
    void deveRenderizarPaginaDePoliticaDePrivacidade() {
        Model model = new ConcurrentModel();
        String view = controller.politicaDePrivacidade(model);

        assertEquals("landing/privacidade", view);
        assertTrue(model.containsAttribute("pageTitle"));
    }

    @Test
    @DisplayName("Deve redirecionar para o dashboard do salão quando o usuário já estiver logado")
    void deveRedirecionarParaDashboardQuandoAutenticado() {
        Empresa empresa = Empresa.builder().id(EMPRESA_ID).slug(SLUG).nome("Studio NEXI PRO").build();
        User user = User.builder().id(10L).username("admin").empresa(empresa).build();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        Model model = new ConcurrentModel();
        String view = controller.root(authentication, model);

        assertEquals("redirect:/" + SLUG + "/home", view);
    }

    @Test
    @DisplayName("Deve carregar métricas operacionais e gráficos no Dashboard do salão")
    void deveCarregarMetricasNoDashboard() {
        when(clienteRepository.countByEmpresaId(EMPRESA_ID)).thenReturn(25L);
        when(servicoRepository.countByEmpresaId(EMPRESA_ID)).thenReturn(8L);
        when(userRepository.countByEmpresaId(EMPRESA_ID)).thenReturn(4L);
        when(estoqueService.calcularValorParadoEmEstoque()).thenReturn(new BigDecimal("1500.00"));
        when(estoqueService.contarEstoqueBaixo()).thenReturn(2L);
        when(fidelizacaoService.listarAniversariantesDoMes(anyInt())).thenReturn(Collections.emptyList());
        when(fidelizacaoService.buscarClientesEmRiscoRetorno(anyInt())).thenReturn(Collections.emptyList());
        when(financeiroService.buscarCaixaAberto()).thenReturn(Optional.empty());
        when(comandaItemRepository.findTopItensMaisVendidos(EMPRESA_ID)).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.dashboard(SLUG, model);

        assertEquals("home/index", view);
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertEquals(25L, model.getAttribute("totalClientes"));
        assertEquals(8L, model.getAttribute("totalServicos"));
        assertEquals(4L, model.getAttribute("totalProfissionais"));
        assertEquals(new BigDecimal("1500.00"), model.getAttribute("valorParadoEstoque"));
        assertEquals(2L, model.getAttribute("qtdEstoqueBaixo"));
        assertTrue(model.containsAttribute("diasLabels"));
        assertTrue(model.containsAttribute("faturamentoDias"));
    }
}
