package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.SegmentoEmpresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/{slug}/configuracoes/empresa")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class EmpresaConfigController {

    private final EmpresaRepository empresaRepository;

    public EmpresaConfigController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public String paginaConfiguracao(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada para o ID: " + empresaId));

        model.addAttribute("empresa", empresa);
        model.addAttribute("segmentos", SegmentoEmpresa.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Dados da Empresa & Pix");
        model.addAttribute("activePage", "empresa");
        return "configuracoes/empresa";
    }

    @PostMapping
    public String salvarConfiguracao(@PathVariable String slug,
                                     @ModelAttribute Empresa formEmpresa,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long empresaId = TenantContext.getEmpresaId();
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada para o ID: " + empresaId));

            // Atualiza os dados cadastrais e de pagamento
            if (formEmpresa.getNome() != null && !formEmpresa.getNome().isBlank()) {
                empresa.setNome(formEmpresa.getNome());
            }
            empresa.setCnpj(formEmpresa.getCnpj());
            empresa.setTelefone(formEmpresa.getTelefone());
            empresa.setEmail(formEmpresa.getEmail());
            empresa.setLogoUrl(formEmpresa.getLogoUrl());
            empresa.setChavePix(formEmpresa.getChavePix());
            empresa.setCidade(formEmpresa.getCidade());
            if (formEmpresa.getSegmento() != null) {
                empresa.setSegmento(formEmpresa.getSegmento());
            }

            empresaRepository.save(empresa);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados do estabelecimento e chave Pix atualizados com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar dados: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/empresa";
    }
}
