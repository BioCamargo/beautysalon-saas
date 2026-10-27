package com.beautysalon.Implementacao;

import com.beautysalon.DTO.UserDTO;
import com.beautysalon.Inteface.UserService;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.util.TextNormalizationUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmpresaRepository empresaRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           EmpresaRepository empresaRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.empresaRepository = empresaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<UserDTO> listarTodosDaEmpresa(Long empresaId) {
        return userRepository.findAllByEmpresaId(empresaId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO buscarPorId(Long id, Long empresaId) {
        return userRepository.findByIdAndEmpresaId(id, empresaId)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    @org.springframework.beans.factory.annotation.Value("${upload.dir:uploads}")
    private String uploadDir = "uploads";

    private String processarUploadImagem(org.springframework.web.multipart.MultipartFile imagemFile) throws IOException {
        if (imagemFile == null || imagemFile.isEmpty()) {
            return null;
        }
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String originalFilename = imagemFile.getOriginalFilename();
        String extensao = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extensao = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        }
        String nomeArquivo = UUID.randomUUID() + extensao;
        Path caminho = uploadPath.resolve(nomeArquivo);

        try (java.io.InputStream inputStream = imagemFile.getInputStream()) {
            Files.copy(inputStream, caminho, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + nomeArquivo;
    }

    @Override
    @Transactional
    public UserDTO salvar(UserDTO dto) throws IOException {
        Long empresaId = dto.getEmpresaId() != null ? dto.getEmpresaId() : com.beautysalon.tenant.TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        User user = new User();
        user.setNome(TextNormalizationUtil.capitalizeTitle(dto.getNome()));
        user.setUsername(dto.getUsername() != null ? dto.getUsername().trim().toLowerCase() : null);
        user.setEmail(TextNormalizationUtil.normalizeEmail(dto.getEmail()));
        user.setAtivo(true);
        user.setTenantRole(dto.getTenantRole() != null ? dto.getTenantRole() : TenantRole.FUNCIONARIO);
        user.setEmpresa(empresa);

        user.setPercentualComissao(dto.getPercentualComissao() != null ? dto.getPercentualComissao() : java.math.BigDecimal.ZERO);
        user.setEspecialidade(dto.getEspecialidade());
        user.setTelefone(TextNormalizationUtil.normalizePhone(dto.getTelefone()));
        user.setChavePix(dto.getChavePix());
        if (dto.getHoraInicioExpediente() != null) user.setHoraInicioExpediente(dto.getHoraInicioExpediente());
        if (dto.getHoraFimExpediente() != null) user.setHoraFimExpediente(dto.getHoraFimExpediente());
        user.setDiasTrabalho(dto.getDiasTrabalho() != null ? dto.getDiasTrabalho() : "SEGUNDA,TERCA,QUARTA,QUINTA,SEXTA,SABADO");
        user.setCorAgenda(dto.getCorAgenda() != null && !dto.getCorAgenda().isBlank() ? dto.getCorAgenda() : "#d4af37");

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
            String caminhoImagem = processarUploadImagem(dto.getImagem());
            user.setImagem(caminhoImagem);
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO atualizar(Long id, UserDTO dto, Long empresaId) {
        User existente = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        existente.setNome(TextNormalizationUtil.capitalizeTitle(dto.getNome()));
        existente.setEmail(TextNormalizationUtil.normalizeEmail(dto.getEmail()));
        if (dto.getPercentualComissao() != null) {
            existente.setPercentualComissao(dto.getPercentualComissao());
        }
        existente.setEspecialidade(dto.getEspecialidade());
        existente.setTelefone(TextNormalizationUtil.normalizePhone(dto.getTelefone()));
        existente.setChavePix(dto.getChavePix());
        if (dto.getHoraInicioExpediente() != null) existente.setHoraInicioExpediente(dto.getHoraInicioExpediente());
        if (dto.getHoraFimExpediente() != null) existente.setHoraFimExpediente(dto.getHoraFimExpediente());
        if (dto.getDiasTrabalho() != null) existente.setDiasTrabalho(dto.getDiasTrabalho());
        if (dto.getCorAgenda() != null && !dto.getCorAgenda().isBlank()) {
            existente.setCorAgenda(dto.getCorAgenda());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getTenantRole() != null) {
            existente.setTenantRole(dto.getTenantRole());
        }
        if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
            try {
                String caminhoImagem = processarUploadImagem(dto.getImagem());
                existente.setImagem(caminhoImagem);
            } catch (IOException e) {
                throw new RuntimeException("Erro ao processar imagem do usuário: " + e.getMessage(), e);
            }
        }
        return toDTO(userRepository.save(existente));
    }

    @Override
    public void deletar(Long id, Long empresaId) {
        User user = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        userRepository.delete(user);
    }

    @Override
    public boolean existsByUsername(String username, Long empresaId) {
        return userRepository.existsByUsernameAndEmpresaId(username, empresaId);
    }

    @Override
    public boolean existsByEmail(String email, Long empresaId) {
        return userRepository.existsByEmailAndEmpresaId(email, empresaId);
    }

    @Override
    @Transactional
    public void registerUser(UserDTO dto, Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        User user = new User();
        user.setNome(dto.getNome() != null ? dto.getNome() : dto.getUsername());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setAtivo(true);
        user.setTenantRole(dto.getTenantRole() != null ? dto.getTenantRole() : TenantRole.FUNCIONARIO);
        user.setEmpresa(empresa);
        user.setPercentualComissao(dto.getPercentualComissao() != null ? dto.getPercentualComissao() : java.math.BigDecimal.ZERO);
        user.setEspecialidade(dto.getEspecialidade());
        user.setTelefone(dto.getTelefone());
        user.setChavePix(dto.getChavePix());
        if (dto.getHoraInicioExpediente() != null) user.setHoraInicioExpediente(dto.getHoraInicioExpediente());
        if (dto.getHoraFimExpediente() != null) user.setHoraFimExpediente(dto.getHoraFimExpediente());
        if (dto.getDiasTrabalho() != null && !dto.getDiasTrabalho().isBlank()) user.setDiasTrabalho(dto.getDiasTrabalho());
        user.setCorAgenda(dto.getCorAgenda() != null && !dto.getCorAgenda().isBlank() ? dto.getCorAgenda() : "#d4af37");

        userRepository.save(user);
    }

    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setNome(user.getNome());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAtivo(user.isAtivo());
        dto.setTenantRole(user.getTenantRole());
        dto.setImagemUrl(user.getImagem());
        dto.setPercentualComissao(user.getPercentualComissao());
        dto.setEspecialidade(user.getEspecialidade());
        dto.setTelefone(user.getTelefone());
        dto.setChavePix(user.getChavePix());
        dto.setHoraInicioExpediente(user.getHoraInicioExpediente());
        dto.setHoraFimExpediente(user.getHoraFimExpediente());
        dto.setDiasTrabalho(user.getDiasTrabalho());
        dto.setCorAgenda(user.getCorAgenda());
        if (user.getEmpresa() != null) {
            dto.setEmpresaId(user.getEmpresa().getId());
            dto.setEmpresaNome(user.getEmpresa().getNome());
        }
        return dto;
    }
}
