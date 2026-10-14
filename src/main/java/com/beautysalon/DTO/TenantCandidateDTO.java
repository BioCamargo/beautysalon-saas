package com.beautysalon.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO que representa um estabelecimento elegível para o usuário
 * selecionar durante o login multi-tenant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantCandidateDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long empresaId;
    private String empresaNome;
    private String empresaSlug;
    private String empresaLogo;
    private String role;
    private String username;
    private String email;
}
