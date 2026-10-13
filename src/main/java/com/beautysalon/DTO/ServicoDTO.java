package com.beautysalon.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

public class ServicoDTO
{
    private Long id;

    @NotBlank(message = "O nome do serviço é obrigatório")
    private String nome;

    private String descricao;

    @NotNull(message = "O preço é obrigatório")
    @Min(value = 0, message = "O preço deve ser positivo")
    private BigDecimal preco;

    private String imagem;

    private Integer duracaoMinutos = 30;

    private Integer diasCicloRetorno = 30;

    private BigDecimal percentualComissao;

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

    public Integer getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public void setDuracaoMinutos(Integer duracaoMinutos) {
        this.duracaoMinutos = duracaoMinutos;
    }

    public Integer getDiasCicloRetorno() {
        return diasCicloRetorno;
    }

    public void setDiasCicloRetorno(Integer diasCicloRetorno) {
        this.diasCicloRetorno = diasCicloRetorno;
    }

    public BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(BigDecimal percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public MultipartFile getImagemFile() {
        return imagemFile;
    }

    public void setImagemFile(MultipartFile imagemFile) {
        this.imagemFile = imagemFile;
    }

}


