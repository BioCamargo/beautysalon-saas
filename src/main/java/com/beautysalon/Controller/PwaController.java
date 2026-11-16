package com.beautysalon.Controller;

import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
public class PwaController {

    private static final Set<String> SYSTEM_ROUTES = com.beautysalon.tenant.TenantInterceptor.SYSTEM_ROUTES;

    private final EmpresaRepository empresaRepository;

    public PwaController(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    @GetMapping(value = {"/manifest.json", "/{slug}/manifest.json"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getManifest(
            @PathVariable(required = false) String slug,
            @RequestParam(required = false) String empresa,
            HttpServletRequest request,
            Authentication authentication) {

        String targetSlug = slug;
        if (targetSlug == null || targetSlug.isBlank()) {
            targetSlug = empresa;
        }

        // 1. Tenta obter do usuário autenticado no contexto
        if ((targetSlug == null || targetSlug.isBlank()) && authentication != null && authentication.isAuthenticated()) {
            if (authentication.getPrincipal() instanceof TenantUserDetails tud && tud.getEmpresaSlug() != null) {
                targetSlug = tud.getEmpresaSlug();
            }
        }

        // 2. Tenta obter do cookie "nexipro_last_slug"
        if (targetSlug == null || targetSlug.isBlank()) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie c : cookies) {
                    if ("nexipro_last_slug".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                        try {
                            targetSlug = URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8);
                        } catch (Exception ignored) {
                            targetSlug = c.getValue();
                        }
                        break;
                    }
                }
            }
        }

        // 3. Tenta obter pelo header Referer (ex: https://nexipro.com.br/studio/home)
        if (targetSlug == null || targetSlug.isBlank()) {
            String referer = request.getHeader("Referer");
            if (referer != null) {
                try {
                    URI uri = URI.create(referer);
                    String path = uri.getPath();
                    if (path != null && path.length() > 1) {
                        String clean = path.startsWith("/") ? path.substring(1) : path;
                        String[] parts = clean.split("/");
                        if (parts.length > 0 && !SYSTEM_ROUTES.contains(parts[0].toLowerCase())) {
                            targetSlug = parts[0];
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        Map<String, Object> manifest = new LinkedHashMap<>();

        // Se identificou um tenant/empresa válido
        if (targetSlug != null && !targetSlug.isBlank()) {
            Optional<Empresa> empOpt = empresaRepository.findBySlug(targetSlug.trim().toLowerCase());
            if (empOpt.isPresent()) {
                Empresa emp = empOpt.get();
                String appName = "NEXI PRO - " + emp.getNome();
                String shortName = emp.getNome().length() > 14 ? emp.getNome().substring(0, 14) : emp.getNome();

                manifest.put("id", "/" + emp.getSlug() + "/");
                manifest.put("name", appName);
                manifest.put("short_name", shortName);
                manifest.put("description", "Aplicativo Oficial de Gestão - " + emp.getNome());
                // Inicia direto no link exclusivo da empresa (se logado vai pra home, se não vai pro login da empresa)
                manifest.put("start_url", "/" + emp.getSlug() + "/login");
                manifest.put("scope", "/");
                manifest.put("display", "standalone");
                manifest.put("orientation", "portrait-primary");
                manifest.put("background_color", "#0B0E14");
                manifest.put("theme_color", "#00D2EE");
                manifest.put("categories", List.of("business", "productivity"));
                manifest.put("icons", getIconsList());
                return ResponseEntity.ok(manifest);
            }
        }

        // Fallback genérico caso não haja slug identificável
        manifest.put("id", "/");
        manifest.put("name", "NEXI PRO - Gestão Inteligente");
        manifest.put("short_name", "NEXI PRO");
        manifest.put("description", "Gestão Inteligente & Alta Performance para Empresas, Barbearias e Clínicas");
        manifest.put("start_url", "/");
        manifest.put("scope", "/");
        manifest.put("display", "standalone");
        manifest.put("orientation", "portrait-primary");
        manifest.put("background_color", "#0B0E14");
        manifest.put("theme_color", "#00D2EE");
        manifest.put("categories", List.of("business", "productivity"));
        manifest.put("icons", getIconsList());
        return ResponseEntity.ok(manifest);
    }

    private List<Map<String, String>> getIconsList() {
        List<Map<String, String>> icons = new ArrayList<>();

        Map<String, String> icon192 = new LinkedHashMap<>();
        icon192.put("src", "/images/pwa-icon-192.png");
        icon192.put("sizes", "192x192");
        icon192.put("type", "image/png");
        icon192.put("purpose", "any");
        icons.add(icon192);

        Map<String, String> icon512 = new LinkedHashMap<>();
        icon512.put("src", "/images/pwa-icon-512.png");
        icon512.put("sizes", "512x512");
        icon512.put("type", "image/png");
        icon512.put("purpose", "any");
        icons.add(icon512);

        Map<String, String> iconMask192 = new LinkedHashMap<>();
        iconMask192.put("src", "/images/pwa-icon-maskable-192.png");
        iconMask192.put("sizes", "192x192");
        iconMask192.put("type", "image/png");
        iconMask192.put("purpose", "maskable");
        icons.add(iconMask192);

        Map<String, String> iconMask512 = new LinkedHashMap<>();
        iconMask512.put("src", "/images/pwa-icon-maskable-512.png");
        iconMask512.put("sizes", "512x512");
        iconMask512.put("type", "image/png");
        iconMask512.put("purpose", "maskable");
        icons.add(iconMask512);

        Map<String, String> iconSvg = new LinkedHashMap<>();
        iconSvg.put("src", "/images/nexipro-icon.svg");
        iconSvg.put("sizes", "any");
        iconSvg.put("type", "image/svg+xml");
        iconSvg.put("purpose", "any");
        icons.add(iconSvg);

        return icons;
    }
}
