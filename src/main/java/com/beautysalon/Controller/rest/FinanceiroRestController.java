package com.beautysalon.controller.rest;

import com.beautysalon.dto.rest.FinanceiroRestDTO;
import com.beautysalon.model.Caixa;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.Empresa;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.service.FinanceiroService;
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
    private final EmpresaService empresaService;

    public FinanceiroRestController(FinanceiroService financeiroService, EmpresaService empresaService) {
        this.financeiroService = financeiroService;
        this.empresaService = empresaService;
    }

    private Empresa obterEmpresa(String slug) {
        return empresaService.buscarPorSlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
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
                c.getDiferenca(),
                c.getStatus() != null ? c.getStatus().name() : null,
                c.getOperadorAbertura() != null ? c.getOperadorAbertura().getNome() : null
        );
    }

    private FinanceiroRestDTO.ComandaResponse toComandaResponse(Comanda cmd) {
        List<FinanceiroRestDTO.ComandaItemResponse> itens = cmd.getItens() != null
                ? cmd.getItens().stream().map(i -> new FinanceiroRestDTO.ComandaItemResponse(
                        i.getId(),
                        i.getTipoItem() != null ? i.getTipoItem().name() : null,
                        i.getDescricao(),
                        i.getQuantidade(),
                        i.getValorUnitario(),
                        i.getValorDesconto(),
                        i.getValorTotal(),
                        i.getProfissional() != null ? i.getProfissional().getId() : null,
                        i.getProfissional() != null ? i.getProfissional().getNome() : null,
                        i.getValorComissao()
                )).toList()
                : List.of();

        return new FinanceiroRestDTO.ComandaResponse(
                cmd.getId(),
                cmd.getNumeroComanda(),
                cmd.getDataHoraAbertura(),
                cmd.getDataHoraFechamento(),
                cmd.getStatus() != null ? cmd.getStatus().name() : null,
                cmd.getFormaPagamento() != null ? cmd.getFormaPagamento().name() : null,
                cmd.getValorSubtotal(),
                cmd.getValorDesconto(),
                cmd.getValorTotal(),
                cmd.getCliente() != null ? cmd.getCliente().getId() : null,
                cmd.getCliente() != null ? cmd.getCliente().getNome() : null,
                itens
        );
    }

    @GetMapping("/caixa/atual")
    @Operation(summary = "Obter informações do caixa atualmente aberto na empresa")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> obterCaixaAtual(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        Optional<Caixa> caixaOpt = financeiroService.buscarCaixaAberto(empresa.getId());
        return caixaOpt.map(c -> ResponseEntity.ok(toCaixaResponse(c)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/caixa/abrir")
    @Operation(summary = "Abrir caixa do dia com saldo inicial")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> abrirCaixa(
            @PathVariable String slug,
            @Valid @RequestBody FinanceiroRestDTO.AbrirCaixaRequest request) {

        Empresa empresa = obterEmpresa(slug);
        Caixa caixa = financeiroService.abrirCaixa(empresa.getId(), null, request.saldoInicial(), request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toCaixaResponse(caixa));
    }

    @PostMapping("/caixa/{id}/fechar")
    @Operation(summary = "Fechar caixa com conferência física do valor em dinheiro")
    public ResponseEntity<FinanceiroRestDTO.CaixaResponse> fecharCaixa(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.FecharCaixaRequest request) {

        Empresa empresa = obterEmpresa(slug);
        Caixa caixa = financeiroService.fecharCaixa(id, empresa.getId(), null, request.saldoDinheiroContado(), request.observacoes());
        return ResponseEntity.ok(toCaixaResponse(caixa));
    }

    @GetMapping("/comandas")
    @Operation(summary = "Listar comandas abertas da empresa")
    public ResponseEntity<List<FinanceiroRestDTO.ComandaResponse>> listarComandas(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<FinanceiroRestDTO.ComandaResponse> comandas = financeiroService.listarComandasAbertas(empresa.getId())
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

        Empresa empresa = obterEmpresa(slug);
        Comanda comanda = financeiroService.abrirComanda(empresa.getId(), request.clienteId(), null, request.observacoes());
        return ResponseEntity.status(HttpStatus.CREATED).body(toComandaResponse(comanda));
    }

    @PostMapping("/comandas/{id}/itens")
    @Operation(summary = "Adicionar serviço ou produto na comanda")
    public ResponseEntity<FinanceiroRestDTO.ComandaResponse> adicionarItem(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.AdicionarItemComandaRequest request) {

        Empresa empresa = obterEmpresa(slug);
        financeiroService.adicionarItem(
                id,
                empresa.getId(),
                request.servicoId(),
                request.produtoId(),
                request.profissionalId(),
                request.quantidade(),
                request.valorUnitario(),
                request.valorDesconto()
        );

        Comanda comandaAtualizada = financeiroService.buscarComandaPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Comanda não encontrada."));

        return ResponseEntity.ok(toComandaResponse(comandaAtualizada));
    }

    @PostMapping("/comandas/{id}/fechar")
    @Operation(summary = "Finalizar e receber pagamento da comanda (calcula comissões e baixa estoque)")
    public ResponseEntity<FinanceiroRestDTO.ComandaResponse> fecharComanda(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody FinanceiroRestDTO.FecharComandaRequest request) {

        Empresa empresa = obterEmpresa(slug);
        Comanda.FormaPagamento forma = Comanda.FormaPagamento.valueOf(request.formaPagamento().toUpperCase());

        Comanda fechada = financeiroService.fecharComanda(
                id,
                empresa.getId(),
                forma,
                request.descontoGeral(),
                request.acrescimoGeral(),
                request.cupomCodigo()
        );

        return ResponseEntity.ok(toComandaResponse(fechada));
    }
}
