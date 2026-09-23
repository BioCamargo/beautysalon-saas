package com.beautysalon.tenant;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

/**
 * Intercepta todas as requisições que contêm /{slug}/ na URL.
 *
 * Responsabilidades:
 * 1. Extrai o slug da empresa da URL
 * 2. Valida se a empresa existe e está ativa
 * 3. Verifica se o usuário logado pertence à empresa do slug
 * 4. Preenche o TenantContext para uso nos Services
 * 5. Adiciona o slug e nome da empresa ao ModelAndView (para os templates)
 */
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;

    public TenantInterceptor(EmpresaRepository empresaRepository, UserRepository userRepository) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Extrai o slug: /meu-salao/clientes → "meu-salao"
        String[] parts = path.split("/");
        if (parts.length < 2) {
            return true;
        }

        String slug = parts[1];

        // Ignora paths que não são slugs de empresa (recursos estáticos, auth, etc.)
        if (isPublicPath(slug)) {
            return true;
        }

        Optional<Empresa> empresaOpt = empresaRepository.findBySlug(slug);
        if (empresaOpt.isEmpty()) {
            log.warn("Tentativa de acesso a empresa inexistente: {}", slug);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Empresa não encontrada: " + slug);
            return false;
        }

        Empresa empresa = empresaOpt.get();
        if (!empresa.isAtivo()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Empresa inativa.");
            return false;
        }

        // Valida que o usuário logado pertence a esta empresa
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<User> userOpt = userRepository.findByUsernameAndEmpresaId(username, empresa.getId());
            if (userOpt.isEmpty()) {
                log.warn("Usuário '{}' tentou acessar empresa '{}' sem permissão.", username, slug);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado a esta empresa.");
                return false;
            }
        }

        // Preenche o contexto do tenant
        TenantContext.setEmpresaId(empresa.getId());
        TenantContext.setSlug(slug);
        TenantContext.setNome(empresa.getNome());

        log.debug("Tenant definido: {} (id={}, nome={})", slug, empresa.getId(), empresa.getNome());
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        // Injeta o slug e nome da empresa nos templates automaticamente
        if (modelAndView != null && TenantContext.getSlug() != null) {
            modelAndView.addObject("empresaSlug", TenantContext.getSlug());
            modelAndView.addObject("empresaNome", TenantContext.getNome());
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // CRÍTICO: limpa o ThreadLocal após cada request
        TenantContext.clear();
    }

    /**
     * Paths que não representam slugs de empresa e devem ser ignorados pelo interceptor.
     */
    private boolean isPublicPath(String segment) {
        return switch (segment) {
            case "login", "logout", "register", "register-empresa",
                 "css", "js", "images", "uploads", "favicon.ico",
                 "api", "actuator", "error", "" -> true;
            default -> false;
        };
    }
}
