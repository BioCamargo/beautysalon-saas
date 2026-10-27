package com.beautysalon.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de Observabilidade SRE Tenant-Aware (DEV-05).
 * Injeta no MDC (Mapped Diagnostic Context) do SLF4J os identificadores do tenant, empresa e usuário
 * para correlação automática de logs em microsserviços e auditoria Cloud/Prometheus.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 50)
public class TenantLoggingFilter extends OncePerRequestFilter {

    public static final String MDC_KEY_TENANT = "tenant";
    public static final String MDC_KEY_EMPRESA_ID = "empresa_id";
    public static final String MDC_KEY_USUARIO = "usuario";
    public static final String MDC_KEY_TRACE_ID = "trace_id";
    public static final String MDC_KEY_CLIENT_IP = "client_ip";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // Gera ou reaproveita ID de rastreamento distribuído (Trace ID)
            String traceId = request.getHeader("X-Correlation-Id");
            if (traceId == null || traceId.trim().isEmpty()) {
                traceId = request.getHeader("X-Request-Id");
            }
            if (traceId == null || traceId.trim().isEmpty()) {
                traceId = java.util.UUID.randomUUID().toString().substring(0, 8);
            }
            MDC.put(MDC_KEY_TRACE_ID, traceId);
            response.setHeader("X-Correlation-Id", traceId);

            // Extrai IP real com proteção contra proxies reversos
            String clientIp = extrairClientIp(request);
            MDC.put(MDC_KEY_CLIENT_IP, clientIp);

            // Inicializa valores default seguros
            MDC.put(MDC_KEY_TENANT, "global");
            MDC.put(MDC_KEY_EMPRESA_ID, "-");

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                MDC.put(MDC_KEY_USUARIO, auth.getName());
            } else {
                MDC.put(MDC_KEY_USUARIO, "anon");
            }

            filterChain.doFilter(request, response);
        } finally {
            // Limpeza estrita e obrigatória do MDC para não vazar contexto no pool de threads do Tomcat
            MDC.clear();
        }
    }

    private String extrairClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            xfHeader = request.getHeader("Proxy-Client-IP");
        }
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            xfHeader = request.getHeader("WL-Proxy-Client-IP");
        }
        if (xfHeader == null || xfHeader.isEmpty() || "unknown".equalsIgnoreCase(xfHeader)) {
            return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
        }
        return xfHeader.split(",")[0].trim();
    }
}
