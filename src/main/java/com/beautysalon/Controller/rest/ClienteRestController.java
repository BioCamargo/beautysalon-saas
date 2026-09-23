package com.beautysalon.Controller.rest;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.DTO.rest.ClienteRestDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.tenant.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/{slug}/clientes")
@Tag(name = "Clientes", description = "Endpoints para gerenciamento da base de clientes por Tenant")
public class ClienteRestController {

    private final ClienteService clienteService;
    private final EmpresaRepository empresaRepository;

    public ClienteRestController(ClienteService clienteService, EmpresaRepository empresaRepository) {
        this.clienteService = clienteService;
        this.empresaRepository = empresaRepository;
    }

    private Empresa obterEmpresa(String slug) {
        Empresa emp = empresaRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
        TenantContext.setTenant(emp.getSlug(), emp.getId());
        return emp;
    }

    private ClienteRestDTO.Response toResponse(ClienteDTO dto, Long empresaId) {
        return new ClienteRestDTO.Response(
                dto.getId(),
                dto.getNome(),
                dto.getTelefone(),
                dto.getEmail(),
                dto.getDataNascimento(),
                empresaId
        );
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes da empresa")
    public ResponseEntity<List<ClienteRestDTO.Response>> listar(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<ClienteRestDTO.Response> response = clienteService.listarTodos()
                .stream()
                .map(c -> toResponse(c, empresa.getId()))
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteRestDTO.Response> buscarPorId(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        ClienteDTO dto = clienteService.buscarPorId(id);
        if (dto == null) {
            throw new EntityNotFoundException("Cliente com ID " + id + " não encontrado.");
        }
        return ResponseEntity.ok(toResponse(dto, empresa.getId()));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente")
    public ResponseEntity<ClienteRestDTO.Response> criar(
            @PathVariable String slug,
            @Valid @RequestBody ClienteRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        ClienteDTO novo = new ClienteDTO();
        novo.setNome(request.nome());
        novo.setTelefone(request.telefone());
        novo.setEmail(request.email());
        novo.setDataNascimento(request.dataNascimento());

        ClienteDTO salvo = clienteService.salvar(novo);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo, empresa.getId()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de um cliente existente")
    public ResponseEntity<ClienteRestDTO.Response> atualizar(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody ClienteRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        ClienteDTO dto = new ClienteDTO();
        dto.setId(id);
        dto.setNome(request.nome());
        dto.setTelefone(request.telefone());
        dto.setEmail(request.email());
        dto.setDataNascimento(request.dataNascimento());

        ClienteDTO atualizado = clienteService.atualizar(id, dto);
        return ResponseEntity.ok(toResponse(atualizado, empresa.getId()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um cliente")
    public ResponseEntity<Void> excluir(@PathVariable String slug, @PathVariable Long id) {
        obterEmpresa(slug);
        clienteService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
