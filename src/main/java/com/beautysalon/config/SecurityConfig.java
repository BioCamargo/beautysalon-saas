package com.beautysalon.config;

import com.beautysalon.Implementacao.UserDetailsServiceImpl;
import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.beautysalon.config.jwt.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Optional;

@Configuration
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final UserRepository userRepository;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService,
                          UserRepository userRepository,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);

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
                .authenticationProvider(authProvider())
                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS ESTÁTICOS E PÁGINAS PÚBLICAS
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/register-empresa",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/uploads/**",
                                "/favicon.ico",
                                "/error",
                                "/api/**",
                                "/*/agendar/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 2. REGRAS POR PAPEL DO TENANT (OWNER / ADMIN)
                        .requestMatchers(
                                "/*/usuarios/**",
                                "/*/relatorios/**"
                        ).hasAnyRole("OWNER", "ADMIN")

                        // 3. PÁGINAS PROTEGIDAS DO TENANT
                        .requestMatchers(
                                "/*",
                                "/*/home/**",
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
                        .successHandler(authenticationSuccessHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .ignoringRequestMatchers("/api/**", "/h2-console/**")
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                );

        return http.build();
    }
}
