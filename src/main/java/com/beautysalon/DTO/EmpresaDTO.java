package com.beautysalon.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para exibição de dados da empresa no frontend.
 */
@Data
@NoArgsConstructor
public class EmpresaDTO {
    private Long id;
    private String nome;
    private String slug;
    private String cnpj;
    private String telefone;
    private String email;
    private String logoUrl;
    private com.beautysalon.model.SegmentoEmpresa segmento;
    private boolean ativo;
}
