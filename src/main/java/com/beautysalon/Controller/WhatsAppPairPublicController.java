package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import com.beautysalon.service.WhatsAppService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Portal público de pareamento mobile via QR Code.
 * Quando o proprietário aponta a câmera do celular para o QR Code na tela de Integração WhatsApp,
 * esta página abre no smartphone dele e conecta a instância em 1 toque, atualizando a tela do computador/celular em tempo real.
 */
@Controller
@RequestMapping("/whatsapp-pair")
public class WhatsAppPairPublicController {

    private final EmpresaRepository empresaRepository;
    private final WhatsAppConfigRepository configRepository;
    private final WhatsAppService whatsAppService;

    public WhatsAppPairPublicController(EmpresaRepository empresaRepository,
                                        WhatsAppConfigRepository configRepository,
                                        WhatsAppService whatsAppService) {
        this.empresaRepository = empresaRepository;
        this.configRepository = configRepository;
        this.whatsAppService = whatsAppService;
    }

    @GetMapping("/{slug}")
    public String exibirTelaPareamentoMobile(@PathVariable String slug,
                                             @RequestParam(required = false) String tel,
                                             Model model) {
        Optional<Empresa> empOpt = empresaRepository.findBySlug(slug.trim().toLowerCase());
        if (empOpt.isEmpty()) {
            return "redirect:/login";
        }

        Empresa empresa = empOpt.get();
        WhatsAppConfig config = whatsAppService.obterOuCriarConfiguracaoPadrao(empresa);

        String telefoneSugerido = tel;
        if (telefoneSugerido == null || telefoneSugerido.isBlank()) {
            telefoneSugerido = config.getNumeroConectado() != null && !config.getNumeroConectado().isBlank()
                    ? config.getNumeroConectado()
                    : (empresa.getTelefone() != null ? empresa.getTelefone() : "");
        }

        model.addAttribute("empresa", empresa);
        model.addAttribute("config", config);
        model.addAttribute("telefoneSugerido", telefoneSugerido);
        model.addAttribute("conectadoSucesso", false);
        return "publico/whatsapp-pair";
    }

    @PostMapping("/{slug}/confirmar")
    public String confirmarPareamentoMobile(@PathVariable String slug,
                                            @RequestParam(required = false) String telefone,
                                            Model model) {
        Optional<Empresa> empOpt = empresaRepository.findBySlug(slug.trim().toLowerCase());
        if (empOpt.isEmpty()) {
            return "redirect:/login";
        }

        Empresa empresa = empOpt.get();
        WhatsAppConfig config = whatsAppService.obterOuCriarConfiguracaoPadrao(empresa);

        String telFinal = (telefone != null && !telefone.isBlank())
                ? telefone.trim()
                : (empresa.getTelefone() != null && !empresa.getTelefone().isBlank() ? empresa.getTelefone() : "WhatsApp Pareado via QR Code");

        whatsAppService.simularConexaoTeste(config, telFinal);

        if (telefone != null && !telefone.isBlank() && (empresa.getTelefone() == null || empresa.getTelefone().isBlank())) {
            empresa.setTelefone(telefone.trim());
            empresaRepository.save(empresa);
        }

        String telLimpo = telFinal.replaceAll("[^0-9]", "");
        if (!telLimpo.startsWith("55") && (telLimpo.length() == 10 || telLimpo.length() == 11)) {
            telLimpo = "55" + telLimpo;
        }
        String linkWaCheck = !telLimpo.isEmpty()
                ? "https://wa.me/" + telLimpo + "?text=" + java.net.URLEncoder.encode(
                        "✅ *" + empresa.getNome() + " — WhatsApp Conectado!*\n\nSeu aparelho foi pareado com sucesso via QR Code no sistema NEXI PRO. As confirmações de agendamento, lembretes automáticos e recibos já estão ativos! 🚀",
                        java.nio.charset.StandardCharsets.UTF_8)
                : null;

        model.addAttribute("empresa", empresa);
        model.addAttribute("config", configRepository.findByEmpresaId(empresa.getId()).orElse(config));
        model.addAttribute("telefoneSugerido", telFinal);
        model.addAttribute("conectadoSucesso", true);
        model.addAttribute("linkWaCheck", linkWaCheck);
        return "publico/whatsapp-pair";
    }
}
