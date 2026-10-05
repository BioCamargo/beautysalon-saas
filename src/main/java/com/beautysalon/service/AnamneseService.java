package com.beautysalon.service;

import com.beautysalon.model.Cliente;
import com.beautysalon.model.ClienteAnamnese;
import com.beautysalon.model.User;
import com.beautysalon.repository.ClienteAnamneseRepository;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AnamneseService {

    private final ClienteAnamneseRepository anamneseRepository;
    private final ClienteRepository clienteRepository;
    private final UserRepository userRepository;

    public AnamneseService(ClienteAnamneseRepository anamneseRepository,
                           ClienteRepository clienteRepository,
                           UserRepository userRepository) {
        this.anamneseRepository = anamneseRepository;
        this.clienteRepository = clienteRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteAnamnese> buscarHistoricoPorCliente(Long clienteId) {
        Long empresaId = TenantContext.getEmpresaId();
        return anamneseRepository.findByClienteIdAndEmpresaIdOrderByDataRegistroDesc(clienteId, empresaId);
    }

    @Transactional
    public ClienteAnamnese salvarAnamnese(Long clienteId,
                                          String procedimentoRealizado,
                                          String formulaQuimica,
                                          String historicoCapilarAlergias,
                                          String observacoesTecnicas,
                                          String fotoAntesUrl,
                                          String fotoDepoisUrl,
                                          String assinaturaDigitalBase64,
                                          Long profissionalId) {
        Long empresaId = TenantContext.getEmpresaId();
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado para esta empresa"));

        User profissional = null;
        if (profissionalId != null) {
            profissional = userRepository.findByIdAndEmpresaId(profissionalId, empresaId).orElse(null);
        }

        ClienteAnamnese anamnese = ClienteAnamnese.builder()
                .cliente(cliente)
                .empresa(cliente.getEmpresa())
                .profissional(profissional)
                .procedimentoRealizado(procedimentoRealizado)
                .formulaQuimica(formulaQuimica)
                .historicoCapilarAlergias(historicoCapilarAlergias)
                .observacoesTecnicas(observacoesTecnicas)
                .fotoAntesUrl(fotoAntesUrl)
                .fotoDepoisUrl(fotoDepoisUrl)
                .assinaturaDigitalBase64(assinaturaDigitalBase64)
                .termoConsentimentoAceito(true)
                .dataRegistro(LocalDateTime.now())
                .build();

        return anamneseRepository.save(anamnese);
    }
}
