package com.beautysalon.config;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Provedor de autenticação customizado Multi-Tenant para o Spring Security.
 * Suporta o mesmo nome de usuário/e-mail coexistindo em empresas diferentes,
 * roteando com precisão via slug da empresa ou login único.
 */
@Slf4j
@Component
public class TenantAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    public TenantAuthenticationProvider(UserRepository userRepository,
                                      EmpresaRepository empresaRepository,
                                      @org.springframework.context.annotation.Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String loginInput = authentication.getName();
        String rawPassword = authentication.getCredentials() != null ? authentication.getCredentials().toString() : "";

        if (loginInput == null || loginInput.isBlank()) {
            throw new BadCredentialsException("Informe o usuário ou e-mail.");
        }

        // Tenta obter o slug do TenantAuthenticationDetails ou do request
        String slug = null;
        if (authentication.getDetails() instanceof TenantAuthenticationDetails details) {
            slug = details.getSlug();
        }
        if ((slug == null || slug.isBlank()) && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();
            String p = req.getParameter("slug");
            if (p != null && !p.isBlank()) {
                slug = p.trim().toLowerCase();
            }
        }

        User userToAuthenticate = null;

        // CENÁRIO 1: Slug foi informado (seja por /{slug}/login ou pelo formulário)
        if (slug != null && !slug.isBlank()) {
            Optional<Empresa> empresaOpt = empresaRepository.findBySlug(slug);
            if (empresaOpt.isEmpty()) {
                throw new BadCredentialsException("Salão não encontrado para o identificador '" + slug + "'.");
            }

            Empresa empresa = empresaOpt.get();
            if (!empresa.isAtivo()) {
                throw new DisabledException("Este estabelecimento encontra-se desativado.");
            }

            // Busca pelo username ou pelo email dentro desta empresa
            userToAuthenticate = userRepository.findByUsernameAndEmpresaId(loginInput, empresa.getId())
                    .or(() -> userRepository.findByEmailAndEmpresaId(loginInput, empresa.getId()))
                    .orElseThrow(() -> new BadCredentialsException("Usuário ou e-mail não encontrado neste salão (" + empresa.getNome() + ")."));

            if (!passwordEncoder.matches(rawPassword, userToAuthenticate.getPassword())) {
                throw new BadCredentialsException("Senha incorreta.");
            }
        } else {
            // CENÁRIO 2: Slug NÃO foi informado (login genérico /login)
            List<User> candidates = new ArrayList<>(userRepository.findAllByUsername(loginInput));
            if (candidates.isEmpty()) {
                candidates.addAll(userRepository.findAllByEmail(loginInput));
            }

            if (candidates.isEmpty()) {
                throw new BadCredentialsException("Usuário ou e-mail não encontrado.");
            }

            // Filtra os usuários cuja senha confere
            List<User> matchingPassword = candidates.stream()
                    .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                    .toList();

            if (matchingPassword.isEmpty()) {
                throw new BadCredentialsException("Senha incorreta.");
            }

            if (matchingPassword.size() == 1) {
                userToAuthenticate = matchingPassword.get(0);
            } else {
                // Existe mais de um salão com exatamente o mesmo usuário e senha
                throw new BadCredentialsException(
                        "Você possui acesso a múltiplos estabelecimentos com este usuário. " +
                        "Por favor, acesse pelo link exclusivo do seu salão (ex: /{slug}/login) ou informe o identificador do salão."
                );
            }
        }

        if (!userToAuthenticate.isAtivo()) {
            throw new DisabledException("Sua conta de usuário está desativada.");
        }

        TenantUserDetails tenantUserDetails = new TenantUserDetails(userToAuthenticate);

        return new UsernamePasswordAuthenticationToken(
                tenantUserDetails,
                null,
                tenantUserDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
