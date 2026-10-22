package com.beautysalon.Implementacao;

import com.beautysalon.DTO.EmpresaDTO;
import com.beautysalon.DTO.RegisterEmpresaDTO;
import com.beautysalon.model.*;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceImplTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private com.beautysalon.service.WhatsAppService whatsAppService;

    @InjectMocks
    private EmpresaServiceImpl empresaService;

    @Test
    @DisplayName("Deve registrar nova empresa com Plano Studio e 7 dias de Trial gratuito")
    void deveRegistrarNovaEmpresaComPlanoStudioETrialDe7Dias() {
        RegisterEmpresaDTO dto = new RegisterEmpresaDTO();
        dto.setNomeEmpresa("Studio Bella Donna");
        dto.setEmailEmpresa("contato@belladonna.com");
        dto.setTelefoneEmpresa("11988887777");
        dto.setCnpj("12.345.678/0001-90");
        dto.setSegmento(SegmentoEmpresa.SALAO_BELEZA);
        dto.setNomeUsuario("Donna");
        dto.setUsername("donna");
        dto.setEmailUsuario("donna@belladonna.com");
        dto.setPassword("senha123");
        dto.setConfirmPassword("senha123");

        when(empresaRepository.existsBySlug("studio-bella-donna")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");

        when(empresaRepository.save(any(Empresa.class))).thenAnswer(invocation -> {
            Empresa e = invocation.getArgument(0);
            e.setId(10L);
            return e;
        });

        String slug = empresaService.registrarNovaEmpresa(dto);

        assertEquals("studio-bella-donna", slug);

        ArgumentCaptor<Empresa> empresaCaptor = ArgumentCaptor.forClass(Empresa.class);
        verify(empresaRepository).save(empresaCaptor.capture());
        Empresa empresaSalva = empresaCaptor.getValue();

        assertEquals("Studio Bella Donna", empresaSalva.getNome());
        assertEquals("studio-bella-donna", empresaSalva.getSlug());
        assertEquals(PlanoSaaS.STUDIO, empresaSalva.getPlano());
        assertEquals(StatusAssinatura.TRIAL, empresaSalva.getStatusAssinatura());
        assertNotNull(empresaSalva.getDataExpiracaoTrial());
        assertTrue(empresaSalva.isTrialAtivo());
        assertTrue(empresaSalva.getDiasRestantesTrial() >= 6 && empresaSalva.getDiasRestantesTrial() <= 7);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User ownerSalvo = userCaptor.getValue();

        assertEquals("donna", ownerSalvo.getUsername());
        assertEquals("encodedPassword", ownerSalvo.getPassword());
        assertEquals(TenantRole.OWNER, ownerSalvo.getTenantRole());
    }

    @Test
    @DisplayName("Deve rejeitar autocadastro quando as senhas não conferem")
    void deveRejeitarSenhasDiferentes() {
        RegisterEmpresaDTO dto = new RegisterEmpresaDTO();
        dto.setPassword("senha123");
        dto.setConfirmPassword("outraSenha");

        assertThrows(IllegalArgumentException.class, () -> empresaService.registrarNovaEmpresa(dto));
        verify(empresaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve mapear corretamente os dados de plano e dias de trial no DTO")
    void deveMapearPlanoETrialNoDTO() {
        Empresa empresa = Empresa.builder()
                .id(1L)
                .nome("Studio Glow")
                .slug("studio-glow")
                .plano(PlanoSaaS.VIP)
                .statusAssinatura(StatusAssinatura.TRIAL)
                .dataExpiracaoTrial(LocalDateTime.now().plusDays(4))
                .ativo(true)
                .build();

        when(empresaRepository.findById(1L)).thenReturn(Optional.of(empresa));

        EmpresaDTO dto = empresaService.buscarPorId(1L);

        assertEquals("Studio Glow", dto.getNome());
        assertEquals(PlanoSaaS.VIP, dto.getPlano());
        assertEquals(StatusAssinatura.TRIAL, dto.getStatusAssinatura());
        assertTrue(dto.getDiasRestantesTrial() >= 3 && dto.getDiasRestantesTrial() <= 4);
        assertTrue(dto.isAssinaturaValida());
    }
}
