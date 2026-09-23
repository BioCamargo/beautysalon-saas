package com.beautysalon.Controller.rest;

import com.beautysalon.DTO.rest.ProdutoRestDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.MovimentacaoEstoque;
import com.beautysalon.model.Produto;
import com.beautysalon.model.TipoProduto;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.service.EstoqueService;
import com.beautysalon.tenant.TenantContext;
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
    private final EmpresaRepository empresaRepository;

    public EstoqueRestController(EstoqueService estoqueService, EmpresaRepository empresaRepository) {
        this.estoqueService = estoqueService;
        this.empresaRepository = empresaRepository;
    }

    private Empresa obterEmpresa(String slug) {
        Empresa emp = empresaRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
        TenantContext.setTenant(emp.getSlug(), emp.getId());
        return emp;
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
                p.getQuantidadeEstoque() != null ? java.math.BigDecimal.valueOf(p.getQuantidadeEstoque()) : java.math.BigDecimal.ZERO,
                p.getEstoqueMinimo() != null ? java.math.BigDecimal.valueOf(p.getEstoqueMinimo()) : java.math.BigDecimal.ZERO,
                p.getUnidadeMedida(),
                p.isEstoqueBaixo()
        );
    }

    @GetMapping("/produtos")
    @Operation(summary = "Listar produtos e saldos atuais")
    public ResponseEntity<List<ProdutoRestDTO.Response>> listarProdutos(@PathVariable String slug) {
        obterEmpresa(slug);
        List<ProdutoRestDTO.Response> response = estoqueService.listarTodos()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/produtos/{id}")
    @Operation(summary = "Buscar produto por ID")
    public ResponseEntity<ProdutoRestDTO.Response> buscarProdutoPorId(@PathVariable String slug, @PathVariable Long id) {
        obterEmpresa(slug);
        Produto produto = estoqueService.buscarPorId(id);
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
            p.setTipo(TipoProduto.valueOf(request.tipo().toUpperCase()));
        }
        p.setPrecoCusto(request.precoCusto());
        p.setPrecoVenda(request.precoVenda());
        p.setQuantidadeEstoque(request.quantidadeEstoque() != null ? request.quantidadeEstoque().intValue() : 0);
        p.setEstoqueMinimo(request.estoqueMinimo() != null ? request.estoqueMinimo().intValue() : 0);
        p.setUnidadeMedida(request.unidadeMedida());
        p.setEmpresa(empresa);

        Produto salvo = estoqueService.salvar(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @GetMapping("/alertas")
    @Operation(summary = "Listar produtos com estoque abaixo do nível mínimo")
    public ResponseEntity<List<ProdutoRestDTO.Response>> listarAlertas(@PathVariable String slug) {
        obterEmpresa(slug);
        List<ProdutoRestDTO.Response> response = estoqueService.listarEstoqueBaixo()
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

        obterEmpresa(slug);
        estoqueService.registrarMovimentacao(
                request.produtoId(),
                request.tipo().toUpperCase(),
                request.quantidade().intValue(),
                request.motivo(),
                null
        );

        Produto produtoAtualizado = estoqueService.buscarPorId(request.produtoId());
        return ResponseEntity.ok(toResponse(produtoAtualizado));
    }
}
