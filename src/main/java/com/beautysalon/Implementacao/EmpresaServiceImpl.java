package com.beautysalon.Implementacao;

import com.beautysalon.DTO.EmpresaDTO;
import com.beautysalon.DTO.RegisterEmpresaDTO;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.WhatsAppService;
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

    private final WhatsAppService whatsAppService;

    public EmpresaServiceImpl(EmpresaRepository empresaRepository,
                              UserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder,
                              WhatsAppService whatsAppService) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.whatsAppService = whatsAppService;
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

        // Verifica se a empresa faz parte da lista de contratos vitalícios (ex: Studio Helen Coltro)
        boolean vitalicio = isEmpresaVitalicia(dto.getNomeEmpresa(), slugFinal, dto.getCnpj(), dto.getEmailEmpresa(), dto.getEmailUsuario(), dto.getUsername());

        // Cria a empresa (Vitalícia VIP ou Plano Studio em período de teste de 7 dias)
        Empresa empresa = Empresa.builder()
                .nome(dto.getNomeEmpresa())
                .slug(slugFinal)
                .email(dto.getEmailEmpresa())
                .telefone(dto.getTelefoneEmpresa())
                .cnpj(dto.getCnpj())
                .segmento(dto.getSegmento() != null ? dto.getSegmento() : (vitalicio ? com.beautysalon.model.SegmentoEmpresa.LASH_BROW : com.beautysalon.model.SegmentoEmpresa.SALAO_BELEZA))
                .plano(vitalicio ? com.beautysalon.model.PlanoSaaS.VITALICIO : com.beautysalon.model.PlanoSaaS.STUDIO)
                .statusAssinatura(vitalicio ? com.beautysalon.model.StatusAssinatura.VITALICIO : com.beautysalon.model.StatusAssinatura.TRIAL)
                .dataExpiracaoTrial(vitalicio ? null : java.time.LocalDateTime.now().plusDays(7))
                .ativo(true)
                .build();

        empresa = empresaRepository.save(empresa);
        log.info("Empresa criada: {} (slug={})", empresa.getNome(), empresa.getSlug());

        // Auto-provisiona a Integração WhatsApp com zero parâmetros necessários
        whatsAppService.obterOuCriarConfiguracaoPadrao(empresa);

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
        log.info("🎉 [ONBOARDING] Link exclusivo de acesso gerado para '{}': /{}/login (Enviado para: {})",
                empresa.getNome(), empresa.getSlug(), owner.getEmail());

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
        if (dto.getSegmento() != null) {
            empresa.setSegmento(dto.getSegmento());
        }
        return toDTO(empresaRepository.save(empresa));
    }

    /**
     * Converte nome da empresa em slug URL-safe.
     * Ex: "Studio NEXI PRO & Cia." → "studio-nexi-pro-cia"
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

    public static boolean isEmpresaVitalicia(String nome, String slug, String cnpj, String emailEmpresa, String emailUsuario, String username) {
        String combined = ((nome != null ? nome : "") + " " +
                (slug != null ? slug : "") + " " +
                (emailEmpresa != null ? emailEmpresa : "") + " " +
                (emailUsuario != null ? emailUsuario : "") + " " +
                (username != null ? username : "")).toLowerCase();
        String cnpjDigits = cnpj != null ? cnpj.replaceAll("\\D", "") : "";

        return cnpjDigits.equals("46101466000184")
                || combined.contains("helencoltro")
                || combined.contains("helen coltro")
                || combined.contains("helen-coltro")
                || combined.contains("studiohelencoltro")
                || combined.contains("studio-helen-coltro");
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
        dto.setSegmento(e.getSegmento() != null ? e.getSegmento() : com.beautysalon.model.SegmentoEmpresa.SALAO_BELEZA);
        dto.setPlano(e.getPlano() != null ? e.getPlano() : com.beautysalon.model.PlanoSaaS.STUDIO);
        dto.setStatusAssinatura(e.getStatusAssinatura() != null ? e.getStatusAssinatura() : com.beautysalon.model.StatusAssinatura.TRIAL);
        dto.setDataExpiracaoTrial(e.getDataExpiracaoTrial());
        dto.setDiasRestantesTrial(e.getDiasRestantesTrial());
        dto.setAssinaturaValida(e.isAssinaturaValida());
        dto.setAtivo(e.isAtivo());
        return dto;
    }
}
