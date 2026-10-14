package com.beautysalon.Implementacao;

import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
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
        List<User> users = repo.findAllByUsername(username);
        if (users.isEmpty()) {
            users = repo.findAllByEmail(username);
        }
        if (users.isEmpty()) {
            throw new UsernameNotFoundException("Usuário não encontrado: " + username);
        }

        User user = users.get(0);
        return new com.beautysalon.tenant.TenantUserDetails(user);
    }
}