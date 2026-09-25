package com.beautysalon.DTO.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthRestDTO {

    @Schema(description = "Requisição de login para obtenção de Token JWT")
    public record LoginRequest(
            @NotBlank(message = "O nome de usuário é obrigatório.")
            @Schema(example = "keilla.silva")
            String username,

            @NotBlank(message = "A senha é obrigatória.")
            @Schema(example = "senha123")
            String password
    ) {}

    @Schema(description = "Resposta de autenticação com Token JWT e dados do usuário")
    public record LoginResponse(
            @Schema(example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String token,

            @Schema(example = "Bearer")
            String tokenType,

            @Schema(example = "keilla.silva")
            String username,

            @Schema(example = "ROLE_OWNER")
            String role,

            @Schema(example = "1")
            Long empresaId,

            @Schema(example = "lumora")
            String empresaSlug,

            @Schema(example = "Lumora Studio & Estética")
            String empresaNome
    ) {}
}
