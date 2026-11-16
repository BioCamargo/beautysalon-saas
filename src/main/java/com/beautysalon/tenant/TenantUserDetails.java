package com.beautysalon.tenant;

import com.beautysalon.model.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Representação customizada de UserDetails no contexto Multi-Tenant.
 * Mantém vínculo direto com a Empresa (Tenant) e o Objeto de Usuário.
 */
@Getter
public class TenantUserDetails implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final boolean enabled;
    private final Long empresaId;
    private final String empresaSlug;
    private final String empresaNome;
    private final Collection<? extends GrantedAuthority> authorities;
    private final User user;

    public TenantUserDetails(User user) {
        this.user = user;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.enabled = user.isAtivo();

        if (user.getEmpresa() != null) {
            this.empresaId = user.getEmpresa().getId();
            this.empresaSlug = user.getEmpresa().getSlug();
            this.empresaNome = user.getEmpresa().getNome();
        } else {
            this.empresaId = null;
            this.empresaSlug = null;
            this.empresaNome = null;
        }

        String authority = "ROLE_" + (user.getTenantRole() != null ? user.getTenantRole().name() : "FUNCIONARIO");
        this.authorities = List.of(new SimpleGrantedAuthority(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getNome() {
        return user != null && user.getNome() != null && !user.getNome().isBlank() ? user.getNome() : username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
