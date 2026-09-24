package com.beautysalon.Controller;

import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final com.beautysalon.repository.ClienteRepository clienteRepository;
    private final com.beautysalon.repository.AgendamentoRepository agendamentoRepository;
    private final com.beautysalon.service.EstoqueService estoqueService;
    private final com.beautysalon.service.FinanceiroService financeiroService;
    private final com.beautysalon.service.FidelizacaoService fidelizacaoService;
    private final com.beautysalon.repository.ComandaRepository comandaRepository;
    private final com.beautysalon.repository.ComandaItemRepository comandaItemRepository;

    public HomeController(UserRepository userRepository,
                          com.beautysalon.repository.ClienteRepository clienteRepository,
                          com.beautysalon.repository.AgendamentoRepository agendamentoRepository,
                          com.beautysalon.service.EstoqueService estoqueService,
                          com.beautysalon.service.FinanceiroService financeiroService,
                          com.beautysalon.service.FidelizacaoService fidelizacaoService,
                          com.beautysalon.repository.ComandaRepository comandaRepository,
                          com.beautysalon.repository.ComandaItemRepository comandaItemRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.estoqueService = estoqueService;
        this.financeiroService = financeiroService;
        this.fidelizacaoService = fidelizacaoService;
        this.comandaRepository = comandaRepository;
        this.comandaItemRepository = comandaItemRepository;
    }

    @GetMapping("/")
    public String root(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isPresent() && userOpt.get().getEmpresa() != null) {
                return "redirect:/" + userOpt.get().getEmpresa().getSlug() + "/home";
            }
        }
        return "redirect:/login";
    }

    @GetMapping({"/{slug}", "/{slug}/home"})
    public String dashboard(@PathVariable String slug, Model model) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();

        int mesAtual = java.time.LocalDate.now().getMonthValue();
        java.time.LocalDateTime hojeInicio = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime hojeFim = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);

        model.addAttribute("totalClientes", clienteRepository.countByEmpresaId(empresaId));
        model.addAttribute("agendamentosHoje", agendamentoRepository.countByEmpresaIdAndDataHoraBetween(empresaId, hojeInicio, hojeFim));
        model.addAttribute("listaAgendamentosHoje", agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, hojeInicio, hojeFim));
        model.addAttribute("valorParadoEstoque", estoqueService.calcularValorParadoEmEstoque());
        model.addAttribute("qtdEstoqueBaixo", estoqueService.contarEstoqueBaixo());
        model.addAttribute("aniversariantesMes", fidelizacaoService.listarAniversariantesDoMes(mesAtual));
        model.addAttribute("clientesAusentes", fidelizacaoService.buscarClientesEmRiscoRetorno(30));
        model.addAttribute("caixaAberto", financeiroService.buscarCaixaAberto().orElse(null));

        // Gráfico de Faturamento dos últimos 7 dias
        List<String> diasLabels = new ArrayList<>();
        List<BigDecimal> faturamentoDias = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            LocalDateTime di = d.atStartOfDay();
            LocalDateTime df = d.atTime(java.time.LocalTime.MAX);
            BigDecimal valorDia = comandaRepository.sumFaturamentoPorPeriodo(empresaId, di, df);
            diasLabels.add(d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            faturamentoDias.add(valorDia != null ? valorDia : BigDecimal.ZERO);
        }
        model.addAttribute("diasLabels", diasLabels);
        model.addAttribute("faturamentoDias", faturamentoDias);

        // Top Itens Mais Vendidos para Gráfico de Pizza
        List<Object[]> topItensRaw = comandaItemRepository.findTopItensMaisVendidos(empresaId);
        List<String> topItensLabels = new ArrayList<>();
        List<Long> topItensQtds = new ArrayList<>();
        int count = 0;
        for (Object[] row : topItensRaw) {
            if (count++ >= 5) break;
            topItensLabels.add(row[0] != null ? row[0].toString() : "Item");
            topItensQtds.add(row[1] != null ? ((Number) row[1]).longValue() : 0L);
        }
        model.addAttribute("topItensLabels", topItensLabels);
        model.addAttribute("topItensQtds", topItensQtds);

        model.addAttribute("contentPage", "home/indexContent");
        model.addAttribute("content", "home/welcome");
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Dashboard - Lumora");
        return "home/index";
    }
}
