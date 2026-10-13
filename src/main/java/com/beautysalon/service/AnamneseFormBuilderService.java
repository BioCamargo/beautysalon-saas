package com.beautysalon.service;

import com.beautysalon.model.AnamneseCampoCustomizado;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TipoCampoCustomizado;
import com.beautysalon.repository.AnamneseCampoCustomizadoRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnamneseFormBuilderService {

    private final AnamneseCampoCustomizadoRepository campoRepository;
    private final EmpresaRepository empresaRepository;

    public AnamneseFormBuilderService(AnamneseCampoCustomizadoRepository campoRepository,
                                      EmpresaRepository empresaRepository) {
        this.campoRepository = campoRepository;
        this.empresaRepository = empresaRepository;
    }

    @Transactional(readOnly = true)
    public List<AnamneseCampoCustomizado> listarCamposAtivosDoTenant() {
        Long empresaId = TenantContext.getEmpresaId();
        return campoRepository.findAllByEmpresaIdAndAtivoTrueOrderByOrdemAscIdAsc(empresaId);
    }

    @Transactional(readOnly = true)
    public List<AnamneseCampoCustomizado> listarTodosDoTenant() {
        Long empresaId = TenantContext.getEmpresaId();
        return campoRepository.findAllByEmpresaIdOrderByOrdemAscIdAsc(empresaId);
    }

    @Transactional
    public AnamneseCampoCustomizado criarOuAtualizarCampo(Long id,
                                                         String rotulo,
                                                         TipoCampoCustomizado tipoCampo,
                                                         String opcoes,
                                                         String placeholder,
                                                         boolean obrigatorio,
                                                         Integer ordem) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada"));

        AnamneseCampoCustomizado campo;
        if (id != null) {
            campo = campoRepository.findByIdAndEmpresaId(id, empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Campo customizado não encontrado"));
        } else {
            campo = new AnamneseCampoCustomizado();
            campo.setEmpresa(empresa);
        }

        campo.setRotulo(rotulo);
        campo.setTipoCampo(tipoCampo);
        campo.setOpcoes(opcoes);
        campo.setPlaceholder(placeholder);
        campo.setObrigatorio(obrigatorio);
        campo.setOrdem(ordem != null ? ordem : 0);
        campo.setAtivo(true);

        return campoRepository.save(campo);
    }

    @Transactional
    public void alternarStatus(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        AnamneseCampoCustomizado campo = campoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Campo customizado não encontrado"));
        campo.setAtivo(!campo.isAtivo());
        campoRepository.save(campo);
    }

    @Transactional
    public void excluirCampo(Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        AnamneseCampoCustomizado campo = campoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Campo customizado não encontrado"));
        campoRepository.delete(campo);
    }
}
