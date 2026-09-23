package com.beautysalon.Controller;

import com.beautysalon.model.Caixa;
import com.beautysalon.model.Comanda;
import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.EstoqueService;
import com.beautysalon.service.FidelizacaoService;
import com.beautysalon.service.FinanceiroService;
import com.beautysalon.service.ProfissionalService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/{slug}/financeiro")
public class FinanceiroController {

    private final FinanceiroService financeiroService;
    private final EstoqueService estoqueService;
    private final ProfissionalService profissionalService;
    private final FidelizacaoService fidelizacaoService;
    private final UserRepository userRepository;

    public FinanceiroController(FinanceiroService financeiroService,
                                EstoqueService estoqueService,
                                ProfissionalService profissionalService,
                                FidelizacaoService fidelizacaoService,
                                UserRepository userRepository) {
        this.financeiroService = financeiroService;
        this.estoqueService = estoqueService;
        this.profissionalService = profissionalService;
        this.fidelizacaoService = fidelizacaoService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@PathVariable String slug, Model model) {
        Optional<Caixa> caixaAbertoOpt = financeiroService.buscarCaixaAberto();
        model.addAttribute("caixaAberto", caixaAbertoOpt.orElse(null));
        model.addAttribute("historicoCaixas", financeiroService.listarHistoricoCaixas());
        model.addAttribute("comandasAbertas", financeiroService.listarComandasAbertas());
        model.addAttribute("todasComandas", financeiroService.listarComandas());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Financeiro & Caixa");
        return "financeiro/index";
    }

    @PostMapping("/caixa/abrir")
    public String abrirCaixa(@PathVariable String slug,
                             @RequestParam(defaultValue = "0") BigDecimal saldoInicial,
                             @RequestParam(required = false) String observacoes,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User operador = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            operador = userRepository.findByUsernameAndEmpresaId(auth.getName(), com.beautysalon.tenant.TenantContext.getEmpresaId())
                    .or(() -> userRepository.findByUsername(auth.getName()))
                    .orElse(null);
        }
        try {
            financeiroService.abrirCaixa(saldoInicial, observacoes, operador);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Caixa aberto com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/" + slug + "/financeiro";
    }

    @PostMapping("/caixa/{id}/fechar")
    public String fecharCaixa(@PathVariable String slug,
                              @PathVariable Long id,
                              @RequestParam(required = false) BigDecimal saldoFinalContado,
                              @RequestParam(required = false) String observacoes,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        User operador = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            operador = userRepository.findByUsernameAndEmpresaId(auth.getName(), com.beautysalon.tenant.TenantContext.getEmpresaId())
                    .or(() -> userRepository.findByUsername(auth.getName()))
                    .orElse(null);
        }
        try {
            financeiroService.fecharCaixa(id, saldoFinalContado, observacoes, operador);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Caixa fechado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/" + slug + "/financeiro";
    }

    @PostMapping("/caixa/{id}/movimentacao")
    public String movimentacaoCaixa(@PathVariable String slug,
                                    @PathVariable Long id,
                                    @RequestParam String tipo,
                                    @RequestParam BigDecimal valor,
                                    @RequestParam(required = false) String categoria,
                                    @RequestParam(required = false) String descricao,
                                    Authentication auth,
                                    RedirectAttributes redirectAttributes) {
        User operador = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            operador = userRepository.findByUsernameAndEmpresaId(auth.getName(), com.beautysalon.tenant.TenantContext.getEmpresaId())
                    .or(() -> userRepository.findByUsername(auth.getName()))
                    .orElse(null);
        }
        try {
            financeiroService.registrarMovimentacaoCaixa(id, tipo, valor, categoria, descricao, operador);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação de caixa registrada!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/" + slug + "/financeiro";
    }

    // ================= COMANDAS =================

    @GetMapping("/comandas/{id}")
    public String detalheComanda(@PathVariable String slug, @PathVariable Long id, Model model) {
        Comanda comanda = financeiroService.buscarComandaPorId(id);
        model.addAttribute("comanda", comanda);
        model.addAttribute("produtosRevenda", estoqueService.listarPorTipo(com.beautysalon.model.TipoProduto.REVENDA));
        model.addAttribute("profissionais", profissionalService.listarProfissionaisAtivos());
        model.addAttribute("cupons", fidelizacaoService.listarCupons());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Comanda " + comanda.getNumeroComanda());
        return "financeiro/comanda-detalhe";
    }

    @PostMapping("/comandas/nova")
    public String novaComanda(@PathVariable String slug,
                              @RequestParam(required = false) String nomeAvulso,
                              @RequestParam(required = false) String observacoes,
                              RedirectAttributes redirectAttributes) {
        Comanda c = financeiroService.criarComanda(null, nomeAvulso, null, observacoes);
        return "redirect:/" + slug + "/financeiro/comandas/" + c.getId();
    }

    @PostMapping("/comandas/{id}/adicionar-item")
    public String adicionarItemComanda(@PathVariable String slug,
                                       @PathVariable Long id,
                                       @RequestParam String tipo,
                                       @RequestParam(required = false) Long servicoId,
                                       @RequestParam(required = false) Long produtoId,
                                       @RequestParam(required = false) Long profissionalId,
                                       @RequestParam(defaultValue = "1") int quantidade,
                                       @RequestParam(required = false) BigDecimal precoUnitario,
                                       @RequestParam(required = false) BigDecimal percentualComissao,
                                       Authentication auth) {
        User usuario = auth != null ? userRepository.findByUsername(auth.getName()).orElse(null) : null;
        financeiroService.adicionarItemComanda(id, tipo, servicoId, produtoId, profissionalId, quantidade, precoUnitario, percentualComissao, usuario);
        return "redirect:/" + slug + "/financeiro/comandas/" + id;
    }

    @PostMapping("/comandas/{id}/fechar")
    public String fecharComanda(@PathVariable String slug,
                                @PathVariable Long id,
                                @RequestParam String formaPagamento,
                                @RequestParam(required = false) BigDecimal desconto,
                                @RequestParam(required = false) BigDecimal acrescimo,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        User operador = auth != null ? userRepository.findByUsername(auth.getName()).orElse(null) : null;
        try {
            financeiroService.fecharComanda(id, formaPagamento, desconto, acrescimo, operador);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Comanda finalizada e paga com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", e.getMessage());
        }
        return "redirect:/" + slug + "/financeiro";
    }
}
