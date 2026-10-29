package com.beautysalon.config;

import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.beautysalon.config.jwt.JwtAuthenticationFilter;
import com.beautysalon.tenant.TenantUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final TenantAuthenticationProvider tenantAuthenticationProvider;
    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final com.beautysalon.service.AuditoriaService auditoriaService;

    public SecurityConfig(@org.springframework.context.annotation.Lazy TenantAuthenticationProvider tenantAuthenticationProvider,
                          UserRepository userRepository,
                          JwtAuthenticationFilter jwtAuthenticationFilter,
                          @org.springframework.context.annotation.Lazy com.beautysalon.service.AuditoriaService auditoriaService) {
        this.tenantAuthenticationProvider = tenantAuthenticationProvider;
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.auditoriaService = auditoriaService;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            Object principal = authentication.getPrincipal();

            try {
                auditoriaService.registrar("LOGIN_SUCESSO", "usuario", username, "Autenticação realizada com sucesso");
            } catch (Exception ignored) {}

            if (principal instanceof TenantUserDetails tenantUser && tenantUser.getEmpresaSlug() != null) {
                response.sendRedirect("/" + tenantUser.getEmpresaSlug() + "/home");
                return;
            }

            Optional<User> userOpt = userRepository.findFirstByUsername(username);

            if (userOpt.isPresent() && userOpt.get().getEmpresa() != null) {
                String slug = userOpt.get().getEmpresa().getSlug();
                response.sendRedirect("/" + slug + "/home");
            } else {
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(tenantAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS ESTÁTICOS E PÁGINAS PÚBLICAS
                        .requestMatchers(
                                "/",
                                "/login",
                                "/*/login",
                                "/select-tenant",
                                "/register",
                                "/register-empresa",
                                "/termos",
                                "/privacidade",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/favicon.ico",
                                "/robots.txt",
                                "/sitemap.xml",
                                "/manifest.json",
                                "/*/manifest.json",
                                "/service-worker.js",
                                "/error",
                                "/api/auth/**",
                                "/*/agendar",
                                "/*/agendar/**",
                                "/agendamento/**",
                                "/p/**",
                                "/api/v1/*/agendar/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/v1/webhooks/**",
                                "/api/webhooks/**",
                                "/api/webhc",
                                "/api/webhc/**"
                        ).permitAll()

                        // 2. REGRAS POR PAPEL DO TENANT (OWNER / ADMIN)
                        .requestMatchers(
                                "/*/usuarios/**",
                                "/*/relatorios/**",
                                "/*/configuracoes/**",
                                "/*/assinatura",
                                "/*/assinatura/**"
                        ).hasAnyRole("OWNER", "ADMIN")

                        // 3. PÁGINAS PROTEGIDAS DO TENANT
                        .requestMatchers(
                                "/*",
                                "/*/home/**",
                                "/*/meus-ganhos/**",
                                "/*/clientes/**",
                                "/*/agendamentos/**",
                                "/*/servicos/**",
                                "/*/financeiro/**",
                                "/*/estoque/**",
                                "/*/fidelizacao/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .authenticationDetailsSource(TenantAuthenticationDetails::new)
                        .successHandler(authenticationSuccessHandler())
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof com.beautysalon.exception.MultipleTenantsAuthenticationException mte) {
                                request.getSession().setAttribute("PENDING_TENANT_CANDIDATES", mte.getCandidates());
                                request.getSession().setAttribute("PENDING_TENANT_USERNAME", mte.getUsernameOrEmail());
                                response.sendRedirect("/select-tenant");
                                return;
                            }
                            String slug = request.getParameter("slug");
                            request.getSession().setAttribute(org.springframework.security.web.WebAttributes.AUTHENTICATION_EXCEPTION, exception);
                            if (slug != null && !slug.isBlank()) {
                                response.sendRedirect("/" + slug.trim().toLowerCase() + "/login?error");
                            } else {
                                response.sendRedirect("/login?error");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            if (uri != null && uri.length() > 1) {
                                String clean = uri.startsWith("/") ? uri.substring(1) : uri;
                                String[] parts = clean.split("/");
                                if (parts.length > 0) {
                                    String candidateSlug = parts[0].trim().toLowerCase();
                                    if (!isSystemRoute(candidateSlug)) {
                                        response.sendRedirect("/" + candidateSlug + "/login");
                                        return;
                                    }
                                }
                            }

                            // Verifica se há cookie de empresa registrada/acessada anteriormente
                            jakarta.servlet.http.Cookie[] cookies = request.getCookies();
                            if (cookies != null) {
                                for (jakarta.servlet.http.Cookie c : cookies) {
                                    if ("nexipro_last_slug".equals(c.getName()) && c.getValue() != null && !c.getValue().isBlank()) {
                                        try {
                                            String slugCookie = java.net.URLDecoder.decode(c.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                                            if (!slugCookie.isBlank() && !isSystemRoute(slugCookie)) {
                                                response.sendRedirect("/" + slugCookie.trim().toLowerCase() + "/login");
                                                return;
                                            }
                                        } catch (Exception ignored) {}
                                    }
                                }
                            }

                            response.sendRedirect("/login");
                        })
                )

                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**", "/h2-console/**", "/*/agendar", "/*/agendar/**")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        return http.build();
    }

    private static final java.util.Set<String> SYSTEM_ROUTES = java.util.Set.of(
            "login", "register", "register-empresa", "select-tenant",
            "termos", "privacidade", "css", "js", "images", "uploads",
            "favicon.ico", "manifest.json", "service-worker.js", "error",
            "api", "v3", "swagger-ui", "swagger-ui.html"
    );

    private boolean isSystemRoute(String route) {
        return SYSTEM_ROUTES.contains(route.toLowerCase());
    }
}
