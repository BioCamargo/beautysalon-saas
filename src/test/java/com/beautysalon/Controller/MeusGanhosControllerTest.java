package com.beautysalon.Controller;

import com.beautysalon.model.Comanda;
import com.beautysalon.model.ComandaItem;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.ComandaItemRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MeusGanhosControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ComandaItemRepository comandaItemRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private MeusGanhosController meusGanhosController;

    private Empresa empresa;
    private User funcionario;
    private User admin;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder().id(10L).slug("studio-vip").nome("Studio VIP").build();
        TenantContext.setEmpresaId(10L);
        TenantContext.setSlug("studio-vip");

        funcionario = User.builder()
                .id(101L)
                .username("keilla")
                .nome("Keilla Especialista")
                .tenantRole(TenantRole.FUNCIONARIO)
                .percentualComissao(new BigDecimal("40.00"))
                .empresa(empresa)
                .build();

        admin = User.builder()
                .id(100L)
                .username("admin")
                .nome("Admin Dono")
                .tenantRole(TenantRole.OWNER)
                .empresa(empresa)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("DEV-03: Funcionário acessa seus ganhos e é forçado a consultar apenas seus próprios itens")
    void funcionarioConsultaSeusGanhosComSucesso() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("keilla");
        when(userRepository.findByUsernameAndEmpresaId("keilla", 10L)).thenReturn(Optional.of(funcionario));

        Comanda comanda = Comanda.builder().id(1L).empresa(empresa).dataFechamento(LocalDateTime.now()).status("PAGA").build();
        ComandaItem item = ComandaItem.builder()
                .id(50L)
                .comanda(comanda)
                .descricaoItem("Corte & Escova")
                .tipo("SERVICO")
                .valorTotal(new BigDecimal("100.00"))
                .percentualComissao(new BigDecimal("40.00"))
                .valorComissao(new BigDecimal("40.00"))
                .profissional(funcionario)
                .build();

        when(comandaItemRepository.sumComissaoProfissional(eq(10L), eq(101L), any(), any()))
                .thenReturn(new BigDecimal("40.00"));
        when(comandaItemRepository.findItensComComandaEClientePorPeriodo(eq(10L), eq(101L), any(), any()))
                .thenReturn(List.of(item));

        Model model = new ConcurrentModel();

        // Tenta passar profissionalId=999 (de outro colaborador), mas o controller DEVE ignorar e forçar keilla (101L)
        String view = meusGanhosController.meusGanhos("studio-vip", "mes", null, null, 999L, authentication, model);

        assertEquals("profissional/meus-ganhos", view);
        assertEquals(funcionario, model.getAttribute("profissionalAlvo"));
        assertEquals(new BigDecimal("40.00"), model.getAttribute("ganhosMes"));
        assertEquals(new BigDecimal("40.00"), model.getAttribute("ganhosPeriodo"));
        assertEquals(1, model.getAttribute("totalAtendimentosPeriodo"));

        // Garante que consultou o ID 101L e NÃO o 999L
        verify(comandaItemRepository, atLeastOnce()).sumComissaoProfissional(eq(10L), eq(101L), any(), any());
        verify(comandaItemRepository, never()).sumComissaoProfissional(eq(10L), eq(999L), any(), any());
    }

    @Test
    @DisplayName("DEV-03: Admin pode consultar ganhos de qualquer profissional da equipe")
    void adminPodeConsultarOutroProfissional() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin");
        when(userRepository.findByUsernameAndEmpresaId("admin", 10L)).thenReturn(Optional.of(admin));
        when(userRepository.findByIdAndEmpresaId(101L, 10L)).thenReturn(Optional.of(funcionario));
        when(userRepository.findAllByEmpresaIdAndAtivoTrue(10L)).thenReturn(List.of(admin, funcionario));

        when(comandaItemRepository.sumComissaoProfissional(eq(10L), eq(101L), any(), any()))
                .thenReturn(new BigDecimal("80.00"));
        when(comandaItemRepository.findItensComComandaEClientePorPeriodo(eq(10L), eq(101L), any(), any()))
                .thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = meusGanhosController.meusGanhos("studio-vip", "7dias", null, null, 101L, authentication, model);

        assertEquals("profissional/meus-ganhos", view);
        assertEquals(funcionario, model.getAttribute("profissionalAlvo"));
        assertEquals(true, model.getAttribute("isAdminOuOwner"));
        assertNotNull(model.getAttribute("listaEquipe"));
    }

    @Test
    @DisplayName("DEV-03: Acesso não autenticado redireciona para login")
    void acessoNaoAutenticadoRedirecionaParaLogin() {
        String view = meusGanhosController.meusGanhos("studio-vip", "mes", null, null, null, null, new ConcurrentModel());
        assertEquals("redirect:/login", view);
    }
}
