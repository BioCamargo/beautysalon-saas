package com.beautysalon.Controller;

import com.beautysalon.DTO.DREDTO;
import com.beautysalon.DTO.RentabilidadeServicoDTO;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.User;
import com.beautysalon.repository.ComandaItemRepository;
import com.beautysalon.repository.ComandaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.EstoqueService;
import com.beautysalon.service.FinanceiroService;
import com.beautysalon.service.RelatorioService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelatorioControllerTest {

    private static final Long EMPRESA_ID = 1L;
    private static final String SLUG = "studio-vip";

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private ComandaItemRepository comandaItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private FinanceiroService financeiroService;

    @Mock
    private RelatorioService relatorioService;

    @InjectMocks
    private RelatorioController relatorioController;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve carregar dashboard de relatórios com DRE, rentabilidade e métricas de pagamento")
    void deveCarregarDashboardRelatoriosComDRE() {
        Model model = new ConcurrentModel();

        DREDTO mockDre = DREDTO.builder()
                .receitaBrutaTotal(new BigDecimal("10000.00"))
                .receitaLiquida(new BigDecimal("9500.00"))
                .margemContribuicao(new BigDecimal("6000.00"))
                .resultadoLiquido(new BigDecimal("4500.00"))
                .lucratividadePercentual(new BigDecimal("47.37"))
                .build();

        RentabilidadeServicoDTO mockRentabilidade = RentabilidadeServicoDTO.builder()
                .nomeServico("Coloração")
                .quantidadeExecutada(20)
                .faturamentoTotal(new BigDecimal("3000.00"))
                .lucroLiquidoTotal(new BigDecimal("2100.00"))
                .margemLucroPercentual(new BigDecimal("70.00"))
                .build();

        when(comandaRepository.sumFaturamentoPorPeriodo(eq(EMPRESA_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("10000.00"));
        when(comandaRepository.sumComissoesPorPeriodo(eq(EMPRESA_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("2000.00"));
        when(comandaRepository.countAtendimentosPorPeriodo(eq(EMPRESA_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(50L);
        when(estoqueService.calcularValorParadoEmEstoque()).thenReturn(new BigDecimal("1500.00"));
        when(financeiroService.calcularDREPeriodo(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(mockDre);
        when(financeiroService.calcularRentabilidadeServicos(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(mockRentabilidade));

        String view = relatorioController.index(SLUG, LocalDate.now().minusDays(30), LocalDate.now(), "financeiro", model);

        assertEquals("relatorios/index", view);
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertEquals("financeiro", model.getAttribute("activeTab"));
        assertEquals(mockDre, model.getAttribute("dre"));
        assertNotNull(model.getAttribute("rentabilidadeServicos"));
        assertEquals(new BigDecimal("10000.00"), model.getAttribute("faturamentoTotal"));
        assertEquals(new BigDecimal("200.00"), model.getAttribute("ticketMedio")); // 10000 / 50 = 200
    }

    @Test
    @DisplayName("Deve carregar extrato de comissões individual de profissional")
    void deveCarregarExtratoComissoesProfissional() {
        Model model = new ConcurrentModel();
        User profissional = User.builder().id(5L).nome("Camila Manicure").build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(profissional));
        when(comandaItemRepository.findItensPorProfissionalEPeriodo(eq(EMPRESA_ID), eq(5L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(comandaItemRepository.sumComissaoProfissional(eq(EMPRESA_ID), eq(5L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("750.00"));

        String view = relatorioController.extratoProfissional(SLUG, 5L, LocalDate.now().minusDays(15), LocalDate.now(), model);

        assertEquals("relatorios/comissao-detalhe", view);
        assertEquals(profissional, model.getAttribute("profissional"));
        assertEquals(new BigDecimal("750.00"), model.getAttribute("totalComissao"));
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
    }
}
