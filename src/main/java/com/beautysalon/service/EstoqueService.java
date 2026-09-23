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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final EmpresaRepository empresaRepository;

    public EstoqueService(ProdutoRepository produtoRepository,
                          MovimentacaoEstoqueRepository movimentacaoEstoqueRepository,
                          EmpresaRepository empresaRepository) {
        this.produtoRepository = produtoRepository;
        this.movimentacaoEstoqueRepository = movimentacaoEstoqueRepository;
        this.empresaRepository = empresaRepository;
    }

    public List<Produto> listarTodos() {
        return produtoRepository.findByEmpresaIdAndAtivoTrue(TenantContext.getEmpresaId());
    }

    public List<Produto> listarPorTipo(TipoProduto tipo) {
        return produtoRepository.findByEmpresaIdAndTipoAndAtivoTrue(TenantContext.getEmpresaId(), tipo);
    }

    public List<Produto> listarEstoqueBaixo() {
        return produtoRepository.findProdutosEstoqueBaixo(TenantContext.getEmpresaId());
    }

    public BigDecimal calcularValorParadoEmEstoque() {
        return produtoRepository.calcularValorTotalParadoEstoque(TenantContext.getEmpresaId());
    }

    public long contarEstoqueBaixo() {
        return produtoRepository.countProdutosEstoqueBaixo(TenantContext.getEmpresaId());
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findByIdAndEmpresaId(id, TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com id: " + id));
    }

    @Transactional
    public Produto salvar(Produto produto) {
        if (produto.getEmpresa() == null) {
            Empresa empresa = empresaRepository.findById(TenantContext.getEmpresaId())
                    .orElseThrow(() -> new IllegalStateException("Empresa não encontrada no contexto"));
            produto.setEmpresa(empresa);
        }
        return produtoRepository.save(produto);
    }

    @Transactional
    public void registrarMovimentacao(Long produtoId, String tipo, int quantidade, String motivo, User usuario) {
        Produto produto = buscarPorId(produtoId);
        int saldoAnterior = produto.getQuantidadeEstoque() != null ? produto.getQuantidadeEstoque() : 0;
        int novoSaldo = saldoAnterior;

        if ("ENTRADA".equals(tipo)) {
            novoSaldo = saldoAnterior + quantidade;
        } else if ("SAIDA_VENDA".equals(tipo) || "CONSUMO_SERVICO".equals(tipo) || "SAIDA_AVULSA".equals(tipo)) {
            novoSaldo = Math.max(0, saldoAnterior - quantidade);
        } else if ("AJUSTE_MANUAL".equals(tipo)) {
            novoSaldo = quantidade;
        }

        produto.setQuantidadeEstoque(novoSaldo);
        produtoRepository.save(produto);

        MovimentacaoEstoque mov = MovimentacaoEstoque.builder()
                .produto(produto)
                .tipo(tipo)
                .quantidade(quantidade)
                .saldoAnterior(saldoAnterior)
                .saldoAtual(novoSaldo)
                .motivo(motivo)
                .usuario(usuario)
                .empresa(produto.getEmpresa())
                .build();

        movimentacaoEstoqueRepository.save(mov);
    }

    public List<MovimentacaoEstoque> listarMovimentacoes() {
        return movimentacaoEstoqueRepository.findByEmpresaIdOrderByDataHoraDesc(TenantContext.getEmpresaId());
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarPorId(id);
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }
}
