package com.beautysalon.service;

import com.beautysalon.model.*;
import com.beautysalon.repository.*;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
}
