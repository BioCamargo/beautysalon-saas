package com.beautysalon.Controller;

import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final com.beautysalon.repository.ClienteRepository clienteRepository;
    private final com.beautysalon.repository.AgendamentoRepository agendamentoRepository;
    private final com.beautysalon.service.EstoqueService estoqueService;
    private final com.beautysalon.service.FinanceiroService financeiroService;
    private final com.beautysalon.service.FidelizacaoService fidelizacaoService;

    public HomeController(UserRepository userRepository,
                          com.beautysalon.repository.ClienteRepository clienteRepository,
                          com.beautysalon.repository.AgendamentoRepository agendamentoRepository,
                          com.beautysalon.service.EstoqueService estoqueService,
                          com.beautysalon.service.FinanceiroService financeiroService,
                          com.beautysalon.service.FidelizacaoService fidelizacaoService) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.estoqueService = estoqueService;
        this.financeiroService = financeiroService;
        this.fidelizacaoService = fidelizacaoService;
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

        model.addAttribute("contentPage", "home/indexContent");
        model.addAttribute("content", "home/welcome");
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Dashboard - Lumora");
        return "home/index";
    }
}
