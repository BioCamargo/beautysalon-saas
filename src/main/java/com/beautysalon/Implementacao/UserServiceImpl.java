package com.beautysalon.Implementacao;

import com.beautysalon.DTO.UserDTO;
import com.beautysalon.Inteface.UserService;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
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

    @Override
    @Transactional
    public UserDTO salvar(UserDTO dto) throws IOException {
        Long empresaId = dto.getEmpresaId() != null ? dto.getEmpresaId() : com.beautysalon.tenant.TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        User user = new User();
        user.setNome(dto.getNome());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setAtivo(true);
        user.setTenantRole(dto.getTenantRole() != null ? dto.getTenantRole() : TenantRole.FUNCIONARIO);
        user.setEmpresa(empresa);

        user.setPercentualComissao(dto.getPercentualComissao() != null ? dto.getPercentualComissao() : java.math.BigDecimal.ZERO);
        user.setEspecialidade(dto.getEspecialidade());
        user.setTelefone(dto.getTelefone());
        user.setCorAgenda(dto.getCorAgenda() != null && !dto.getCorAgenda().isBlank() ? dto.getCorAgenda() : "#d4af37");

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getImagem() != null && !dto.getImagem().isEmpty()) {
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String nomeArquivo = UUID.randomUUID() + "_" + dto.getImagem().getOriginalFilename();
            Path caminho = uploadPath.resolve(nomeArquivo);
            dto.getImagem().transferTo(caminho.toFile());
            user.setImagem("/uploads/" + nomeArquivo);
        }

        return toDTO(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO atualizar(Long id, UserDTO dto, Long empresaId) {
        User existente = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        existente.setNome(dto.getNome());
        existente.setEmail(dto.getEmail());
        if (dto.getPercentualComissao() != null) {
            existente.setPercentualComissao(dto.getPercentualComissao());
        }
        existente.setEspecialidade(dto.getEspecialidade());
        existente.setTelefone(dto.getTelefone());
        if (dto.getCorAgenda() != null && !dto.getCorAgenda().isBlank()) {
            existente.setCorAgenda(dto.getCorAgenda());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            existente.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        if (dto.getTenantRole() != null) {
            existente.setTenantRole(dto.getTenantRole());
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
        dto.setCorAgenda(user.getCorAgenda());
        if (user.getEmpresa() != null) {
            dto.setEmpresaId(user.getEmpresa().getId());
            dto.setEmpresaNome(user.getEmpresa().getNome());
        }
        return dto;
    }
}
