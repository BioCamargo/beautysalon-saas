package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/{slug}/divulgacao")
public class DivulgacaoController {

    private final EmpresaRepository empresaRepository;

    public DivulgacaoController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public String index(@PathVariable String slug, HttpServletRequest request, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada: " + empresaId));

        // Constrói a URL base dinâmica do servidor atual
        int port = request.getServerPort();
        String portStr = (port == 80 || port == 443) ? "" : ":" + port;
        String baseUrl = request.getScheme() + "://" + request.getServerName() + portStr;

        String linkBio = baseUrl + "/p/" + slug;
        String linkAgendar = baseUrl + "/" + slug + "/agendar";
        boolean isLocalhost = "localhost".equalsIgnoreCase(request.getServerName()) 
                || "127.0.0.1".equals(request.getServerName());
        String linkBioProducao = "https://app.nexipro.com.br/p/" + slug;

        model.addAttribute("empresa", empresa);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("linkBio", linkBio);
        model.addAttribute("linkBioProducao", linkBioProducao);
        model.addAttribute("isLocalhost", isLocalhost);
        model.addAttribute("linkAgendar", linkAgendar);
        model.addAttribute("pageTitle", "Divulgação & QR Code de Balcão");
        model.addAttribute("subtitle", "Gere materiais com QR Code para balcão, espelhos e link da bio do Instagram");

        return "configuracoes/divulgacao";
    }
}
