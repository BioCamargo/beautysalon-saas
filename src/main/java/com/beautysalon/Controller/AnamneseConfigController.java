package com.beautysalon.Controller;

import com.beautysalon.model.AnamneseCampoCustomizado;
import com.beautysalon.model.TipoCampoCustomizado;
import com.beautysalon.service.AnamneseFormBuilderService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/{slug}/configuracoes/anamnese")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class AnamneseConfigController {

    private final AnamneseFormBuilderService formBuilderService;

    public AnamneseConfigController(AnamneseFormBuilderService formBuilderService) {
        this.formBuilderService = formBuilderService;
    }

    @GetMapping
    public String index(@PathVariable String slug, Model model) {
        List<AnamneseCampoCustomizado> campos = formBuilderService.listarTodosDoTenant();
        model.addAttribute("campos", campos);
        model.addAttribute("tiposCampo", TipoCampoCustomizado.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Configuração da Ficha de Anamnese (Form Builder)");
        return "configuracoes/anamnese-builder";
    }

    @PostMapping("/salvar")
    public String salvarCampo(@PathVariable String slug,
                              @RequestParam(required = false) Long id,
                              @RequestParam String rotulo,
                              @RequestParam TipoCampoCustomizado tipoCampo,
                              @RequestParam(required = false) String opcoes,
                              @RequestParam(required = false) String placeholder,
                              @RequestParam(defaultValue = "false") boolean obrigatorio,
                              @RequestParam(defaultValue = "0") Integer ordem,
                              RedirectAttributes redirectAttributes) {
        try {
            formBuilderService.criarOuAtualizarCampo(id, rotulo, tipoCampo, opcoes, placeholder, obrigatorio, ordem);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Campo da ficha de anamnese salvo com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar campo: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/anamnese";
    }

    @PostMapping("/{campoId}/alternar-status")
    public String alternarStatus(@PathVariable String slug,
                                 @PathVariable Long campoId,
                                 RedirectAttributes redirectAttributes) {
        try {
            formBuilderService.alternarStatus(campoId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Status do campo atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao alterar status: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/anamnese";
    }

    @PostMapping("/{campoId}/excluir")
    public String excluirCampo(@PathVariable String slug,
                               @PathVariable Long campoId,
                               RedirectAttributes redirectAttributes) {
        try {
            formBuilderService.excluirCampo(campoId);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Campo excluído com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao excluir campo: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/anamnese";
    }
}
