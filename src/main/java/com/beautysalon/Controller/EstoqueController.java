package com.beautysalon.Controller;

import com.beautysalon.model.Produto;
import com.beautysalon.model.TipoProduto;
import com.beautysalon.service.EstoqueService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/{slug}/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    public String index(@PathVariable String slug, Model model) {
        model.addAttribute("produtos", estoqueService.listarTodos());
        model.addAttribute("produtosBaixo", estoqueService.listarEstoqueBaixo());
        model.addAttribute("valorParado", estoqueService.calcularValorParadoEmEstoque());
        model.addAttribute("qtdEstoqueBaixo", estoqueService.contarEstoqueBaixo());
        model.addAttribute("movimentacoes", estoqueService.listarMovimentacoes());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Gestão de Estoque");
        return "estoque/index";
    }

    @GetMapping("/produtos/novo")
    public String novoProdutoForm(@PathVariable String slug, Model model) {
        model.addAttribute("produto", new Produto());
        model.addAttribute("tipos", TipoProduto.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Novo Produto");
        return "estoque/form";
    }

    @PostMapping("/produtos")
    public String salvarProduto(@PathVariable String slug,
                                @ModelAttribute("produto") Produto produto,
                                RedirectAttributes redirectAttributes) {
        estoqueService.salvar(produto);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto salvo com sucesso!");
        return "redirect:/" + slug + "/estoque";
    }

    @GetMapping("/produtos/{id}/editar")
    public String editarProdutoForm(@PathVariable String slug, @PathVariable Long id, Model model) {
        model.addAttribute("produto", estoqueService.buscarPorId(id));
        model.addAttribute("tipos", TipoProduto.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Editar Produto");
        return "estoque/form";
    }

    @PostMapping("/movimentacao")
    public String registrarMovimentacao(@PathVariable String slug,
                                        @RequestParam Long produtoId,
                                        @RequestParam String tipo,
                                        @RequestParam int quantidade,
                                        @RequestParam String motivo,
                                        RedirectAttributes redirectAttributes) {
        estoqueService.registrarMovimentacao(produtoId, tipo, quantidade, motivo, null);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Movimentação registrada com sucesso!");
        return "redirect:/" + slug + "/estoque";
    }

    @PostMapping("/produtos/{id}/deletar")
    public String deletar(@PathVariable String slug, @PathVariable Long id, RedirectAttributes redirectAttributes) {
        estoqueService.excluir(id);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto removido com sucesso!");
        return "redirect:/" + slug + "/estoque";
    }
}
