package com.beautysalon.API;

import com.beautysalon.DTO.ServicoDTO;
import com.beautysalon.Inteface.ServicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoRestController {

    @Autowired
    private ServicoService servicoService;

    @GetMapping
    public List<ServicoDTO> listarTodos() {
        return servicoService.listarTodos();
    }
}
