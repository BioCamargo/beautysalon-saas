package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.SegmentoEmpresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Base64;

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
                                     @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
                                     @RequestParam(value = "removerLogo", required = false, defaultValue = "false") boolean removerLogo,
                                     RedirectAttributes redirectAttributes) {
        try {
            Long empresaId = TenantContext.getEmpresaId();
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada para o ID: " + empresaId));

            // Atualiza os dados cadastrais
            if (formEmpresa.getNome() != null && !formEmpresa.getNome().isBlank()) {
                empresa.setNome(formEmpresa.getNome());
            }
            empresa.setCnpj(formEmpresa.getCnpj());
            empresa.setTelefone(formEmpresa.getTelefone());
            empresa.setEmail(formEmpresa.getEmail());
            empresa.setChavePix(formEmpresa.getChavePix());
            empresa.setCidade(formEmpresa.getCidade());
            if (formEmpresa.getSegmento() != null) {
                empresa.setSegmento(formEmpresa.getSegmento());
            }

            // Atualiza a taxa média estimada de cartão para provisionamento da DRE
            if (formEmpresa.getTaxaCartaoEstimada() != null) {
                empresa.setTaxaCartaoEstimada(formEmpresa.getTaxaCartaoEstimada());
            } else {
                empresa.setTaxaCartaoEstimada(BigDecimal.ZERO);
            }

            // Processamento do Logotipo (upload com persistência Base64 Data URL)
            if (removerLogo) {
                empresa.setLogoUrl(null);
            } else if (logoFile != null && !logoFile.isEmpty()) {
                String contentType = logoFile.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    redirectAttributes.addFlashAttribute("mensagemErro", "O arquivo selecionado deve ser uma imagem válida (PNG, JPG, WebP ou SVG).");
                    return "redirect:/" + slug + "/configuracoes/empresa";
                }
                if (logoFile.getSize() > 5 * 1024 * 1024) {
                    redirectAttributes.addFlashAttribute("mensagemErro", "A imagem do logotipo não pode ultrapassar 5MB.");
                    return "redirect:/" + slug + "/configuracoes/empresa";
                }
                String base64Image = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(logoFile.getBytes());
                empresa.setLogoUrl(base64Image);
            }

            empresaRepository.save(empresa);

            // Atualiza o contexto do tenant em memória
            TenantContext.setNome(empresa.getNome());
            TenantContext.setLogoUrl(empresa.getLogoUrl());

            redirectAttributes.addFlashAttribute("mensagemSucesso", "Dados do estabelecimento e chave Pix atualizados com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao atualizar dados: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/empresa";
    }
}
