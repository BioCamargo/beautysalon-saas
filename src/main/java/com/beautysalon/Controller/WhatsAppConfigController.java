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

            String empresaNome = com.beautysalon.tenant.TenantContext.getNome() != null 
                    ? com.beautysalon.tenant.TenantContext.getNome() : "NEXI PRO Studio";
            String dataHoje = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String horaAtual = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

            String mensagemTeste;
            if (config.getMensagemAgendamentoCustom() != null && !config.getMensagemAgendamentoCustom().isBlank()) {
                mensagemTeste = config.getMensagemAgendamentoCustom()
                        .replace("{empresa}", empresaNome)
                        .replace("{cliente}", "Cliente VIP (Teste)")
                        .replace("{servico}", "Corte & Escova Modelada")
                        .replace("{data}", dataHoje)
                        .replace("{hora}", horaAtual);
            } else {
                mensagemTeste = String.format(
                        "🌸 *%s — Teste de Integração WhatsApp*\n\n" +
                        "Olá, *Cliente VIP (Teste)*! ✨\n" +
                        "Seu agendamento para *Corte & Escova Modelada* foi confirmado com sucesso!\n\n" +
                        "📅 *Data:* %s\n" +
                        "⏰ *Horário:* %s\n\n" +
                        "As variáveis dinâmicas ({cliente}, {servico}, {data}, {hora}) foram substituídas perfeitamente! 🚀",
                        empresaNome, dataHoje, horaAtual
                );
            }

            boolean enviado = whatsAppService.enviarMensagemTexto(
                    config,
                    telefoneTeste,
                    mensagemTeste
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

    /**
     * Pareamento instantâneo em 1 clique (Modo Demonstração / Novo Estabelecimento).
     */
    @PostMapping("/conectar-demo")
    public String conectarDemo(@PathVariable String slug,
                               @RequestParam(required = false) String telefone,
                               RedirectAttributes redirectAttributes) {
        try {
            WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual()
                    .orElseThrow(() -> new IllegalStateException("Configuração do WhatsApp não encontrada."));

            String numero = (telefone != null && !telefone.isBlank()) 
                    ? telefone : "(11) 95430-1259";

            whatsAppService.simularConexaoTeste(config, numero);
            redirectAttributes.addFlashAttribute("mensagemSucesso", 
                    "🎉 WhatsApp conectado com sucesso! As automações de lembrete, agendamento e comanda já estão ativas.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao conectar WhatsApp: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/whatsapp";
    }

    /**
     * Desconecta o WhatsApp pareado.
     */
    @PostMapping("/desconectar")
    public String desconectarWhatsApp(@PathVariable String slug,
                                      RedirectAttributes redirectAttributes) {
        try {
            WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual().orElse(null);
            if (config != null) {
                whatsAppService.desconectar(config);
            }
            redirectAttributes.addFlashAttribute("mensagemSucesso", "WhatsApp desconectado com sucesso. Você pode parear um novo aparelho quando desejar.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao desconectar: " + e.getMessage());
        }
        return "redirect:/" + slug + "/configuracoes/whatsapp";
    }

    /**
     * Endpoint AJAX para polling de status de conexão do WhatsApp.
     */
    @GetMapping("/status")
    @ResponseBody
    public java.util.Map<String, Object> consultarStatus(@PathVariable String slug) {
        WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual().orElse(null);
        if (config == null) {
            return java.util.Map.of("conectado", false);
        }

        boolean conectado = config.isConectado();
        if (!conectado) {
            conectado = whatsAppService.verificarEAtualizarConexaoReal(config);
        }

        return java.util.Map.of(
                "conectado", conectado,
                "ativo", config.isAtivo(),
                "numeroConectado", config.getNumeroConectado() != null ? config.getNumeroConectado() : "",
                "instanceName", config.getInstanceName() != null ? config.getInstanceName() : slug
        );
    }

    /**
     * Endpoint AJAX para obter o QR Code em tempo real da Evolution API para pareamento via WhatsApp Web.
     */
    @GetMapping("/qrcode")
    @ResponseBody
    public java.util.Map<String, Object> obterQrCode(@PathVariable String slug) {
        WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual().orElse(null);
        if (config == null) {
            return java.util.Map.of("sucesso", false, "mensagem", "Nenhuma configuração encontrada.");
        }
        return whatsAppService.obterQrCodeInstancia(config);
    }

    /**
     * Auto-registra o webhook do NEXI PRO na Evolution API.
     */
    @PostMapping("/webhook/auto-config")
    @ResponseBody
    public java.util.Map<String, Object> autoConfigurarWebhook(@PathVariable String slug,
                                                               jakarta.servlet.http.HttpServletRequest request) {
        WhatsAppConfig config = whatsAppService.obterConfiguracaoAtual().orElse(null);
        if (config == null) {
            return java.util.Map.of("sucesso", false, "mensagem", "Nenhuma configuração encontrada.");
        }

        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String portPart = (port == 80 || port == 443) ? "" : ":" + port;
        String webhookUrl = scheme + "://" + host + portPart + "/api/v1/webhooks/whatsapp";

        boolean configurado = whatsAppService.configurarWebhook(config, webhookUrl);
        return java.util.Map.of(
                "sucesso", configurado,
                "webhookUrl", webhookUrl,
                "mensagem", configurado ? "Webhook configurado com sucesso na Evolution API!" : "Não foi possível registrar o webhook automaticamente."
        );
    }
}
