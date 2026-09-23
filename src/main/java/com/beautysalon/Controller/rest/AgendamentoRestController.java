package com.beautysalon.controller.rest;

import com.beautysalon.dto.rest.AgendamentoRestDTO;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.Servico;
import com.beautysalon.model.User;
import com.beautysalon.Inteface.AgendamentoService;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.Inteface.ServicoService;
import com.beautysalon.Inteface.UserService;
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
    private final EmpresaService empresaService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final UserService userService;

    public AgendamentoRestController(AgendamentoService agendamentoService,
                                     EmpresaService empresaService,
                                     ClienteService clienteService,
                                     ServicoService servicoService,
                                     UserService userService) {
        this.agendamentoService = agendamentoService;
        this.empresaService = empresaService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
        this.userService = userService;
    }

    private Empresa obterEmpresa(String slug) {
        return empresaService.buscarPorSlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Empresa não encontrada para o slug: " + slug));
    }

    private AgendamentoRestDTO.Response toResponse(Agendamento a) {
        return new AgendamentoRestDTO.Response(
                a.getId(),
                a.getDataHora(),
                a.getStatus() != null ? a.getStatus().name() : "PENDENTE",
                a.getObservacoes(),
                a.getCliente() != null ? a.getCliente().getId() : null,
                a.getCliente() != null ? a.getCliente().getNome() : null,
                a.getServico() != null ? a.getServico().getId() : null,
                a.getServico() != null ? a.getServico().getNome() : null,
                a.getProfissional() != null ? a.getProfissional().getId() : null,
                a.getProfissional() != null ? a.getProfissional().getNome() : null
        );
    }

    @GetMapping
    @Operation(summary = "Listar agendamentos da empresa")
    public ResponseEntity<List<AgendamentoRestDTO.Response>> listar(@PathVariable String slug) {
        Empresa empresa = obterEmpresa(slug);
        List<AgendamentoRestDTO.Response> response = agendamentoService.listarPorEmpresa(empresa.getId())
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar agendamento por ID")
    public ResponseEntity<AgendamentoRestDTO.Response> buscarPorId(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        Agendamento agendamento = agendamentoService.buscarPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));
        return ResponseEntity.ok(toResponse(agendamento));
    }

    @PostMapping
    @Operation(summary = "Criar novo agendamento")
    public ResponseEntity<AgendamentoRestDTO.Response> criar(
            @PathVariable String slug,
            @Valid @RequestBody AgendamentoRestDTO.Request request) {

        Empresa empresa = obterEmpresa(slug);
        Cliente cliente = clienteService.buscarPorIdEEmpresa(request.clienteId(), empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cliente informado não existe no tenant."));

        Servico servico = servicoService.buscarPorIdEEmpresa(request.servicoId(), empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Serviço informado não existe no tenant."));

        User profissional = null;
        if (request.profissionalId() != null) {
            profissional = userService.buscarPorIdEEmpresa(request.profissionalId(), empresa.getId())
                    .orElse(null);
        }

        Agendamento agendamento = new Agendamento();
        agendamento.setDataHora(request.dataHora());
        agendamento.setCliente(cliente);
        agendamento.setServico(servico);
        agendamento.setProfissional(profissional);
        agendamento.setObservacoes(request.observacoes());
        agendamento.setEmpresa(empresa);
        agendamento.setStatus(Agendamento.StatusAgendamento.CONFIRMADO);

        Agendamento salvo = agendamentoService.salvar(agendamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(salvo));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Atualizar status do agendamento (ex: REALIZADO, CANCELADO, CONFIRMADO)")
    public ResponseEntity<AgendamentoRestDTO.Response> atualizarStatus(
            @PathVariable String slug,
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoRestDTO.StatusUpdateRequest request) {

        Empresa empresa = obterEmpresa(slug);
        Agendamento agendamento = agendamentoService.buscarPorIdEEmpresa(id, empresa.getId())
                .orElseThrow(() -> new EntityNotFoundException("Agendamento não encontrado."));

        try {
            Agendamento.StatusAgendamento novoStatus = Agendamento.StatusAgendamento.valueOf(request.status().toUpperCase());
            agendamento.setStatus(novoStatus);
            Agendamento atualizado = agendamentoService.salvar(agendamento);
            return ResponseEntity.ok(toResponse(atualizado));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status inválido: " + request.status() + ". Valores aceitos: PENDENTE, CONFIRMADO, EM_ATENDIMENTO, REALIZADO, CANCELADO, FALTOU");
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um agendamento")
    public ResponseEntity<Void> excluir(@PathVariable String slug, @PathVariable Long id) {
        Empresa empresa = obterEmpresa(slug);
        agendamentoService.excluir(id, empresa.getId());
        return ResponseEntity.noContent().build();
    }
}
