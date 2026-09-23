package com.beautysalon.Inteface;

import com.beautysalon.DTO.ClienteDTO;

import java.util.List;

public interface ClienteService {
    List<ClienteDTO> listarTodos();
    ClienteDTO buscarPorId(Long id);
    ClienteDTO salvar(ClienteDTO clienteDTO);
    void deletar(Long id);
    ClienteDTO atualizar(Long id, ClienteDTO dto);
    List<ClienteDTO> buscarPorNomeParcial(String nome);

}
