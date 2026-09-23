package com.beautysalon.dto.rest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ClienteRestDTO {

    public record Request(
            @NotBlank(message = "O nome do cliente é obrigatório")
            @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
            String nome,

            @Size(max = 20, message = "O telefone não pode ter mais de 20 caracteres")
            String telefone,

            @Email(message = "E-mail inválido")
            String email,

            LocalDate dataNascimento
    ) {}

    public record Response(
            Long id,
            String nome,
            String telefone,
            String email,
            LocalDate dataNascimento,
            Long empresaId
    ) {}
}
