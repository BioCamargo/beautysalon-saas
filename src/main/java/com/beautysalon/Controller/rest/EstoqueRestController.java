package com.beautysalon.Controller.rest;

import com.beautysalon.dto.rest.ProdutoRestDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.MovimentacaoEstoque;
import com.beautysalon.model.Produto;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.service.EstoqueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/{slug}/estoque")
@Tag(name = "Estoque e Produtos", description = "Endpoints para gestão de produtos, controle de saldos e movimentações de estoque")
public class EstoqueRestController {

    private final EstoqueService estoqueService;
    private final EmpresaService empresaService;

    public EstoqueRestController(EstoqueService estoqueService, EmpresaService empresaService) {
        this.estoqueService = estoqueService;
        this.empresaService = empresaService;
    }

    private Empresa obterEmpresa(String slug) {
        return empresaService.buscarPorSlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
    }

    private ProdutoRestDTO.Response toResponse(Produto p) {
        return new ProdutoRestDTO.Response(
                p.getId(),
                p.getNome(),
                p.getCodigoBarras(),
                p.getCategoria(),
                p.getTipo() != null ? p.getTipo().name() : null,
                p.getPrecoCusto(),
                p.getPrecoVenda(),
                p.getQuantidadeEstoque(),
                p.getEstoqueMinimo(),
                p.getUnidadeMedida(),
                p.isEstoqueBaixo()
        );
    }

    @GetMapping("/produtos")
    @Operation(summary = "Listar produtos e saldos atuais")
    public ResponseEntity<List<ProdutoRestDTO.Response>> listarProdutos(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<ProdutoRestDTO.Response> response = estoqueService.listarProdutosPorEmpresa(empresa.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/produtos/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProdutoRestDTO.Response> buscarProdutoPorId(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        Produto produto = estoqueService.buscarProdutoPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado."));
        return ResponseEntity.ok(toResponse(produto));
    }

    @PostMapping("/produtos")
    @Operation(summary = "Cadastrar novo produto no estoque")
    public ResponseEntity<ProdutoRestDTO.Response> salvarProduto(
            @PathVariable String slug,
            @Valid @RequestBody ProdutoRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        Produto p = new Produto();
        p.setNome(request.nome());
        p.setCodigoBarras(request.codigoBarras());
        p.setCategoria(request.categoria());
        if (request.tipo() != null) {
            p.setTipo(Produto.TipoProduto.valueOf(request.tipo().toUpperCase()));
        }
        p.setPrecoCusto(request.precoCusto());
        p.setPrecoVenda(request.precoVenda());
        p.setQuantidadeEstoque(request.quantidadeEstoque());
        p.setEstoqueMinimo(request.estoqueMinimo());
        p.setUnidadeMedida(request.unidadeMedida());
        p.setEmpresa(empresa);

        Produto salvo = estoqueService.salvarProduto(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @GetMapping("/alertas")
    @Operation(summary = "Listar produtos com estoque abaixo do nível mínimo")
    public ResponseEntity<List<ProdutoRestDTO.Response>> listarAlertas(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<ProdutoRestDTO.Response> response = estoqueService.listarProdutosComEstoqueBaixo(empresa.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/movimentacoes")
    @Operation(summary = "Registrar entrada, saída avulsa ou consumo de produto")
    public ResponseEntity<ProdutoRestDTO.Response> movimentar(
            @PathVariable String slug,
            @Valid @RequestBody ProdutoRestDTO.MovimentacaoRequest request) {

        Empresa empresa = obterEmpresa(slug);
        MovimentacaoEstoque.TipoMovimentacao tipo = MovimentacaoEstoque.TipoMovimentacao.valueOf(request.tipo().toUpperCase());

        estoqueService.movimentarEstoque(
                request.produtoId(),
                empresa.getId(),
                tipo,
                request.quantidade(),
                request.motivo(),
                null,
                null
        );

        Produto produtoAtualizado = estoqueService.buscarProdutoPorIdEEmpresa(request.produtoId(), empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado após movimentação."));

        return ResponseEntity.ok(toResponse(produtoAtualizado));
    }
}
