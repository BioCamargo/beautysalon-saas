package com.beautysalon.tenant;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());

        // Extrai o slug: /meu-salao/clientes → "meu-salao"
        // Ou rotas REST: /api/v1/meu-salao/clientes → "meu-salao"
        String[] parts = path.split("/");
        if (parts.length < 2) {
            return true;
        }

        String slug = parts[1];
        if ("api".equals(slug)) {
            if (parts.length >= 4 && "v1".equals(parts[2]) && !isPublicPath(parts[3])) {
                slug = parts[3];
            } else {
                return true;
            }
        } else if (isPublicPath(slug)) {
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

        // Auto-promoção para Contrato Vitalício se pertencer à lista de exceções vitalícias (ex: Studio Helen Coltro)
        if (!empresa.isVitalicio() && com.beautysalon.Implementacao.EmpresaServiceImpl.isEmpresaVitalicia(
                empresa.getNome(), empresa.getSlug(), empresa.getCnpj(), empresa.getEmail(), null, null)) {
            empresa.setPlano(com.beautysalon.model.PlanoSaaS.VITALICIO);
            empresa.setStatusAssinatura(com.beautysalon.model.StatusAssinatura.VITALICIO);
            empresa.setDataExpiracaoTrial(null);
            empresaRepository.save(empresa);
        }

        // Preenche o contexto do tenant
        TenantContext.setEmpresaId(empresa.getId());
        TenantContext.setSlug(slug);
        TenantContext.setNome(empresa.getNome());
        TenantContext.setSegmento(empresa.getSegmento() != null ? empresa.getSegmento() : com.beautysalon.model.SegmentoEmpresa.SALAO_BELEZA);
        TenantContext.setPlano(empresa.getPlano() != null ? empresa.getPlano() : com.beautysalon.model.PlanoSaaS.STUDIO);
        TenantContext.setStatusAssinatura(empresa.getStatusAssinatura() != null ? empresa.getStatusAssinatura() : com.beautysalon.model.StatusAssinatura.TRIAL);
        TenantContext.setDiasRestantesTrial(empresa.getDiasRestantesTrial());
        TenantContext.setTrialAtivo(empresa.isTrialAtivo());
        TenantContext.setLogoUrl(empresa.getLogoUrl());
        TenantContext.setMetaPixelId(empresa.getMetaPixelId());
        TenantContext.setGoogleAdsId(empresa.getGoogleAdsId());
        TenantContext.setGoogleAnalyticsId(empresa.getGoogleAnalyticsId());
        TenantContext.setLinkFaturaAtual(empresa.getLinkFaturaAtual());
        TenantContext.setInadimplente(empresa.isInadimplente());
        TenantContext.setBloqueioSuaveAtivo(empresa.isBloqueioSuaveAtivo());
        TenantContext.setToleranciaInadimplenteAtiva(empresa.isToleranciaInadimplenteAtiva());
        TenantContext.setDiasRestantesTolerancia(empresa.getDiasRestantesTolerancia());

        // DEV-05: Atualiza MDC com contexto do tenant e usuário resolvidos
        org.slf4j.MDC.put(TenantLoggingFilter.MDC_KEY_TENANT, slug);
        org.slf4j.MDC.put(TenantLoggingFilter.MDC_KEY_EMPRESA_ID, String.valueOf(empresa.getId()));
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            org.slf4j.MDC.put(TenantLoggingFilter.MDC_KEY_USUARIO, auth.getName());
        }

        log.debug("Tenant definido: {} (id={}, nome={}, segmento={}, plano={}, status={})", 
                slug, empresa.getId(), empresa.getNome(), empresa.getSegmento(), empresa.getPlano(), empresa.getStatusAssinatura());
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request,
                           @NonNull HttpServletResponse response,
                           @NonNull Object handler,
                           @Nullable ModelAndView modelAndView) {
        // Injeta os dados da empresa nos templates automaticamente (apenas para renderização de views, não em redirects)
        if (modelAndView != null && TenantContext.getSlug() != null) {
            String viewName = modelAndView.getViewName();
            boolean isRedirect = (viewName != null && viewName.startsWith("redirect:"))
                    || modelAndView.getView() instanceof org.springframework.web.servlet.view.RedirectView;

            if (!isRedirect) {
                modelAndView.addObject("empresaSlug", TenantContext.getSlug());
                modelAndView.addObject("empresaNome", TenantContext.getNome());
                modelAndView.addObject("empresaLogo", TenantContext.getLogoUrl());
                modelAndView.addObject("empresaSegmento", TenantContext.getSegmento());
                modelAndView.addObject("empresaPlano", TenantContext.getPlano());
                modelAndView.addObject("empresaStatusAssinatura", TenantContext.getStatusAssinatura());
                modelAndView.addObject("empresaDiasRestantesTrial", TenantContext.getDiasRestantesTrial());
                modelAndView.addObject("empresaTrialAtivo", TenantContext.isTrialAtivo());
                modelAndView.addObject("empresaMetaPixelId", TenantContext.getMetaPixelId());
                modelAndView.addObject("empresaGoogleAdsId", TenantContext.getGoogleAdsId());
                modelAndView.addObject("empresaGoogleAnalyticsId", TenantContext.getGoogleAnalyticsId());
                modelAndView.addObject("empresaLinkFatura", TenantContext.getLinkFaturaAtual());
                modelAndView.addObject("empresaIsInadimplente", TenantContext.isInadimplente());
                modelAndView.addObject("empresaBloqueioSuaveAtivo", TenantContext.isBloqueioSuaveAtivo());
                modelAndView.addObject("empresaToleranciaInadimplenteAtiva", TenantContext.isToleranciaInadimplenteAtiva());
                modelAndView.addObject("empresaDiasRestantesTolerancia", TenantContext.getDiasRestantesTolerancia());
            }
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler,
                                @Nullable Exception ex) {
        // CRÍTICO: limpa o ThreadLocal e o MDC do tenant após cada request
        org.slf4j.MDC.remove(TenantLoggingFilter.MDC_KEY_TENANT);
        org.slf4j.MDC.remove(TenantLoggingFilter.MDC_KEY_EMPRESA_ID);
        TenantContext.clear();
    }

    /**
     * Paths que não representam slugs de empresa e devem ser ignorados pelo interceptor.
     */
    private boolean isPublicPath(String segment) {
        return switch (segment) {
            case "login", "logout", "register", "register-empresa",
                 "select-tenant", "whatsapp-pair",
                 "termos", "privacidade",
                 "css", "js", "images", "uploads", "favicon.ico",
                 "manifest.json", "service-worker.js", "robots.txt", "sitemap.xml",
                 "swagger-ui", "swagger-ui.html", "v3",
                 "api", "actuator", "error", "p", "agendamento", "" -> true;
            default -> false;
        };
    }
}
