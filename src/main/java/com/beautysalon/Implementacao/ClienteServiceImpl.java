package com.beautysalon.Implementacao;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public ClienteServiceImpl(ClienteRepository clienteRepository, EmpresaRepository empresaRepository) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @Override
    public List<ClienteDTO> listarTodos() {
        Long empresaId = TenantContext.getEmpresaId();
        return clienteRepository.findAllByEmpresaId(empresaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDTO buscarPorId(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
    }

    @Override
    @Transactional
    public ClienteDTO salvar(ClienteDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada no contexto"));

        Cliente cliente = new Cliente();
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setAlergias(dto.getAlergias());
        cliente.setTipoCabeloPele(dto.getTipoCabeloPele());
        cliente.setHistoricoQuimico(dto.getHistoricoQuimico());
        cliente.setObservacoesTecnicas(dto.getObservacoesTecnicas());
        cliente.setEmpresa(empresa);

        return toDTO(clienteRepository.save(cliente));
    }

    @Override
    public void deletar(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        clienteRepository.delete(cliente);
    }

    @Override
    @Transactional
    public ClienteDTO atualizar(Long id, ClienteDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado"));
        cliente.setNome(dto.getNome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefone(dto.getTelefone());
        cliente.setDataNascimento(dto.getDataNascimento());
        cliente.setAlergias(dto.getAlergias());
        cliente.setTipoCabeloPele(dto.getTipoCabeloPele());
        cliente.setHistoricoQuimico(dto.getHistoricoQuimico());
        cliente.setObservacoesTecnicas(dto.getObservacoesTecnicas());
        return toDTO(clienteRepository.save(cliente));
    }

    @Override
    public List<ClienteDTO> buscarPorNomeParcial(String nome) {
        Long empresaId = TenantContext.getEmpresaId();
        return clienteRepository.buscarPorNomeParcialEEmpresa(nome, empresaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ClienteDTO toDTO(Cliente c) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(c.getId());
        dto.setNome(c.getNome());
        dto.setEmail(c.getEmail());
        dto.setTelefone(c.getTelefone());
        dto.setDataNascimento(c.getDataNascimento());
        dto.setAlergias(c.getAlergias());
        dto.setTipoCabeloPele(c.getTipoCabeloPele());
        dto.setHistoricoQuimico(c.getHistoricoQuimico());
        dto.setObservacoesTecnicas(c.getObservacoesTecnicas());
        return dto;
    }
}
