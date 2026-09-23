package com.beautysalon.Implementacao;

import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Carrega o usuário pelo username para autenticação do Spring Security.
 *
 * NOTA: Em um SaaS multi-tenant com login por slug, o mesmo username pode existir
 * em empresas diferentes. O login acontece via formulário que envia também o slug
 * da empresa. O CustomAuthenticationFilter captura o slug e usa o EmpresaRepository
 * para buscar o tenant correto antes de invocar este service.
 *
 * Para compatibilidade com o fluxo padrão do Spring Security, este método
 * usa findByUsername sem filtro de empresa — a validação de tenant ocorre no
 * TenantInterceptor após o login.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repo;

    public UserDetailsServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        // Converte o TenantRole para authority do Spring Security
        // Ex: TenantRole.ADMIN → "ROLE_ADMIN"
        String authority = "ROLE_" + user.getTenantRole().name();
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isAtivo(),     // enabled
                true,               // accountNonExpired
                true,               // credentialsNonExpired
                true,               // accountNonLocked
                authorities
        );
    }
}