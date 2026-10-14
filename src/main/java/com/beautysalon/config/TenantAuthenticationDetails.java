package com.beautysalon.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

import java.io.Serializable;

/**
 * Captura dados complementares da requisição de autenticação, como o slug da empresa (tenant).
 */
@Getter
public class TenantAuthenticationDetails implements Serializable {

    private final String slug;

    public TenantAuthenticationDetails(HttpServletRequest request) {
        String slugParam = request.getParameter("slug");
        if (slugParam == null || slugParam.isBlank()) {
            // Tenta obter do header Referer caso o form tenha sido submetido de /{slug}/login
            String referer = request.getHeader("Referer");
            if (referer != null) {
                try {
                    java.net.URI uri = java.net.URI.create(referer);
                    String path = uri.getPath();
                    if (path != null) {
                        String[] parts = path.split("/");
                        if (parts.length >= 3 && "login".equalsIgnoreCase(parts[2])) {
                            slugParam = parts[1];
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }
        this.slug = (slugParam != null && !slugParam.isBlank()) ? slugParam.trim().toLowerCase() : null;
    }
}
