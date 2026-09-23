package com.beautysalon.Controller;

import com.beautysalon.model.User;
import com.beautysalon.repository.AgendamentoRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/{slug}/relatorios")
public class RelatorioController {

    private final ComandaRepository comandaRepository;
    private final ComandaItemRepository comandaItemRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final UserRepository userRepository;
    private final EstoqueService estoqueService;

    public RelatorioController(ComandaRepository comandaRepository,
                               ComandaItemRepository comandaItemRepository,
                               AgendamentoRepository agendamentoRepository,
                               UserRepository userRepository,
                               EstoqueService estoqueService) {
        this.comandaRepository = comandaRepository;
        this.comandaItemRepository = comandaItemRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.userRepository = userRepository;
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public String index(@PathVariable String slug,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
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

        // Relatório de Comissões por Profissional
        List<User> profissionais = userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);
        List<Map<String, Object>> relatorioComissoes = new ArrayList<>();

        for (User prof : profissionais) {
            BigDecimal comissao = comandaItemRepository.sumComissaoProfissional(empresaId, prof.getId(), inicio, fim);
            if (comissao.compareTo(BigDecimal.ZERO) > 0 || totalAtendimentos > 0) {
                Map<String, Object> linha = new HashMap<>();
                linha.put("profissional", prof);
                linha.put("comissao", comissao);
                relatorioComissoes.add(linha);
            }
        }

        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);
        model.addAttribute("faturamentoTotal", faturamentoTotal);
        model.addAttribute("totalComissoes", totalComissoes);
        model.addAttribute("lucroBruto", faturamentoTotal.subtract(totalComissoes));
        model.addAttribute("totalAtendimentos", totalAtendimentos);
        model.addAttribute("ticketMedio", ticketMedio);
        model.addAttribute("valorParadoEstoque", valorParadoEstoque);
        model.addAttribute("relatorioComissoes", relatorioComissoes);
        model.addAttribute("comandasPagas", comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim));

        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Relatórios Financeiros & Gerenciais");
        return "relatorios/index";
    }
}
