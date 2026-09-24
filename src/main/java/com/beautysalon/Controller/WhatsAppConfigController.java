package com.beautysalon.Controller;

import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.service.WhatsAppService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/{slug}/configuracoes/whatsapp")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class WhatsAppConfigController {

    private final WhatsAppService whatsAppService;

    public WhatsAppConfigController(WhatsAppService whatsAppService) {
        this.whatsAppService = whatsAppService;
    }

    @GetMapping
    public String paginaConfiguracao(@PathVariable String slug, Model model) {
        WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual()
                .orElseGet(() -> WhatsAppConfig.builder().provider("META_OFFICIAL").ativo(false).build());

        model.addAttribute("config", config);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Integração WhatsApp");
        return "configuracoes/whatsapp";
    }

    @PostMapping
    public String salvarConfiguracao(@PathVariable String slug,
                                     @ModelAttribute WhatsAppConfig config,
                                     RedirectAttributes redirectAttributes) {
        try {
            whatsAppService.salvarConfiguracao(config);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Configurações do WhatsApp salvas com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao salvar configurações: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/whatsapp";
    }

    @PostMapping("/teste")
    public String enviarTeste(@PathVariable String slug,
                              @RequestParam String telefoneTeste,
                              RedirectAttributes redirectAttributes) {
        try {
            WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual()
                    .orElseThrow(() -> new IllegalStateException("Nenhuma configuração do WhatsApp encontrada."));

            boolean enviado = whatsAppService.enviarMensagemTexto(
                    config,
                    telefoneTeste,
                    "🎉 *Teste de Conexão WhatsApp*\n\nSeu estúdio está conectado com sucesso ao sistema BeautySalon SaaS! As mensagens automáticas estão ativas."
            );

            if (enviado) {
                redirectAttributes.addFlashAttribute("mensagemSucesso", "Mensagem de teste enviada com sucesso para " + telefoneTeste + "!");
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro", "Não foi possível enviar o teste. Verifique suas credenciais e formato do número.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao testar envio: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/whatsapp";
    }
}
