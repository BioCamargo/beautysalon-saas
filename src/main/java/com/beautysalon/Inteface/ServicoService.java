package com.beautysalon.Inteface;

import com.beautysalon.DTO.ServicoDTO;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public interface ServicoService
{
    ServicoDTO salvar(ServicoDTO dto);
    List<ServicoDTO> listarTodos();
    ServicoDTO buscarPorId(Long id);
    void excluir(Long id);

    ServicoDTO atualizar(Long id, ServicoDTO dto);

    //  void atualizar(Long id, ServicoDTO dto);
}
