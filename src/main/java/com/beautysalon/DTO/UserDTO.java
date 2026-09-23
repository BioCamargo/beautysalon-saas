package com.beautysalon.DTO;

import com.beautysalon.model.TenantRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO de usuário. Sempre vinculado a uma empresa (tenant).
 * O campo tenantRole substitui os antigos roleIds/roles.
 */
@Data
@NoArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "Nome é obrigatório")
    private String nome;

    @NotBlank(message = "Username é obrigatório")
    private String username;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @Size(min = 6, message = "Mínimo de 6 caracteres")
    private String password;

    private TenantRole tenantRole;

    private MultipartFile imagem;
    private String imagemUrl;

    private boolean ativo;

    private java.math.BigDecimal percentualComissao;
    private String especialidade;
    private String telefone;
    private String corAgenda;

    // ID e nome da empresa (para exibição)
    private Long empresaId;
    private String empresaNome;
}
