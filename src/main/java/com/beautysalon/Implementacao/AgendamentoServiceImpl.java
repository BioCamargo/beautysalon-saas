package com.beautysalon.Implementacao;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.Inteface.AgendamentoService;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.Servico;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.ServicoRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgendamentoServiceImpl implements AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final ClienteRepository clienteRepository;
    private final ServicoRepository servicoRepository;
    private final EmpresaRepository empresaRepository;
    private final com.beautysalon.repository.UserRepository userRepository;
    private final com.beautysalon.service.WhatsAppService whatsAppService;

    public AgendamentoServiceImpl(AgendamentoRepository agendamentoRepository,
                                  ClienteRepository clienteRepository,
                                  ServicoRepository servicoRepository,
                                  EmpresaRepository empresaRepository,
                                  com.beautysalon.repository.UserRepository userRepository,
                                  com.beautysalon.service.WhatsAppService whatsAppService) {
        this.agendamentoRepository = agendamentoRepository;
        this.clienteRepository = clienteRepository;
        this.servicoRepository = servicoRepository;
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.whatsAppService = whatsAppService;
    }

    @Override
    public List<AgendamentoDTO> listarTodos() {
        Long empresaId = TenantContext.getEmpresaId();
        return agendamentoRepository.findAllByEmpresaId(empresaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public AgendamentoDTO buscarPorId(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return agendamentoRepository.findByIdAndEmpresaId(id, empresaId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
    }

    @Override
    @Transactional
    public AgendamentoDTO salvar(AgendamentoDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada no contexto"));

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(dto.getClienteId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado nesta empresa"));

        Servico servico = servicoRepository.findByIdAndEmpresaId(dto.getServicoId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado nesta empresa"));

        Agendamento agendamento = new Agendamento();
        agendamento.setEmpresa(empresa);
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setCliente(cliente);
        agendamento.setStatus(dto.getStatus() != null ? dto.getStatus() : "AGENDADO");
        agendamento.setObservacoes(dto.getObservacoes());

        if (dto.getProfissionalId() != null) {
            validarConflitoHorario(empresaId, dto.getProfissionalId(), dto.getDataHora(), servico, null);
            agendamento.setProfissional(userRepository.findByIdAndEmpresaId(dto.getProfissionalId(), empresaId).orElse(null));
        }

        if (agendamento.getServicos() == null) {
            agendamento.setServicos(new ArrayList<>());
        }
        agendamento.getServicos().add(servico);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        // Dispara mensagem de confirmação no WhatsApp do cliente
        try {
            whatsAppService.enviarConfirmacaoAgendamento(salvo);
        } catch (Exception e) {
            // Ignora falha de WhatsApp para não interromper agendamento
        }

        return toDTO(salvo);
    }

    @Override
    @Transactional
    public AgendamentoDTO atualizar(Long id, AgendamentoDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Agendamento existente = agendamentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(dto.getClienteId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado nesta empresa"));

        Servico servico = servicoRepository.findByIdAndEmpresaId(dto.getServicoId(), empresaId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado nesta empresa"));

        if (dto.getProfissionalId() != null) {
            validarConflitoHorario(empresaId, dto.getProfissionalId(), dto.getDataHora(), servico, id);
            existente.setProfissional(userRepository.findByIdAndEmpresaId(dto.getProfissionalId(), empresaId).orElse(null));
        } else {
            existente.setProfissional(null);
        }

        existente.setDataHora(dto.getDataHora());
        existente.setCliente(cliente);
        if (dto.getStatus() != null) existente.setStatus(dto.getStatus());
        if (dto.getObservacoes() != null) existente.setObservacoes(dto.getObservacoes());
        existente.getServicos().clear();
        existente.getServicos().add(servico);

        Agendamento salvo = agendamentoRepository.save(existente);
        return toDTO(salvo);
    }

    private void validarConflitoHorario(Long empresaId, Long profissionalId, java.time.LocalDateTime novoInicio, Servico servico, Long agendamentoIdIgnorar) {
        int duracaoMin = (servico.getDuracaoMinutos() != null && servico.getDuracaoMinutos() > 0) ? servico.getDuracaoMinutos() : 30;
        java.time.LocalDateTime novoFim = novoInicio.plusMinutes(duracaoMin);

        List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndProfissionalIdOrderByDataHoraAsc(empresaId, profissionalId);

        for (Agendamento ag : agendamentos) {
            if (agendamentoIdIgnorar != null && ag.getId().equals(agendamentoIdIgnorar)) {
                continue;
            }
            if ("CANCELADO".equalsIgnoreCase(ag.getStatus()) || "NAO_COMPARECEU".equalsIgnoreCase(ag.getStatus())) {
                continue;
            }

            java.time.LocalDateTime existenteInicio = ag.getDataHora();
            int duracaoExistente = 30;
            if (ag.getServicos() != null && !ag.getServicos().isEmpty() && ag.getServicos().get(0).getDuracaoMinutos() != null) {
                duracaoExistente = ag.getServicos().get(0).getDuracaoMinutos();
            }
            java.time.LocalDateTime existenteFim = existenteInicio.plusMinutes(duracaoExistente);

            // Intervalos [novoInicio, novoFim) e [existenteInicio, existenteFim) se sobrepõem se:
            // novoInicio < existenteFim && novoFim > existenteInicio
            if (novoInicio.isBefore(existenteFim) && novoFim.isAfter(existenteInicio)) {
                java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
                throw new RuntimeException(String.format(
                        "Conflito de agenda: O profissional já possui atendimento entre %s e %s.",
                        existenteInicio.format(fmt), existenteFim.format(fmt)
                ));
            }
        }
    }

    @Override
    @Transactional
    public void deletar(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Agendamento agendamento = agendamentoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado"));
        agendamentoRepository.delete(agendamento);
    }

    @Override
    public List<Agendamento> listarPorServico(Long servicoId) {
        Long empresaId = TenantContext.getEmpresaId();
        return agendamentoRepository.findByServicoIdAndEmpresaId(servicoId, empresaId);
    }

    private AgendamentoDTO toDTO(Agendamento a) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(a.getId());
        dto.setDataHora(a.getDataHora());
        dto.setStatus(a.getStatus());
        dto.setObservacoes(a.getObservacoes());

        if (a.getCliente() != null) {
            dto.setClienteId(a.getCliente().getId());
            dto.setClienteNome(a.getCliente().getNome());
            dto.setClienteTelefone(a.getCliente().getTelefone());
        }

        if (a.getProfissional() != null) {
            dto.setProfissionalId(a.getProfissional().getId());
            dto.setProfissionalNome(a.getProfissional().getNome());
        }

        if (a.getServicos() != null && !a.getServicos().isEmpty()) {
            Servico servico = a.getServicos().get(0);
            dto.setServicoId(servico.getId());
            dto.setServicoNome(servico.getNome());
            dto.setDuracaoMinutos(servico.getDuracaoMinutos() != null ? servico.getDuracaoMinutos() : 30);
        } else {
            dto.setDuracaoMinutos(30);
        }

        return dto;
    }
}
