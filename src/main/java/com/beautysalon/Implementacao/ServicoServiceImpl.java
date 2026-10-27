package com.beautysalon.Implementacao;

import com.beautysalon.DTO.ServicoDTO;
import com.beautysalon.Inteface.ServicoService;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.Servico;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.ServicoRepository;
import com.beautysalon.tenant.TenantContext;
import com.beautysalon.util.TextNormalizationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServicoServiceImpl implements ServicoService {

    private final ServicoRepository servicoRepository;
    private final EmpresaRepository empresaRepository;

    public ServicoServiceImpl(ServicoRepository servicoRepository, EmpresaRepository empresaRepository) {
        this.servicoRepository = servicoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Override
    @Transactional
    public ServicoDTO salvar(ServicoDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada no contexto"));

        Servico servico = new Servico();
        servico.setNome(TextNormalizationUtil.capitalizeTitle(dto.getNome()));
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        servico.setImagem(dto.getImagem());
        servico.setDuracaoMinutos(dto.getDuracaoMinutos() != null ? dto.getDuracaoMinutos() : 30);
        servico.setDiasCicloRetorno(dto.getDiasCicloRetorno() != null ? dto.getDiasCicloRetorno() : 30);
        servico.setPercentualComissao(dto.getPercentualComissao());
        servico.setEmpresa(empresa);

        Servico salvo = servicoRepository.save(servico);
        dto.setId(salvo.getId());
        return dto;
    }

    @Override
    public List<ServicoDTO> listarTodos() {
        Long empresaId = TenantContext.getEmpresaId();
        return servicoRepository.findAllByEmpresaId(empresaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ServicoDTO buscarPorId(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        return servicoRepository.findByIdAndEmpresaId(id, empresaId)
                .map(this::toDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public void excluir(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        Servico servico = servicoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));
        servicoRepository.delete(servico);
    }

    @Override
    @Transactional
    public ServicoDTO atualizar(Long id, ServicoDTO dto) {
        Long empresaId = TenantContext.getEmpresaId();
        Servico servico = servicoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Serviço não encontrado"));

        servico.setNome(TextNormalizationUtil.capitalizeTitle(dto.getNome()));
        servico.setDescricao(dto.getDescricao());
        servico.setPreco(dto.getPreco());
        if (dto.getImagem() != null && !dto.getImagem().isBlank()) {
            servico.setImagem(dto.getImagem());
        }
        if (dto.getDuracaoMinutos() != null) {
            servico.setDuracaoMinutos(dto.getDuracaoMinutos());
        }
        if (dto.getDiasCicloRetorno() != null) {
            servico.setDiasCicloRetorno(dto.getDiasCicloRetorno());
        }
        servico.setPercentualComissao(dto.getPercentualComissao());

        Servico atualizado = servicoRepository.save(servico);
        return toDTO(atualizado);
    }

    private ServicoDTO toDTO(Servico s) {
        ServicoDTO dto = new ServicoDTO();
        dto.setId(s.getId());
        dto.setNome(s.getNome());
        dto.setDescricao(s.getDescricao());
        dto.setPreco(s.getPreco());
        dto.setImagem(s.getImagem());
        dto.setDuracaoMinutos(s.getDuracaoMinutos());
        dto.setDiasCicloRetorno(s.getDiasCicloRetorno());
        dto.setPercentualComissao(s.getPercentualComissao());
        return dto;
    }
}
