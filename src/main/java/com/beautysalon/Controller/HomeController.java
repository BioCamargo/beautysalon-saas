package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
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
    private final EmpresaRepository empresaRepository;
    private final com.beautysalon.repository.ClienteRepository clienteRepository;
    private final com.beautysalon.repository.AgendamentoRepository agendamentoRepository;
    private final com.beautysalon.service.EstoqueService estoqueService;
    private final com.beautysalon.service.FinanceiroService financeiroService;
    private final com.beautysalon.service.FidelizacaoService fidelizacaoService;
    private final com.beautysalon.repository.ComandaRepository comandaRepository;
    private final com.beautysalon.repository.ComandaItemRepository comandaItemRepository;
    private final com.beautysalon.repository.ServicoRepository servicoRepository;

    public HomeController(UserRepository userRepository,
                          EmpresaRepository empresaRepository,
                          com.beautysalon.repository.ClienteRepository clienteRepository,
                          com.beautysalon.repository.AgendamentoRepository agendamentoRepository,
                          com.beautysalon.service.EstoqueService estoqueService,
                          com.beautysalon.service.FinanceiroService financeiroService,
                          com.beautysalon.service.FidelizacaoService fidelizacaoService,
                          com.beautysalon.repository.ComandaRepository comandaRepository,
                          com.beautysalon.repository.ComandaItemRepository comandaItemRepository,
                          com.beautysalon.repository.ServicoRepository servicoRepository) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.estoqueService = estoqueService;
        this.financeiroService = financeiroService;
        this.fidelizacaoService = fidelizacaoService;
        this.comandaRepository = comandaRepository;
        this.comandaItemRepository = comandaItemRepository;
        this.servicoRepository = servicoRepository;
    }

    @GetMapping("/")
    public String root(Authentication authentication,
                       @CookieValue(name = "nexipro_last_slug", required = false) String lastSlug,
                       Model model) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            if (authentication.getPrincipal() instanceof com.beautysalon.tenant.TenantUserDetails tud && tud.getEmpresaSlug() != null) {
                return "redirect:/" + tud.getEmpresaSlug() + "/home";
            }
            Optional<User> userOpt = userRepository.findByUsername(authentication.getName());
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findFirstByUsername(authentication.getName());
            }
            if (userOpt.isPresent() && userOpt.get().getEmpresa() != null) {
                return "redirect:/" + userOpt.get().getEmpresa().getSlug() + "/home";
            }
            if (lastSlug != null && !lastSlug.isBlank()) {
                return "redirect:/" + lastSlug.trim().toLowerCase() + "/home";
            }
        }

        // Se o usuário já possui empresa registrada/acessada no dispositivo (ex: abriu o App instalado ou atalho)
        if (lastSlug != null && !lastSlug.isBlank()) {
            try {
                String cleanSlug = java.net.URLDecoder.decode(lastSlug, java.nio.charset.StandardCharsets.UTF_8).trim().toLowerCase();
                Optional<Empresa> empOpt = empresaRepository.findBySlug(cleanSlug);
                if (empOpt.isPresent()) {
                    return "redirect:/" + empOpt.get().getSlug() + "/login";
                }
            } catch (Exception ignored) {}
        }

        model.addAttribute("pageTitle", "NEXI PRO | Sistema de Gestão para Salões de Beleza, Barbearias e Clínicas");
        return "landing/index";
    }

    public String root(Authentication authentication, Model model) {
        return root(authentication, null, model);
    }

    @GetMapping("/termos")
    public String termosDeUso(Model model) {
        model.addAttribute("pageTitle", "Termos de Uso do Serviço | NEXI PRO");
        return "landing/termos";
    }

    @GetMapping("/privacidade")
    public String politicaDePrivacidade(Model model) {
        model.addAttribute("pageTitle", "Política de Privacidade & LGPD | NEXI PRO");
        return "landing/privacidade";
    }

    @GetMapping({"/agendamento/{slug}", "/p/{slug}"})
    public String redirectAgendamentoPublico(@PathVariable String slug) {
        return "redirect:/" + slug + "/agendar";
    }

    @GetMapping("/{slug:^(?!login$|register$|register-empresa$|select-tenant$|termos$|privacidade$|error$|favicon\\.ico$|robots\\.txt$|sitemap\\.xml$|manifest\\.json$|service-worker\\.js$).+}")
    public String dashboardRoot(@PathVariable String slug, Model model) {
        return dashboard(slug, model);
    }

    @GetMapping("/{slug}/home")
    public String dashboard(@PathVariable String slug, Model model) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();

        int mesAtual = java.time.LocalDate.now().getMonthValue();
        java.time.LocalDateTime hojeInicio = java.time.LocalDate.now().atStartOfDay();
        java.time.LocalDateTime hojeFim = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);

        long totalServicos = servicoRepository.countByEmpresaId(empresaId);
        long totalProfissionais = userRepository.countByEmpresaId(empresaId);
        long totalClientes = clienteRepository.countByEmpresaId(empresaId);
        long totalProdutos = estoqueService.contarTotalProdutos();
        long totalAgendamentosGeral = agendamentoRepository.countByEmpresaId(empresaId);

        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("totalServicos", totalServicos);
        model.addAttribute("totalProfissionais", totalProfissionais);
        model.addAttribute("totalProdutos", totalProdutos);
        model.addAttribute("totalAgendamentosGeral", totalAgendamentosGeral);
        model.addAttribute("hasServicos", totalServicos > 0);
        model.addAttribute("hasProfissionais", totalProfissionais > 1);
        model.addAttribute("hasClientes", totalClientes > 0);
        model.addAttribute("hasProdutos", totalProdutos > 0);
        model.addAttribute("hasAgendamentos", totalAgendamentosGeral > 0);
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
        model.addAttribute("pageTitle", "Dashboard - NEXI PRO");
        return "home/index";
    }
}
