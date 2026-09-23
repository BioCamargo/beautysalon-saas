package com.beautysalon.converter;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Servico;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AgendamentoConverter
{
    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ServicoRepository servicoRepository;

    @Autowired
    private com.beautysalon.repository.UserRepository userRepository;

    public AgendamentoDTO toDTO(Agendamento agendamento) {
        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setId(agendamento.getId());
        dto.setDataHora(agendamento.getDataHora());
        dto.setStatus(agendamento.getStatus());
        dto.setObservacoes(agendamento.getObservacoes());

        if (agendamento.getCliente() != null) {
            dto.setClienteId(agendamento.getCliente().getId());
            dto.setClienteNome(agendamento.getCliente().getNome());
        }

        if (agendamento.getProfissional() != null) {
            dto.setProfissionalId(agendamento.getProfissional().getId());
            dto.setProfissionalNome(agendamento.getProfissional().getNome());
        }

        if (!agendamento.getServicos().isEmpty()) {
            Servico primeiroServico = agendamento.getServicos().get(0);
            dto.setServicoId(primeiroServico.getId());
            dto.setServicoNome(primeiroServico.getNome());
        }

        return dto;
    }

    public Agendamento toEntity(AgendamentoDTO dto) {
        Agendamento agendamento = new Agendamento();
        agendamento.setId(dto.getId());
        agendamento.setDataHora(dto.getDataHora());
        agendamento.setStatus(dto.getStatus() != null ? dto.getStatus() : "AGENDADO");
        agendamento.setObservacoes(dto.getObservacoes());

        Cliente cliente = clienteRepository.findById(dto.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        agendamento.setCliente(cliente);

        if (dto.getProfissionalId() != null) {
            com.beautysalon.model.User prof = userRepository.findById(dto.getProfissionalId()).orElse(null);
            agendamento.setProfissional(prof);
        }

        Servico servico = servicoRepository.findById(dto.getServicoId())
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        agendamento.getServicos().add(servico);

        return agendamento;
    }

}
