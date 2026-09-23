package com.beautysalon.service;

import com.beautysalon.model.AusenciaProfissional;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.AusenciaProfissionalRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProfissionalService {

    private final UserRepository userRepository;
    private final AusenciaProfissionalRepository ausenciaProfissionalRepository;
    private final EmpresaRepository empresaRepository;

    public ProfissionalService(UserRepository userRepository,
                               AusenciaProfissionalRepository ausenciaProfissionalRepository,
                               EmpresaRepository empresaRepository) {
        this.userRepository = userRepository;
        this.ausenciaProfissionalRepository = ausenciaProfissionalRepository;
        this.empresaRepository = empresaRepository;
    }

    public List<User> listarProfissionaisAtivos() {
        return userRepository.findAllByEmpresaIdAndAtivoTrue(TenantContext.getEmpresaId());
    }

    public List<AusenciaProfissional> listarAusencias() {
        return ausenciaProfissionalRepository.findByEmpresaIdAndAtivoTrue(TenantContext.getEmpresaId());
    }

    public List<AusenciaProfissional> listarAusenciasPorProfissional(Long profissionalId) {
        return ausenciaProfissionalRepository.findByProfissionalIdAndEmpresaIdAndAtivoTrue(profissionalId, TenantContext.getEmpresaId());
    }

    @Transactional
    public AusenciaProfissional salvarAusencia(AusenciaProfissional ausencia) {
        if (ausencia.getEmpresa() == null) {
            Empresa emp = empresaRepository.findById(TenantContext.getEmpresaId()).orElseThrow();
            ausencia.setEmpresa(emp);
        }
        return ausenciaProfissionalRepository.save(ausencia);
    }

    @Transactional
    public void removerAusencia(Long id) {
        ausenciaProfissionalRepository.deleteById(id);
    }
}
