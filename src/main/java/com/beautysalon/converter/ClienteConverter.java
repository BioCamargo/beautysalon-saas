package com.beautysalon.converter;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.model.Cliente;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ClienteConverter  implements Converter<Cliente, ClienteDTO> {
    @Override
    public ClienteDTO convert(Cliente source)
    {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(source.getId());
        dto.setNome(source.getNome());
        dto.setEmail(source.getEmail());
        dto.setTelefone(source.getTelefone());
        dto.setDataNascimento(source.getDataNascimento());
        return dto;
    }
}
