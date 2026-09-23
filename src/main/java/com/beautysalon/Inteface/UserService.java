package com.beautysalon.Inteface;

import com.beautysalon.DTO.UserDTO;

import java.io.IOException;
import java.util.List;

public interface UserService {

    List<UserDTO> listarTodosDaEmpresa(Long empresaId);

    UserDTO buscarPorId(Long id, Long empresaId);

    UserDTO salvar(UserDTO dto) throws IOException;

    UserDTO atualizar(Long id, UserDTO dto, Long empresaId);

    void deletar(Long id, Long empresaId);

    boolean existsByUsername(String username, Long empresaId);

    boolean existsByEmail(String email, Long empresaId);

    /**
     * Registra um novo usuário em uma empresa (usado no convite interno).
     */
    void registerUser(UserDTO userDTO, Long empresaId);
}
