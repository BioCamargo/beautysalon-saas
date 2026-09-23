package com.beautysalon.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ServicoDTO
{
    private Long id;

    @NotBlank(message = "Service name required")
    private String nome;

    @NotBlank(message = "The description is not mandatory ")
    private String descricao;

    @NotNull(message = "Price required")
    @Min(value = 0, message = "Price must be positive")
    private BigDecimal preco;

    private String imagem;

    private transient MultipartFile imagemFile;



    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }


    public MultipartFile getImagemFile() {return imagemFile;}
    public void setImagemFile(MultipartFile imagemFile) {this.imagemFile = imagemFile;}

}


