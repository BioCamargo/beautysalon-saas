package com.beautysalon.exception;

import com.beautysalon.DTO.TenantCandidateDTO;
import org.springframework.security.core.AuthenticationException;

import java.util.List;

/**
 * Exceção lançada quando as credenciais do usuário estão corretas,
 * porém o usuário possui acesso a múltiplos estabelecimentos e precisa
 * selecionar qual salão deseja gerenciar.
 */
public class MultipleTenantsAuthenticationException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    private final List<TenantCandidateDTO> candidates;
    private final String usernameOrEmail;

    public MultipleTenantsAuthenticationException(String msg, List<TenantCandidateDTO> candidates, String usernameOrEmail) {
        super(msg);
        this.candidates = candidates;
        this.usernameOrEmail = usernameOrEmail;
    }

    public List<TenantCandidateDTO> getCandidates() {
        return candidates;
    }

    public String getUsernameOrEmail() {
        return usernameOrEmail;
    }
}
