package com.beautysalon.controller.rest;

import com.beautysalon.dto.rest.ClienteRestDTO;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.Inteface.EmpresaService;
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
    private final EmpresaService empresaService;

    public ClienteRestController(ClienteService clienteService, EmpresaService empresaService) {
        this.clienteService = clienteService;
        this.empresaService = empresaService;
    }

    private Empresa obterEmpresa(String slug) {
        return empresaService.buscarPorSlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
    }

    private ClienteRestDTO.Response toResponse(Cliente cliente) {
        return new ClienteRestDTO.Response(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getDataNascimento(),
                cliente.getEmpresa() != null ? cliente.getEmpresa().getId() : null
        );
    }

    @GetMapping
    @Operation(summary = "Listar todos os clientes da empresa")
    public ResponseEntity<List<ClienteRestDTO.Response>> listar(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<ClienteRestDTO.Response> response = clienteService.listarPorEmpresa(empresa.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteRestDTO.Response> buscarPorId(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        Cliente cliente = clienteService.buscarPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + id + " não encontrado."));
        return ResponseEntity.ok(toResponse(cliente));
    }

    @PostMapping
    @Operation(summary = "Cadastrar novo cliente")
    public ResponseEntity<ClienteRestDTO.Response> criar(
            @PathVariable String slug,
            @Valid @RequestBody ClienteRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        Cliente novoCliente = new Cliente();
        novoCliente.setNome(request.nome());
        novoCliente.setTelefone(request.telefone());
        novoCliente.setEmail(request.email());
        novoCliente.setDataNascimento(request.dataNascimento());
        novoCliente.setEmpresa(empresa);

        Cliente salvo = clienteService.salvar(novoCliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de um cliente existente")
    public ResponseEntity<ClienteRestDTO.Response> atualizar(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody ClienteRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        Cliente existente = clienteService.buscarPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente com ID " + id + " não encontrado."));

        existente.setNome(request.nome());
        existente.setTelefone(request.telefone());
        existente.setEmail(request.email());
        existente.setDataNascimento(request.dataNascimento());

        Cliente atualizado = clienteService.salvar(existente);
        return ResponseEntity.ok(toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um cliente")
    public ResponseEntity<Void> excluir(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        clienteService.excluir(id, empresa.getId());
        return ResponseEntity.noContent().build();
    }
}
