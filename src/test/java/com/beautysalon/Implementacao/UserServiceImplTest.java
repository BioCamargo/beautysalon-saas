package com.beautysalon.Implementacao;

import com.beautysalon.DTO.UserDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Deve salvar usuário com foto de perfil sem lançar FileNotFoundException")
    void deveSalvarUsuarioComFotoDePerfilComSucesso() throws IOException {
        Long empresaId = 1L;
        Empresa empresa = new Empresa();
        empresa.setId(empresaId);
        empresa.setNome("NEXI PRO Studio");

        when(empresaRepository.findById(empresaId)).thenReturn(Optional.of(empresa));
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        // Simula o arquivo com nome com espaços e caracteres especiais exatamente como no erro relatado
        byte[] conteudoArquivo = "teste de imagem avatar".getBytes();
        MockMultipartFile foto = new MockMultipartFile(
                "imagem",
                "Foto Curriculo-Linkedin.jpg",
                "image/jpeg",
                conteudoArquivo
        );

        UserDTO dto = new UserDTO();
        dto.setEmpresaId(empresaId);
        dto.setNome("Juliana Cabelereira");
        dto.setUsername("juliana");
        dto.setEmail("juliana@nexipro.com.br");
        dto.setPassword("senha123");
        dto.setTenantRole(TenantRole.PROFISSIONAL);
        dto.setPercentualComissao(new BigDecimal("40.0"));
        dto.setChavePix("juliana@pix.com");
        dto.setImagem(foto);

        UserDTO resultado = userService.salvar(dto);

        assertNotNull(resultado);
        assertNotNull(resultado.getImagemUrl());
        assertTrue(resultado.getImagemUrl().startsWith("/uploads/"));
        assertTrue(resultado.getImagemUrl().endsWith(".jpg"));

        // Verifica se o arquivo foi realmente criado no disco
        String caminhoRelativo = resultado.getImagemUrl().replace("/uploads/", "");
        Path arquivoCriado = Paths.get("uploads").resolve(caminhoRelativo);
        assertTrue(Files.exists(arquivoCriado), "O arquivo de foto deve existir no disco em uploads/");

        // Limpeza do teste
        Files.deleteIfExists(arquivoCriado);
    }
}
