package com.beautysalon.Implementacao;

import com.beautysalon.DTO.EmpresaDTO;
import com.beautysalon.DTO.RegisterEmpresaDTO;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmpresaServiceImpl implements EmpresaService {

    private static final Logger log = LoggerFactory.getLogger(EmpresaServiceImpl.class);

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository,
                              UserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public String registrarNovaEmpresa(RegisterEmpresaDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas não conferem.");
        }

        // Gera o slug a partir do nome da empresa
        String slug = gerarSlug(dto.getNomeEmpresa());

        // Garante que o slug é único
        String slugFinal = slug;
        int contador = 1;
        while (empresaRepository.existsBySlug(slugFinal)) {
            slugFinal = slug + "-" + contador++;
        }

        // Cria a empresa
        Empresa empresa = Empresa.builder()
                .nome(dto.getNomeEmpresa())
                .slug(slugFinal)
                .email(dto.getEmailEmpresa())
                .telefone(dto.getTelefoneEmpresa())
                .cnpj(dto.getCnpj())
                .ativo(true)
                .build();

        empresa = empresaRepository.save(empresa);
        log.info("Empresa criada: {} (slug={})", empresa.getNome(), empresa.getSlug());

        // Cria o usuário OWNER
        User owner = User.builder()
                .nome(dto.getNomeUsuario())
                .username(dto.getUsername())
                .email(dto.getEmailUsuario())
                .password(passwordEncoder.encode(dto.getPassword()))
                .ativo(true)
                .tenantRole(TenantRole.OWNER)
                .empresa(empresa)
                .build();

        userRepository.save(owner);
        log.info("Usuário OWNER criado: {} para empresa {}", owner.getUsername(), empresa.getSlug());

        return empresa.getSlug();
    }

    @Override
    public EmpresaDTO buscarPorSlug(String slug) {
        return empresaRepository.findBySlug(slug)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada: " + slug));
    }

    @Override
    public EmpresaDTO buscarPorId(Long id) {
        return empresaRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada: " + id));
    }

    @Override
    public List<EmpresaDTO> listarTodas() {
        return empresaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmpresaDTO atualizar(Long id, EmpresaDTO dto) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada: " + id));
        empresa.setNome(dto.getNome());
        empresa.setEmail(dto.getEmail());
        empresa.setTelefone(dto.getTelefone());
        empresa.setCnpj(dto.getCnpj());
        return toDTO(empresaRepository.save(empresa));
    }

    /**
     * Converte nome da empresa em slug URL-safe.
     * Ex: "Studio Lumora & Cia." → "studio-lumora-cia"
     */
    public static String gerarSlug(String nome) {
        String normalizado = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
        return normalizado.length() > 50 ? normalizado.substring(0, 50) : normalizado;
    }

    private EmpresaDTO toDTO(Empresa e) {
        EmpresaDTO dto = new EmpresaDTO();
        dto.setId(e.getId());
        dto.setNome(e.getNome());
        dto.setSlug(e.getSlug());
        dto.setCnpj(e.getCnpj());
        dto.setTelefone(e.getTelefone());
        dto.setEmail(e.getEmail());
        dto.setLogoUrl(e.getLogoUrl());
        dto.setAtivo(e.isAtivo());
        return dto;
    }
}
