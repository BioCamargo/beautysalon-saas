package com.beautysalon.Controller;

import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.Inteface.ServicoService;
import com.beautysalon.model.CupomDesconto;
import com.beautysalon.model.PacoteCombo;
import com.beautysalon.model.VoucherPresente;
import com.beautysalon.service.FidelizacaoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/{slug}/fidelizacao")
public class FidelizacaoController {

    private final FidelizacaoService fidelizacaoService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;

    public FidelizacaoController(FidelizacaoService fidelizacaoService,
                                 ClienteService clienteService,
                                 ServicoService servicoService) {
        this.fidelizacaoService = fidelizacaoService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
    }

    @GetMapping
    public String index(@PathVariable String slug, Model model) {
        int mesAtual = LocalDate.now().getMonthValue();
        model.addAttribute("aniversariantes", fidelizacaoService.listarAniversariantesDoMes(mesAtual));
        model.addAttribute("clientesAusentes", fidelizacaoService.buscarClientesEmRiscoRetorno(30)); // 30 dias sem vir
        model.addAttribute("cupons", fidelizacaoService.listarCupons());
        model.addAttribute("vouchers", fidelizacaoService.listarVouchers());
        model.addAttribute("combos", fidelizacaoService.listarCombos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Fidelização & CRM");
        return "fidelizacao/index";
    }

    @GetMapping("/cliente/{id}/historico")
    public String historicoCliente(@PathVariable String slug, @PathVariable Long id, Model model) {
        model.addAttribute("dados", fidelizacaoService.obterSugestoesEHistoricoCliente(id));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Histórico & Fidelização do Cliente");
        return "fidelizacao/historico-cliente";
    }

    @PostMapping("/cupons")
    public String criarCupom(@PathVariable String slug,
                             @ModelAttribute("cupom") CupomDesconto cupom,
                             RedirectAttributes redirectAttributes) {
        fidelizacaoService.salvarCupom(cupom);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cupom criado com sucesso!");
        return "redirect:/" + slug + "/fidelizacao";
    }

    @PostMapping("/vouchers")
    public String criarVoucher(@PathVariable String slug,
                               @ModelAttribute("voucher") VoucherPresente voucher,
                               RedirectAttributes redirectAttributes) {
        fidelizacaoService.criarVoucher(voucher);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Cartão Presente gerado com sucesso!");
        return "redirect:/" + slug + "/fidelizacao";
    }

    @PostMapping("/combos")
    public String criarCombo(@PathVariable String slug,
                             @ModelAttribute("combo") PacoteCombo combo,
                             RedirectAttributes redirectAttributes) {
        fidelizacaoService.salvarCombo(combo);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Pacote/Combo cadastrado com sucesso!");
        return "redirect:/" + slug + "/fidelizacao";
    }
}
