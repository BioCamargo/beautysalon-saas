package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

@ControllerAdvice(basePackages = "com.beautysalon.Controller")
public class GlobalTenantModelAdvice {

    private static final Set<String> SYSTEM_ROUTES = Set.of(
            "login", "register", "register-empresa", "select-tenant",
            "termos", "privacidade", "css", "js", "images", "uploads",
            "favicon.ico", "manifest.json", "service-worker.js", "error",
            "api", "v3", "swagger-ui", "swagger-ui.html"
    );

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;

    public GlobalTenantModelAdvice(EmpresaRepository empresaRepository, UserRepository userRepository) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void populateTenantModel(HttpServletRequest request, Authentication authentication, Model model) {
        String uri = request.getRequestURI();
        if (uri == null || uri.startsWith("/css/") || uri.startsWith("/js/") || uri.startsWith("/images/") || uri.startsWith("/api/")) {
            return;
        }

        String detectedSlug = null;

        // 1. Tenta extrair do primeiro segmento da URL (ex: /studio/home -> studio)
        if (uri.length() > 1) {
            String clean = uri.startsWith("/") ? uri.substring(1) : uri;
            String[] parts = clean.split("/");
            if (parts.length > 0) {
                String firstSegment = parts[0].trim().toLowerCase();
                if (!SYSTEM_ROUTES.contains(firstSegment)) {
                    detectedSlug = firstSegment;
                }
            }
        }

        // 2. Se não detectou na URL, tenta do usuário autenticado
        if (detectedSlug == null && authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud && tud.getEmpresaSlug() != null) {
                detectedSlug = tud.getEmpresaSlug();
            } else {
                userRepository.findByUsername(authentication.getName()).ifPresent(u -> {
                    if (u.getEmpresa() != null) {
                        model.addAttribute("empresaSlug", u.getEmpresa().getSlug());
                        model.addAttribute("empresaNome", u.getEmpresa().getNome());
                    }
                });
            }
        }

        // 3. Se ainda não detectou, tenta do cookie "nexipro_last_slug"
        if (detectedSlug == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("nexipro_last_slug".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                        try {
                            String cookieVal = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8).trim().toLowerCase();
                            if (!SYSTEM_ROUTES.contains(cookieVal)) {
                                detectedSlug = cookieVal;
                            }
                        } catch (Exception ignored) {}
                        break;
                    }
                }
            }
        }

        if (detectedSlug != null && !detectedSlug.isBlank()) {
            if (!model.containsAttribute("empresaSlug")) {
                model.addAttribute("empresaSlug", detectedSlug);
            }
            if (!model.containsAttribute("empresaNome")) {
                Optional<Empresa> empOpt = empresaRepository.findBySlug(detectedSlug);
                if (empOpt.isPresent()) {
                    Empresa emp = empOpt.get();
                    model.addAttribute("empresaNome", emp.getNome());
                    if (emp.getLogoUrl() != null && !model.containsAttribute("empresaLogo")) {
                        model.addAttribute("empresaLogo", emp.getLogoUrl());
                    }
                }
            }
        }
    }
}
