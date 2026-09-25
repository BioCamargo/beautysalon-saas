package com.beautysalon.service;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.MovimentacaoEstoque;
import com.beautysalon.model.Produto;
import com.beautysalon.model.TipoProduto;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.MovimentacaoEstoqueRepository;
import com.beautysalon.repository.ProdutoRepository;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EstoqueServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private com.beautysalon.config.messaging.producer.EventMessageProducer eventMessageProducer;

    @InjectMocks
    private EstoqueService estoqueService;

    private final Long EMPRESA_ID = 1L;
    private Empresa empresaMock;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
        empresaMock = Empresa.builder().id(EMPRESA_ID).nome("Lumora Studio").slug("lumora").build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve listar todos os produtos ativos do tenant atual")
    void deveListarTodosProdutosAtivos() {
        Produto p1 = Produto.builder().id(10L).nome("Shampoo").ativo(true).build();
        Produto p2 = Produto.builder().id(11L).nome("Condicionador").ativo(true).build();

        when(produtoRepository.findByEmpresaIdAndAtivoTrue(EMPRESA_ID)).thenReturn(List.of(p1, p2));

        List<Produto> resultado = estoqueService.listarTodos();

        assertEquals(2, resultado.size());
        verify(produtoRepository, times(1)).findByEmpresaIdAndAtivoTrue(EMPRESA_ID);
    }

    @Test
    @DisplayName("Deve calcular o valor total financeiro parado em estoque")
    void deveCalcularValorParadoEmEstoque() {
        BigDecimal valorEsperado = new BigDecimal("1250.50");
        when(produtoRepository.calcularValorTotalParadoEstoque(EMPRESA_ID)).thenReturn(valorEsperado);

        BigDecimal resultado = estoqueService.calcularValorParadoEmEstoque();

        assertEquals(valorEsperado, resultado);
        verify(produtoRepository, times(1)).calcularValorTotalParadoEstoque(EMPRESA_ID);
    }

    @Test
    @DisplayName("Deve registrar entrada de estoque somando ao saldo existente")
    void deveRegistrarEntradaDeEstoque() {
        Produto produto = Produto.builder()
                .id(1L)
                .nome("Máscara Capilar")
                .quantidadeEstoque(5)
                .empresa(empresaMock)
                .build();

        when(produtoRepository.findByIdAndEmpresaId(1L, EMPRESA_ID)).thenReturn(Optional.of(produto));

        User operador = User.builder().id(100L).nome("Operador").build();
        estoqueService.registrarMovimentacao(1L, "ENTRADA", 10, "Reposição de estoque", operador);

        assertEquals(15, produto.getQuantidadeEstoque());
        verify(produtoRepository, times(1)).save(produto);

        ArgumentCaptor<MovimentacaoEstoque> captor = ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoEstoqueRepository, times(1)).save(captor.capture());

        MovimentacaoEstoque movSalva = captor.getValue();
        assertEquals("ENTRADA", movSalva.getTipo());
        assertEquals(10, movSalva.getQuantidade());
        assertEquals(5, movSalva.getSaldoAnterior());
        assertEquals(15, movSalva.getSaldoAtual());
        assertEquals("Reposição de estoque", movSalva.getMotivo());
    }

    @Test
    @DisplayName("Deve registrar saída/venda de estoque reduzindo o saldo sem ficar negativo")
    void deveRegistrarSaidaDeEstoque() {
        Produto produto = Produto.builder()
                .id(2L)
                .nome("Óleo Reparador")
                .quantidadeEstoque(3)
                .empresa(empresaMock)
                .build();

        when(produtoRepository.findByIdAndEmpresaId(2L, EMPRESA_ID)).thenReturn(Optional.of(produto));

        User operador = User.builder().id(100L).nome("Operador").build();
        estoqueService.registrarMovimentacao(2L, "SAIDA_VENDA", 5, "Venda balcão", operador);

        assertEquals(0, produto.getQuantidadeEstoque()); // Garante que não fica negativo
        verify(produtoRepository, times(1)).save(produto);
        verify(movimentacaoEstoqueRepository, times(1)).save(any(MovimentacaoEstoque.class));
    }

    @Test
    @DisplayName("Deve desativar logicamente o produto ao excluir")
    void deveExcluirLogicamenteProduto() {
        Produto produto = Produto.builder().id(5L).nome("Tonalizante").ativo(true).build();
        when(produtoRepository.findByIdAndEmpresaId(5L, EMPRESA_ID)).thenReturn(Optional.of(produto));

        estoqueService.excluir(5L);

        assertFalse(produto.isAtivo());
        verify(produtoRepository, times(1)).save(produto);
    }
}
