package com.beautysalon.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para o formulário de registro de nova empresa + primeiro usuário (OWNER).
 * Usado em POST /register-empresa
 */
@Data
@NoArgsConstructor
public class RegisterEmpresaDTO {

    // --- Dados da Empresa ---

    @NotBlank(message = "O nome da empresa é obrigatório")
    private String nomeEmpresa;

    @NotBlank(message = "O email da empresa é obrigatório")
    @Email(message = "Email inválido")
    private String emailEmpresa;

    private String telefoneEmpresa;

    private String cnpj;

    // --- Dados do Primeiro Usuário (Owner) ---

    @NotBlank(message = "O nome do responsável é obrigatório")
    private String nomeUsuario;

    @NotBlank(message = "O username é obrigatório")
    @Size(min = 3, max = 50, message = "Username deve ter entre 3 e 50 caracteres")
    private String username;

    @NotBlank(message = "O email do usuário é obrigatório")
    @Email(message = "Email inválido")
    private String emailUsuario;

    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 6, message = "A senha deve ter pelo menos 6 caracteres")
    private String password;

    @NotBlank(message = "Confirme a senha")
    private String confirmPassword;
}
