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
    private com.beautysalon.model.PlanoSaaS plano;
    private com.beautysalon.model.StatusAssinatura statusAssinatura;
    private java.time.LocalDateTime dataExpiracaoTrial;
    private long diasRestantesTrial;
    private boolean assinaturaValida;
    private boolean ativo;
}
