package com.beautysalon.service;

import com.beautysalon.model.*;
import com.beautysalon.repository.*;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinanceiroServiceTest {

    @Mock
    private CaixaRepository caixaRepository;

    @Mock
    private ComandaRepository comandaRepository;

    @Mock
    private MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;

    @Mock
    private ServicoInsumoRepository servicoInsumoRepository;

    @Mock
    private EstoqueService estoqueService;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private PagamentoComandaRepository pagamentoComandaRepository;

    @InjectMocks
    private FinanceiroService financeiroService;

    private final Long EMPRESA_ID = 1L;
    private Empresa empresaMock;
    private User operadorMock;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
        empresaMock = Empresa.builder().id(EMPRESA_ID).nome("Lumora Studio").slug("lumora").build();
        operadorMock = User.builder().id(99L).nome("Operador Caixa").build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve abrir caixa com saldo inicial com sucesso")
    void deveAbrirCaixaComSucesso() {
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.empty());
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(caixaRepository.save(any(Caixa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal saldoInicial = new BigDecimal("150.00");
        Caixa caixa = financeiroService.abrirCaixa(saldoInicial, "Abertura do dia", operadorMock);

        assertNotNull(caixa);
        assertEquals("ABERTO", caixa.getStatus());
        assertEquals(saldoInicial, caixa.getSaldoInicial());
        assertEquals(saldoInicial, caixa.getSaldoFinalEsperado());
        assertEquals(operadorMock, caixa.getOperadorAbertura());
        verify(caixaRepository, times(1)).save(any(Caixa.class));
    }

    @Test
    @DisplayName("Não deve permitir abrir caixa se já houver um aberto")
    void naoDevePermitirAbrirCaixaDuplicado() {
        Caixa caixaExistente = Caixa.builder().id(1L).status("ABERTO").build();
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.of(caixaExistente));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                financeiroService.abrirCaixa(new BigDecimal("100.00"), "Tentativa duplicada", operadorMock)
        );

        assertTrue(ex.getMessage().contains("Já existe um caixa aberto"));
        verify(caixaRepository, never()).save(any(Caixa.class));
    }

    @Test
    @DisplayName("Deve fechar caixa e calcular a diferença de valores corretamente")
    void deveFecharCaixaECalcularDiferenca() {
        Caixa caixa = Caixa.builder()
                .id(1L)
                .status("ABERTO")
                .saldoInicial(new BigDecimal("100.00"))
                .totalEntradas(new BigDecimal("400.00"))
                .totalSaidas(new BigDecimal("50.00"))
                .saldoFinalEsperado(new BigDecimal("450.00"))
                .empresa(empresaMock)
                .build();

        when(caixaRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(caixa));
        when(caixaRepository.save(any(Caixa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BigDecimal saldoContado = new BigDecimal("460.00"); // Sobrou 10
        Caixa fechado = financeiroService.fecharCaixa(1L, saldoContado, "Fechamento noturno", operadorMock);

        assertEquals("FECHADO", fechado.getStatus());
        assertEquals(saldoContado, fechado.getSaldoFinalContado());
        assertEquals(new BigDecimal("450.00"), fechado.getSaldoFinalEsperado());
        assertEquals(new BigDecimal("10.00"), fechado.getDiferencaFechamento()); // Diferença positiva
        verify(caixaRepository, times(1)).save(caixa);
    }

    @Test
    @DisplayName("Deve criar comanda e importar serviços e profissional do agendamento")
    void deveCriarComandaComAgendamento() {
        Servico servico = Servico.builder().id(10L).nome("Corte").preco(new BigDecimal("80.00")).build();
        User profissional = User.builder().id(20L).nome("Cabeleireira").percentualComissao(new BigDecimal("40.00")).build();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(100L);
        agendamento.setProfissional(profissional);
        agendamento.setServicos(List.of(servico));

        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.empty());
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comanda comanda = financeiroService.criarComanda(null, "Maria Silva", agendamento, "Comanda agendada");

        assertNotNull(comanda);
        assertEquals("ABERTA", comanda.getStatus());
        assertEquals(1, comanda.getItens().size());
        assertEquals(new BigDecimal("80.00"), comanda.getValorTotal());
        assertEquals(new BigDecimal("32.00"), comanda.getTotalComissoes()); // 40% de 80 = 32
    }

    @Test
    @DisplayName("Deve fechar comanda com sucesso, dar baixa no caixa e atualizar status do agendamento")
    void deveFecharComandaComSucesso() {
        Caixa caixa = Caixa.builder()
                .id(1L)
                .status("ABERTO")
                .saldoInicial(new BigDecimal("100.00"))
                .totalEntradas(BigDecimal.ZERO)
                .totalSaidas(BigDecimal.ZERO)
                .saldoFinalEsperado(new BigDecimal("100.00"))
                .empresa(empresaMock)
                .build();

        Agendamento agendamento = new Agendamento();
        agendamento.setId(100L);
        agendamento.setStatus("EM_ATENDIMENTO");

        Comanda comanda = Comanda.builder()
                .id(50L)
                .numeroComanda("CMD-50")
                .status("ABERTA")
                .agendamento(agendamento)
                .itens(new ArrayList<>())
                .empresa(empresaMock)
                .build();

        ComandaItem item = ComandaItem.builder()
                .comanda(comanda)
                .tipo("SERVICO")
                .precoUnitario(new BigDecimal("150.00"))
                .quantidade(1)
                .valorTotal(new BigDecimal("150.00"))
                .build();
        comanda.getItens().add(item);
        comanda.recalcularTotais();

        when(comandaRepository.findByIdAndEmpresaId(50L, EMPRESA_ID)).thenReturn(Optional.of(comanda));
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.of(caixa));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comanda paga = financeiroService.fecharComanda(50L, "PIX", BigDecimal.ZERO, BigDecimal.ZERO, null, operadorMock);

        assertEquals("PAGA", paga.getStatus());
        assertEquals("PIX", paga.getFormaPagamento());
        assertEquals("CONCLUIDO", agendamento.getStatus());
        assertEquals(new BigDecimal("150.00"), caixa.getTotalEntradas());
        assertEquals(new BigDecimal("250.00"), caixa.getSaldoFinalEsperado());

        verify(caixaRepository, times(1)).save(caixa);
        verify(agendamentoRepository, times(1)).save(agendamento);
        verify(comandaRepository, times(1)).save(comanda);
    }

    @Test
    @DisplayName("Deve adicionar pagamento parcial em comanda (Split de Pagamento)")
    void deveAdicionarPagamentoParcialComanda() {
        Comanda comanda = Comanda.builder()
                .id(70L)
                .status("ABERTA")
                .valorTotal(new BigDecimal("200.00"))
                .empresa(empresaMock)
                .build();

        when(comandaRepository.findByIdAndEmpresaId(70L, EMPRESA_ID)).thenReturn(Optional.of(comanda));
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(pagamentoComandaRepository.save(any(PagamentoComanda.class))).thenAnswer(i -> i.getArgument(0));

        PagamentoComanda pg = financeiroService.adicionarPagamentoComanda(70L, "PIX", new BigDecimal("100.00"), "Entrada Pix");

        assertNotNull(pg);
        assertEquals(new BigDecimal("100.00"), pg.getValor());
        assertEquals("PIX", pg.getFormaPagamento());
        assertEquals(1, comanda.getPagamentos().size());
        assertEquals(new BigDecimal("100.00"), comanda.getTotalPago());
        assertEquals(new BigDecimal("100.00"), comanda.getSaldoRestante());
    }

    @Test
    @DisplayName("Deve calcular DRE simplificado e lucratividade com precisão")
    void deveCalcularDREPeriodo() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fim = LocalDateTime.now();

        Comanda c1 = Comanda.builder()
                .id(1L)
                .status("PAGA")
                .subtotalServicos(new BigDecimal("1000.00"))
                .subtotalProdutos(new BigDecimal("200.00"))
                .desconto(new BigDecimal("50.00"))
                .totalComissoes(new BigDecimal("300.00"))
                .valorTotal(new BigDecimal("1150.00"))
                .empresa(empresaMock)
                .build();

        when(comandaRepository.findComandasPagasPorPeriodo(EMPRESA_ID, inicio, fim)).thenReturn(List.of(c1));
        when(movimentacaoFinanceiraRepository.findAllByEmpresaIdAndDataHoraBetween(EMPRESA_ID, inicio, fim))
                .thenReturn(List.of());

        var dre = financeiroService.calcularDREPeriodo(inicio, fim);

        assertNotNull(dre);
        assertEquals(new BigDecimal("1200.00"), dre.getReceitaBrutaTotal());
        assertEquals(new BigDecimal("1150.00"), dre.getReceitaLiquida());
        assertEquals(new BigDecimal("300.00"), dre.getTotalComissoesProfissionais());
        assertEquals(new BigDecimal("850.00"), dre.getResultadoLiquido());
    }

    @Test
    @DisplayName("Deve simular venda de produto na comanda e realizar baixa automática no estoque")
    void deveDarBaixaEstoqueNaVendaProdutoComanda() {
        Caixa caixa = Caixa.builder()
                .id(1L)
                .status("ABERTO")
                .saldoInicial(new BigDecimal("100.00"))
                .totalEntradas(BigDecimal.ZERO)
                .totalSaidas(BigDecimal.ZERO)
                .saldoFinalEsperado(new BigDecimal("100.00"))
                .empresa(empresaMock)
                .build();

        Produto shampoo = Produto.builder()
                .id(55L)
                .nome("Shampoo Hidratante 500ml")
                .tipo(TipoProduto.REVENDA)
                .precoVenda(new BigDecimal("60.00"))
                .quantidadeEstoque(10)
                .build();

        Comanda comanda = Comanda.builder()
                .id(80L)
                .numeroComanda("CMD-80")
                .status("ABERTA")
                .itens(new ArrayList<>())
                .empresa(empresaMock)
                .build();

        ComandaItem itemProduto = ComandaItem.builder()
                .comanda(comanda)
                .tipo("PRODUTO")
                .produto(shampoo)
                .descricaoItem(shampoo.getNome())
                .precoUnitario(shampoo.getPrecoVenda())
                .quantidade(2)
                .valorTotal(new BigDecimal("120.00"))
                .build();
        comanda.getItens().add(itemProduto);
        comanda.recalcularTotais();

        when(comandaRepository.findByIdAndEmpresaId(80L, EMPRESA_ID)).thenReturn(Optional.of(comanda));
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.of(caixa));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Comanda finalizada = financeiroService.fecharComanda(80L, "CARTAO_CREDITO", BigDecimal.ZERO, BigDecimal.ZERO, null, operadorMock);

        assertEquals("PAGA", finalizada.getStatus());
        assertEquals(new BigDecimal("120.00"), caixa.getTotalEntradas());
        assertEquals(new BigDecimal("220.00"), caixa.getSaldoFinalEsperado());

        // Valida que o estoque foi chamado com a baixa da venda (2 unidades)
        verify(estoqueService, times(1)).registrarMovimentacao(
                eq(55L),
                eq("SAIDA_VENDA"),
                eq(2),
                contains("Venda na Comanda"),
                eq(operadorMock)
        );
    }

    @Test
    @DisplayName("Deve simular serviço com insumos da ficha técnica e dar baixa automática de consumo")
    void deveDarBaixaInsumosFichaTecnicaFechamentoComanda() {
        Caixa caixa = Caixa.builder()
                .id(1L)
                .status("ABERTO")
                .saldoInicial(new BigDecimal("50.00"))
                .totalEntradas(BigDecimal.ZERO)
                .totalSaidas(BigDecimal.ZERO)
                .saldoFinalEsperado(new BigDecimal("50.00"))
                .empresa(empresaMock)
                .build();

        Servico coloracao = Servico.builder()
                .id(12L)
                .nome("Coloração Completa")
                .preco(new BigDecimal("200.00"))
                .build();

        Produto bisnagaTinta = Produto.builder().id(301L).nome("Tinta 6.0 Louro Escuro").build();
        Produto oxigenada = Produto.builder().id(302L).nome("Água Oxigenada 20 Vol").build();

        ServicoInsumo insumo1 = ServicoInsumo.builder().id(1L).servico(coloracao).produto(bisnagaTinta).quantidadeGasta(1).empresa(empresaMock).build();
        ServicoInsumo insumo2 = ServicoInsumo.builder().id(2L).servico(coloracao).produto(oxigenada).quantidadeGasta(2).empresa(empresaMock).build();

        Comanda comanda = Comanda.builder()
                .id(90L)
                .numeroComanda("CMD-90")
                .status("ABERTA")
                .itens(new ArrayList<>())
                .empresa(empresaMock)
                .build();

        ComandaItem itemServico = ComandaItem.builder()
                .comanda(comanda)
                .tipo("SERVICO")
                .servico(coloracao)
                .descricaoItem(coloracao.getNome())
                .precoUnitario(coloracao.getPreco())
                .quantidade(1)
                .valorTotal(new BigDecimal("200.00"))
                .build();
        comanda.getItens().add(itemServico);
        comanda.recalcularTotais();

        when(comandaRepository.findByIdAndEmpresaId(90L, EMPRESA_ID)).thenReturn(Optional.of(comanda));
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.of(caixa));
        when(servicoInsumoRepository.findByServicoIdAndEmpresaId(12L, EMPRESA_ID)).thenReturn(List.of(insumo1, insumo2));
        when(comandaRepository.save(any(Comanda.class))).thenAnswer(invocation -> invocation.getArgument(0));

        financeiroService.fecharComanda(90L, "DINHEIRO", BigDecimal.ZERO, BigDecimal.ZERO, null, operadorMock);

        // Verifica que foram dadas baixas nos insumos
        verify(estoqueService, times(1)).registrarMovimentacao(
                eq(301L),
                eq("CONSUMO_SERVICO"),
                eq(1),
                contains("Consumo no Serviço"),
                eq(operadorMock)
        );
        verify(estoqueService, times(1)).registrarMovimentacao(
                eq(302L),
                eq("CONSUMO_SERVICO"),
                eq(2),
                contains("Consumo no Serviço"),
                eq(operadorMock)
        );
    }

    @Test
    @DisplayName("Não deve permitir fechar comanda se o caixa estiver fechado")
    void naoDevePermitirFecharComandaSemCaixaAberto() {
        Comanda comanda = Comanda.builder().id(99L).status("ABERTA").empresa(empresaMock).build();
        when(comandaRepository.findByIdAndEmpresaId(99L, EMPRESA_ID)).thenReturn(Optional.of(comanda));
        when(caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(EMPRESA_ID, "ABERTO"))
                .thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                financeiroService.fecharComanda(99L, "PIX", BigDecimal.ZERO, BigDecimal.ZERO, null, operadorMock)
        );

        assertTrue(ex.getMessage().contains("abrir um caixa antes de receber"));
    }

    @Test
    @DisplayName("Deve registrar sangria e reforço de caixa atualizando saldo final esperado")
    void deveRegistrarSangriaEReforco() {
        Caixa caixa = Caixa.builder()
                .id(1L)
                .status("ABERTO")
                .saldoInicial(new BigDecimal("100.00"))
                .totalEntradas(BigDecimal.ZERO)
                .totalSaidas(BigDecimal.ZERO)
                .saldoFinalEsperado(new BigDecimal("100.00"))
                .empresa(empresaMock)
                .build();

        when(caixaRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(caixa));

        // Sangria de 30.00
        financeiroService.registrarMovimentacaoCaixa(1L, "SANGRIA", new BigDecimal("30.00"), "Retirada", "Sangria para cofre", operadorMock);
        assertEquals(new BigDecimal("30.00"), caixa.getTotalSaidas());
        assertEquals(new BigDecimal("70.00"), caixa.getSaldoFinalEsperado());

        // Reforço de 50.00
        financeiroService.registrarMovimentacaoCaixa(1L, "REFORCO", new BigDecimal("50.00"), "Entrada", "Reforço troco moedas", operadorMock);
        assertEquals(new BigDecimal("50.00"), caixa.getTotalEntradas());
        assertEquals(new BigDecimal("120.00"), caixa.getSaldoFinalEsperado()); // 100 + 50 - 30 = 120

        verify(movimentacaoFinanceiraRepository, times(2)).save(any(MovimentacaoFinanceira.class));
    }
}
