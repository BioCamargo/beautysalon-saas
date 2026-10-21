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
        if ((slug == null || slug.isBlank())
                && RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes sra) {
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
                throw new BadCredentialsException("Empresa não encontrada para o identificador '" + slug + "'.");
            }

            Empresa empresa = empresaOpt.get();
            if (!empresa.isAtivo()) {
                throw new DisabledException("Este estabelecimento encontra-se desativado.");
            }

            String inputTrimmed = loginInput.trim();
            // Busca pelo username ou pelo email dentro desta empresa (case-insensitive com fallback)
            userToAuthenticate = userRepository.findByUsernameIgnoreCaseAndEmpresaId(inputTrimmed, empresa.getId())
                    .or(() -> userRepository.findByEmailIgnoreCaseAndEmpresaId(inputTrimmed, empresa.getId()))
                    .orElseThrow(() -> new BadCredentialsException(
                            "Usuário ou e-mail não encontrado nesta empresa (" + empresa.getNome() + ")."));

            if (!passwordEncoder.matches(rawPassword, userToAuthenticate.getPassword())) {
                throw new BadCredentialsException("Senha incorreta.");
            }
        } else {
            // CENÁRIO 2: Slug NÃO foi informado (login genérico /login)
            String inputClean = loginInput.trim();
            boolean isEmail = inputClean.contains("@");

            List<User> rawCandidates = new ArrayList<>();
            if (isEmail) {
                rawCandidates.addAll(userRepository.findAllByEmailIgnoreCase(inputClean));
                if (rawCandidates.isEmpty()) {
                    rawCandidates.addAll(userRepository.findAllByUsernameIgnoreCase(inputClean));
                }
                if (rawCandidates.isEmpty()) {
                    throw new BadCredentialsException("E-mail não encontrado.");
                }
            } else {
                rawCandidates.addAll(userRepository.findAllByUsernameIgnoreCase(inputClean));
                if (rawCandidates.isEmpty()) {
                    rawCandidates.addAll(userRepository.findAllByEmailIgnoreCase(inputClean));
                }
                if (rawCandidates.isEmpty()) {
                    throw new BadCredentialsException("Usuário ou e-mail não encontrado.");
                }
            }

            // Remove duplicados
            List<User> candidates = rawCandidates.stream().distinct().toList();

            // Filtra os usuários cuja senha confere
            List<User> matchingPassword = candidates.stream()
                    .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                    .toList();

            if (matchingPassword.isEmpty()) {
                throw new BadCredentialsException("Senha incorreta.");
            }

            // Filtra contas e empresas ativas
            List<User> activeCandidates = matchingPassword.stream()
                    .filter(User::isAtivo)
                    .filter(u -> u.getEmpresa() == null || u.getEmpresa().isAtivo())
                    .toList();

            if (activeCandidates.isEmpty()) {
                throw new DisabledException("Sua conta de usuário ou estabelecimento está desativado.");
            }

            if (activeCandidates.size() == 1) {
                userToAuthenticate = activeCandidates.get(0);
            } else {
                // Existe mais de um registro compatível
                if (isEmail) {
                    // Verifica se há mais de um colaborador compartilhando o mesmo e-mail na mesma empresa
                    long totalSaloesDistintos = activeCandidates.stream()
                            .map(u -> u.getEmpresa() != null ? u.getEmpresa().getId() : null)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .count();

                    if (totalSaloesDistintos < activeCandidates.size()) {
                        throw new BadCredentialsException(
                                "Este e-mail está associado a múltiplos colaboradores no mesmo estabelecimento. " +
                                        "Por favor, acesse utilizando seu Nome de Usuário (username).");
                    }
                }

                // Usuário legítimo com múltiplos salões autorizados (mesmo usuário/senha em estabelecimentos distintos)
                List<com.beautysalon.DTO.TenantCandidateDTO> candidatesDTO = activeCandidates.stream()
                        .map(u -> com.beautysalon.DTO.TenantCandidateDTO.builder()
                                .userId(u.getId())
                                .empresaId(u.getEmpresa() != null ? u.getEmpresa().getId() : null)
                                .empresaNome(u.getEmpresa() != null ? u.getEmpresa().getNome() : "Estabelecimento")
                                .empresaSlug(u.getEmpresa() != null ? u.getEmpresa().getSlug() : "")
                                .empresaLogo(u.getEmpresa() != null ? u.getEmpresa().getLogoUrl() : null)
                                .role(u.getTenantRole() != null ? u.getTenantRole().name() : "COLABORADOR")
                                .username(u.getUsername())
                                .email(u.getEmail())
                                .build())
                        .toList();

                throw new com.beautysalon.exception.MultipleTenantsAuthenticationException(
                        "Múltiplos estabelecimentos encontrados para este usuário.",
                        candidatesDTO,
                        loginInput);
            }
        }

        if (!userToAuthenticate.isAtivo()) {
            throw new DisabledException("Sua conta de usuário está desativada.");
        }

        TenantUserDetails tenantUserDetails = new TenantUserDetails(userToAuthenticate);

        return new UsernamePasswordAuthenticationToken(
                tenantUserDetails,
                null,
                tenantUserDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
