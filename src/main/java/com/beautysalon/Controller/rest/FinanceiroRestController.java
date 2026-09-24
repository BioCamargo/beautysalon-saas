package com.beautysalon.Controller.rest;

import com.beautysalon.DTO.rest.FinanceiroRestDTO;
import com.beautysalon.model.Caixa;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.service.FinanceiroService;
import com.beautysalon.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/{slug}/financeiro")
@Tag(name = "Financeiro e Comandas", description = "Endpoints para abertura/fechamento de caixa diário e gestão de comandas")
public class FinanceiroRestController {

    private final FinanceiroService financeiroService;
    private final EmpresaRepository empresaRepository;

    public FinanceiroRestController(FinanceiroService financeiroService, EmpresaRepository empresaRepository) {
        this.financeiroService = financeiroService;
        this.empresaRepository = empresaRepository;
    }

    private Empresa obterEmpresa(String slug) {
        Empresa emp = empresaRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
        TenantContext.setTenant(emp.getSlug(), emp.getId());
        return emp;
    }

    private FinanceiroRestDTO.CaixaResponse toCaixaResponse(Caixa c) {
        return new FinanceiroRestDTO.CaixaResponse(
                c.getId(),
                c.getDataAbertura(),
                c.getDataFechamento(),
                c.getSaldoInicial(),
                c.getTotalEntradas(),
                c.getTotalSaidas(),
                c.getSaldoFinalEsperado(),
                c.getSaldoFinalContado(),
                c.getDiferencaFechamento(),
                c.getStatus(),
                c.getOperadorAbertura() != null ? c.getOperadorAbertura().getNome() : null
        );
    }

    private FinanceiroRestDTO.ComandaResponse toComandaResponse(Comanda cmd) {
        List<FinanceiroRestDTO.ComandaItemResponse> itens = cmd.getItens() != null
                ? cmd.getItens().stream().map(i -> new FinanceiroRestDTO.ComandaItemResponse(
                        i.getId(),
                        i.getTipo(),
                        i.getDescricaoItem(),
                        i.getQuantidade() != null ? java.math.BigDecimal.valueOf(i.getQuantidade()) : java.math.BigDecimal.ONE,
                        i.getPrecoUnitario(),
                        java.math.BigDecimal.ZERO,
                        i.getValorTotal(),
                        i.getProfissional() != null ? i.getProfissional().getId() : null,
                        i.getProfissional() != null ? i.getProfissional().getNome() : null,
                        i.getValorComissao()
                )).toList()
                : List.of();

        return new FinanceiroRestDTO.ComandaResponse(
                cmd.getId(),
                cmd.getNumeroComanda(),
                cmd.getDataAbertura(),
                cmd.getDataFechamento(),
                cmd.getStatus(),
                cmd.getFormaPagamento(),
                cmd.getSubtotalServicos().add(cmd.getSubtotalProdutos()),
                cmd.getDesconto(),
                cmd.getValorTotal(),
                cmd.getCliente() != null ? cmd.getCliente().getId() : null,
                cmd.getCliente() != null ? cmd.getCliente().getNome() : cmd.getClienteNomeAvulso(),
                itens
        );
    }

    @GetMapping("/caixa/atual")
    @Operation(summary = "Obter informações do caixa atualmente aberto na empresa")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> obterCaixaAtual(@PathVariable String slug) {
        obterEmpresa(slug);
        Optional<Caixa> caixaOpt = financeiroService.buscarCaixaAberto();
        return caixaOpt.map(c -> ResponseEntity.ok(toCaixaResponse(c)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/caixa/abrir")
    @Operation(summary = "Abrir caixa do dia com saldo inicial")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> abrirCaixa(
            @PathVariable String slug,
            @Valid @RequestBody FinanceiroRestDTO.AbrirCaixaRequest request) {

        obterEmpresa(slug);
        Caixa caixa = financeiroService.abrirCaixa(request.saldoInicial(), request.observacoes(), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(toCaixaResponse(caixa));
    }

    @PostMapping("/caixa/{id}/fechar")
    @Operation(summary = "Fechar caixa com conferência física do valor em dinheiro")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> fecharCaixa(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.FecharCaixaRequest request) {

        obterEmpresa(slug);
        Caixa caixa = financeiroService.fecharCaixa(id, request.saldoDinheiroContado(), request.observacoes(), null);
        return ResponseEntity.ok(toCaixaResponse(caixa));
    }

    @GetMapping("/comandas")
    @Operation(summary = "Listar comandas abertas da empresa")
    public ResponseEntity<List<FinanceiroRestDTO.ComandaResponse>> listarComandas(@PathVariable String slug) {
        obterEmpresa(slug);
        List<FinanceiroRestDTO.ComandaResponse> comandas = financeiroService.listarComandasAbertas()
                .stream()
                .map(this::toComandaResponse)
                .toList();
        return ResponseEntity.ok(comandas);
    }

    @PostMapping("/comandas")
    @Operation(summary = "Abrir nova comanda para cliente ou atendimento avulso")
    public ResponseEntity<FinanceiroRestDTO.ComandaResponse> abrirComanda(
            @PathVariable String slug,
            @RequestBody FinanceiroRestDTO.CriarComandaRequest request) {

        obterEmpresa(slug);
        Comanda comanda = financeiroService.criarComanda(null, "Cliente Balcão", null, request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toComandaResponse(comanda));
    }

    @PostMapping("/comandas/{id}/itens")
    @Operation(summary = "Adicionar serviço ou produto na comanda")
    public ResponseEntity<FinanceiroRestDTO.ComandaResponse> adicionarItem(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.AdicionarItemComandaRequest request) {

        obterEmpresa(slug);
        String tipo = request.servicoId() != null ? "SERVICO" : "PRODUTO";
        Comanda comandaAtualizada = financeiroService.adicionarItemComanda(
                id,
                tipo,
                request.servicoId(),
                request.produtoId(),
                request.profissionalId(),
                request.quantidade().intValue(),
                request.valorUnitario(),
                null,
                null
        );

        return ResponseEntity.ok(toComandaResponse(comandaAtualizada));
    }

    @PostMapping("/comandas/{id}/fechar")
    @Operation(summary = "Finalizar e receber pagamento da comanda (calcula comissões e baixa estoque)")
    public ResponseEntity<FinanceiroRestDTO.ComandaResponse> fecharComanda(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.FecharComandaRequest request) {

        obterEmpresa(slug);
        Comanda fechada = financeiroService.fecharComanda(
                id,
                request.formaPagamento().toUpperCase(),
                request.descontoGeral(),
                request.acrescimoGeral(),
                request.cupomCodigo(),
                null
        );

        return ResponseEntity.ok(toComandaResponse(fechada));
    }
}
