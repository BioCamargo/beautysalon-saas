package com.beautysalon.Inteface;

import com.beautysalon.DTO.EmpresaDTO;
import com.beautysalon.DTO.RegisterEmpresaDTO;

import java.util.List;

public interface EmpresaService {

    /**
     * Registra uma nova empresa e seu primeiro usuário (OWNER).
     * Retorna o slug gerado para redirecionamento.
     */
    String registrarNovaEmpresa(RegisterEmpresaDTO dto);

    EmpresaDTO buscarPorSlug(String slug);

    EmpresaDTO buscarPorId(Long id);

    List<EmpresaDTO> listarTodas();

    EmpresaDTO atualizar(Long id, EmpresaDTO dto);
}
