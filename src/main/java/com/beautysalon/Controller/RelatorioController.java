package com.beautysalon.Controller;

import com.beautysalon.model.User;
import com.beautysalon.repository.ComandaItemRepository;
import com.beautysalon.repository.ComandaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.EstoqueService;
import com.beautysalon.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Controller
@RequestMapping("/{slug}/relatorios")
public class RelatorioController {

    private final ComandaRepository comandaRepository;
    private final ComandaItemRepository comandaItemRepository;
    private final UserRepository userRepository;
    private final EstoqueService estoqueService;
    private final com.beautysalon.service.FinanceiroService financeiroService;
    private final com.beautysalon.service.RelatorioService relatorioService;

    public RelatorioController(ComandaRepository comandaRepository,
                               ComandaItemRepository comandaItemRepository,
                               UserRepository userRepository,
                               EstoqueService estoqueService,
                               com.beautysalon.service.FinanceiroService financeiroService,
                               com.beautysalon.service.RelatorioService relatorioService) {
        this.comandaRepository = comandaRepository;
        this.comandaItemRepository = comandaItemRepository;
        this.userRepository = userRepository;
        this.estoqueService = estoqueService;
        this.financeiroService = financeiroService;
        this.relatorioService = relatorioService;
    }

    @GetMapping
    public String index(@PathVariable String slug,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                        @RequestParam(required = false, defaultValue = "financeiro") String tab,
                        Model model) {
        Long empresaId = TenantContext.getEmpresaId();

        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1); // Primeiro dia do mês
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

        BigDecimal faturamentoTotal = comandaRepository.sumFaturamentoPorPeriodo(empresaId, inicio, fim);
        BigDecimal totalComissoes = comandaRepository.sumComissoesPorPeriodo(empresaId, inicio, fim);
        long totalAtendimentos = comandaRepository.countAtendimentosPorPeriodo(empresaId, inicio, fim);
        BigDecimal ticketMedio = totalAtendimentos > 0
                ? faturamentoTotal.divide(BigDecimal.valueOf(totalAtendimentos), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal valorParadoEstoque = estoqueService.calcularValorParadoEmEstoque();

        var dre = financeiroService.calcularDREPeriodo(inicio, fim);
        var rentabilidadeServicos = financeiroService.calcularRentabilidadeServicos(inicio, fim);
        var formasPagamento = relatorioService.obterMetricasFormasPagamento(inicio, fim);
        var desempenhoProfissionais = relatorioService.obterDesempenhoProfissionais(inicio, fim);
        var topClientes = relatorioService.obterTopClientes(inicio, fim);
        var clientesResgate = relatorioService.obterClientesResgate();
        var metricasProdutos = relatorioService.obterMetricasProdutos(inicio, fim);
        var metricasAgendamentos = relatorioService.obterMetricasAgendamento(inicio, fim);

        model.addAttribute("activeTab", tab);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("faturamentoTotal", faturamentoTotal);
        model.addAttribute("totalComissoes", totalComissoes);
        model.addAttribute("lucroBruto", faturamentoTotal.subtract(totalComissoes));
        model.addAttribute("totalAtendimentos", totalAtendimentos);
        model.addAttribute("ticketMedio", ticketMedio);
        model.addAttribute("valorParadoEstoque", valorParadoEstoque);
        model.addAttribute("comandasPagas", comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim));
        model.addAttribute("dre", dre);
        model.addAttribute("rentabilidadeServicos", rentabilidadeServicos);

        // Novas Métricas
        model.addAttribute("formasPagamento", formasPagamento);
        model.addAttribute("desempenhoProfissionais", desempenhoProfissionais);
        model.addAttribute("topClientes", topClientes);
        model.addAttribute("clientesResgate", clientesResgate);
        model.addAttribute("metricasProdutos", metricasProdutos);
        model.addAttribute("metricasAgendamentos", metricasAgendamentos);

        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Relatórios Financeiros & Gerenciais");
        return "relatorios/index";
    }

    @GetMapping("/comissoes/{profissionalId}")
    public String extratoProfissional(@PathVariable String slug,
                                      @PathVariable Long profissionalId,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                                      Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now();
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(LocalTime.MAX);

        User profissional = userRepository.findById(profissionalId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        var itens = comandaItemRepository.findItensPorProfissionalEPeriodo(empresaId, profissionalId, inicio, fim);
        BigDecimal totalComissao = comandaItemRepository.sumComissaoProfissional(empresaId, profissionalId, inicio, fim);
        BigDecimal totalFaturado = itens.stream()
                .map(com.beautysalon.model.ComandaItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("profissional", profissional);
        model.addAttribute("itens", itens);
        model.addAttribute("totalComissao", totalComissao);
        model.addAttribute("totalFaturado", totalFaturado);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Extrato de Comissões: " + profissional.getNome());
        return "relatorios/comissao-detalhe";
    }
}
