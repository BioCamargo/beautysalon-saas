package com.beautysalon.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ClienteDTO
{
    private Long id;

    @NotBlank(message = "Campo Obrigatorio")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres.")
    private String nome;

    @NotBlank(message = "Campo Obrigatorio")
    @Email(message = "O e-mail deve ser válido.")    
    private String email;

    private String telefone; 
    private LocalDate dataNascimento; 
    private String alergias;
    private String tipoCabeloPele;
    private String historicoQuimico;
    private String observacoesTecnicas;

    public ClienteDTO (Long id, String nome, String email, String telefone, LocalDate dataNascimento)
    {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataNascimento = dataNascimento;
    }

    public ClienteDTO() {
    }
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public String getNome() {return nome;}
    public void setNome(String nome) {this.nome = nome;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getTelefone() {return telefone;}
    public void setTelefone(String telefone) {this.telefone = telefone;}

    public LocalDate getDataNascimento() {return dataNascimento;}
    public void setDataNascimento(LocalDate dataNascimento) {this.dataNascimento = dataNascimento;}

    public String getAlergias() { return alergias; }
    public void setAlergias(String alergias) { this.alergias = alergias; }

    public String getTipoCabeloPele() { return tipoCabeloPele; }
    public void setTipoCabeloPele(String tipoCabeloPele) { this.tipoCabeloPele = tipoCabeloPele; }

    public String getHistoricoQuimico() { return historicoQuimico; }
    public void setHistoricoQuimico(String historicoQuimico) { this.historicoQuimico = historicoQuimico; }

    public String getObservacoesTecnicas() { return observacoesTecnicas; }
    public void setObservacoesTecnicas(String observacoesTecnicas) { this.observacoesTecnicas = observacoesTecnicas; }

}
