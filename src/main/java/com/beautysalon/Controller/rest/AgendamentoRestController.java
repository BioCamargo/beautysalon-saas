package com.beautysalon.Controller.rest;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.DTO.rest.AgendamentoRestDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.Inteface.AgendamentoService;
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
@RequestMapping("/api/v1/{slug}/agendamentos")
@Tag(name = "Agendamentos", description = "Endpoints para gestão de agenda e marcação de horários por Tenant")
public class AgendamentoRestController {

    private final AgendamentoService agendamentoService;
    private final EmpresaRepository empresaRepository;

    public AgendamentoRestController(AgendamentoService agendamentoService,
            EmpresaRepository empresaRepository) {
        this.agendamentoService = agendamentoService;
        this.empresaRepository = empresaRepository;
    }

    private Empresa obterEmpresa(String slug) {
        Empresa emp = empresaRepository.findBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
        TenantContext.setTenant(emp.getSlug(), emp.getId());
        return emp;
    }

    private AgendamentoRestDTO.Response toResponse(AgendamentoDTO a) {
        return new AgendamentoRestDTO.Response(
                a.getId(),
                a.getDataHora(),
                a.getStatus() != null ? a.getStatus() : "PENDENTE",
                a.getObservacoes(),
                a.getClienteId(),
                a.getClienteNome(),
                a.getServicoId(),
                a.getServicoNome(),
                a.getProfissionalId(),
                a.getProfissionalNome());
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos da empresa")
    public ResponseEntity<List<AgendamentoRestDTO.Response>> listar(@PathVariable String slug) {
        obterEmpresa(slug);
        List<AgendamentoRestDTO.Response> response = agendamentoService.listarTodos()
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AgendamentoRestDTO.Response> buscarPorId(@PathVariable String slug, @PathVariable Long id) {
        obterEmpresa(slug);
        AgendamentoDTO dto = agendamentoService.buscarPorId(id);
        if (dto == null) {
            throw new EntityNotFoundException("Agendamento não encontrado com id: " + id);
        }
        return ResponseEntity.ok(toResponse(dto));
    }

    @PostMapping
    @Operation(summary = "Criar novo agendamento")
    public ResponseEntity<AgendamentoRestDTO.Response> criar(
            @PathVariable String slug,
            @Valid @RequestBody AgendamentoRestDTO.Request request) {

        obterEmpresa(slug);
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setDataHora(request.dataHora());
        dto.setClienteId(request.clienteId());
        dto.setServicoId(request.servicoId());
        dto.setProfissionalId(request.profissionalId());
        dto.setObservacoes(request.observacoes());
        dto.setStatus("CONFIRMADO");

        AgendamentoDTO salvo = agendamentoService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do agendamento (ex: REALIZADO, CANCELADO, CONFIRMADO)")
    public ResponseEntity<AgendamentoRestDTO.Response> atualizarStatus(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRestDTO.StatusUpdateRequest request) {

        obterEmpresa(slug);
        AgendamentoDTO dto = agendamentoService.buscarPorId(id);
        if (dto == null) {
            throw new EntityNotFoundException("Agendamento não encontrado com id: " + id);
        }

        dto.setStatus(request.status().toUpperCase());
        AgendamentoDTO atualizado = agendamentoService.atualizar(id, dto);
        return ResponseEntity.ok(toResponse(atualizado));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um agendamento")
    public ResponseEntity<Void> excluir(@PathVariable String slug, @PathVariable Long id) {
        obterEmpresa(slug);
        agendamentoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
