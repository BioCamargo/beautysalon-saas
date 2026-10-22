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
    private final com.beautysalon.service.AsaasClientService asaasClientService;

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

        // Se o gateway Asaas estiver configurado, cria/atualiza a cobrança e fatura
        try {
            java.util.Optional<String> linkFatura = asaasClientService.criarOuAtualizarAssinatura(empresa, novoPlano);
            if (linkFatura.isPresent() && !linkFatura.get().isBlank()) {
                redirectAttributes.addFlashAttribute("sucesso", "Plano alterado para " + novoPlano.getNomeExibicao() + "! Sua fatura de pagamento foi gerada com sucesso.");
                return "redirect:" + linkFatura.get();
            }
        } catch (Exception e) {
            log.warn("Aviso ao tentar gerar assinatura no Asaas: {}", e.getMessage());
        }

        log.info("Empresa ID={} ({}) alterou plano para {}", empresa.getId(), slug, novoPlano);
        redirectAttributes.addFlashAttribute("sucesso", "Plano alterado para " + novoPlano.getNomeExibicao() + " com sucesso!");

        return "redirect:/" + slug + "/assinatura";
    }

    @PostMapping("/gerar-fatura")
    public String gerarFatura(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + slug));

        PlanoSaaS plano = empresa.getPlano() != null ? empresa.getPlano() : PlanoSaaS.STUDIO;

        try {
            java.util.Optional<String> linkFatura = asaasClientService.criarOuAtualizarAssinatura(empresa, plano);
            if (linkFatura.isPresent() && !linkFatura.get().isBlank()) {
                return "redirect:" + linkFatura.get();
            }
        } catch (Exception e) {
            log.error("Erro ao gerar fatura avulsa no Asaas: {}", e.getMessage());
        }

        redirectAttributes.addFlashAttribute("erro", "Não foi possível gerar a fatura no momento. Verifique as configurações de pagamento.");
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

    @PostMapping("/simular-status")
    public String simularStatus(@PathVariable String slug,
                                @RequestParam String status,
                                RedirectAttributes redirectAttributes) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + slug));

        java.time.LocalDateTime agora = java.time.LocalDateTime.now();

        switch (status.toUpperCase()) {
            case "TRIAL_EXPIRADO":
                empresa.setStatusAssinatura(com.beautysalon.model.StatusAssinatura.EXPIRADA);
                empresa.setDataExpiracaoTrial(agora.minusDays(1));
                redirectAttributes.addFlashAttribute("sucesso", "Simulação: Trial expirado! Veja como o sistema bloqueia novos agendamentos e solicita o pagamento.");
                break;
            case "INADIMPLENTE":
                empresa.setStatusAssinatura(com.beautysalon.model.StatusAssinatura.INADIMPLENTE);
                empresa.setDataFimAssinatura(agora.minusDays(4));
                redirectAttributes.addFlashAttribute("sucesso", "Simulação: Fatura em atraso (Inadimplente com carência estourada).");
                break;
            case "ATIVA":
                empresa.setStatusAssinatura(com.beautysalon.model.StatusAssinatura.ATIVA);
                empresa.setDataFimAssinatura(agora.plusDays(30));
                redirectAttributes.addFlashAttribute("sucesso", "Simulação: Assinatura Ativa e regularizada por +30 dias!");
                break;
            default: // RESTAURAR_TRIAL
                empresa.setStatusAssinatura(com.beautysalon.model.StatusAssinatura.TRIAL);
                empresa.setDataExpiracaoTrial(agora.plusDays(7));
                redirectAttributes.addFlashAttribute("sucesso", "Simulação: Período de Testes restaurado para 7 dias grátis.");
                break;
        }

        empresaRepository.save(empresa);
        log.info("Simulação de assinatura executada para empresa ID={} (slug={}): novo status={}", 
                empresa.getId(), slug, empresa.getStatusAssinatura());

        return "redirect:/" + slug + "/assinatura";
    }
}
