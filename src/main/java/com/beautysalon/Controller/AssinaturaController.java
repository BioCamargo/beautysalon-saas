package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.PlanoSaaS;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;

@Slf4j
@Controller
@RequestMapping("/{slug}/assinatura")
@RequiredArgsConstructor
public class AssinaturaController {

    private final EmpresaRepository empresaRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PlanoSaaS.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(PlanoSaaS.fromString(text));
            }
        });
    }

    @GetMapping
    public String exibirGestaoAssinatura(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + slug));

        model.addAttribute("empresa", empresa);
        model.addAttribute("pageTitle", "Gestão da Assinatura & Planos SaaS");
        model.addAttribute("activePage", "assinatura");
        model.addAttribute("planos", PlanoSaaS.values());
        model.addAttribute("empresaSlug", slug);

        return "configuracoes/assinatura";
    }

    @PostMapping("/alterar-plano")
    public String alterarPlano(@PathVariable String slug,
                               @RequestParam PlanoSaaS novoPlano,
                               RedirectAttributes redirectAttributes) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + slug));

        empresa.setPlano(novoPlano);
        empresaRepository.save(empresa);

        log.info("Empresa ID={} ({}) alterou plano para {}", empresa.getId(), slug, novoPlano);
        redirectAttributes.addFlashAttribute("sucesso", "Plano alterado para " + novoPlano.getNomeExibicao() + " com sucesso!");

        return "redirect:/" + slug + "/assinatura";
    }

    @PostMapping("/atualizar-gateway")
    public String atualizarDadosGateway(@PathVariable String slug,
                                        @RequestParam(required = false) String asaasCustomerId,
                                        @RequestParam(required = false) String asaasSubscriptionId,
                                        @RequestParam(required = false) String linkFaturaAtual,
                                        RedirectAttributes redirectAttributes) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + slug));

        if (asaasCustomerId != null && !asaasCustomerId.isBlank()) {
            empresa.setAsaasCustomerId(asaasCustomerId.trim());
        }
        if (asaasSubscriptionId != null && !asaasSubscriptionId.isBlank()) {
            empresa.setAsaasSubscriptionId(asaasSubscriptionId.trim());
        }
        if (linkFaturaAtual != null && !linkFaturaAtual.isBlank()) {
            empresa.setLinkFaturaAtual(linkFaturaAtual.trim());
        }

        empresaRepository.save(empresa);
        log.info("Dados de gateway Asaas atualizados para empresa ID={} ({})", empresa.getId(), slug);
        redirectAttributes.addFlashAttribute("sucesso", "Identificadores de faturamento atualizados com sucesso!");

        return "redirect:/" + slug + "/assinatura";
    }
}
