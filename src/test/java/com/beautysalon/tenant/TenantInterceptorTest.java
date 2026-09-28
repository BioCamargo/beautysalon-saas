package com.beautysalon.tenant;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private TenantInterceptor tenantInterceptor;

    @BeforeEach
    void setUp() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve permitir requisições para rotas públicas sem carregar tenant")
    void deveIgnorarRotasPublicas() throws Exception {
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContextPath()).thenReturn("");

        boolean result = tenantInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertNull(TenantContext.getEmpresaId());
    }

    @Test
    @DisplayName("Deve bloquear acesso se empresa não existir")
    void deveBloquearEmpresaInexistente() throws Exception {
        when(request.getRequestURI()).thenReturn("/salao-fantasma/clientes");
        when(request.getContextPath()).thenReturn("");
        when(empresaRepository.findBySlug("salao-fantasma")).thenReturn(Optional.empty());

        boolean result = tenantInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Empresa não encontrada: salao-fantasma");
    }

    @Test
    @DisplayName("Deve configurar TenantContext quando empresa existir e usuário pertencer a ela")
    void deveConfigurarTenantComSucesso() throws Exception {
        Empresa empresa = Empresa.builder().id(10L).slug("studio-vip").nome("Studio VIP").ativo(true).build();
        User user = User.builder().username("maria").empresa(empresa).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("maria", "123", java.util.Collections.emptyList())
        );

        when(request.getRequestURI()).thenReturn("/studio-vip/agendamentos");
        when(request.getContextPath()).thenReturn("");
        when(empresaRepository.findBySlug("studio-vip")).thenReturn(Optional.of(empresa));
        when(userRepository.findByUsernameAndEmpresaId("maria", 10L)).thenReturn(Optional.of(user));

        boolean result = tenantInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        assertEquals(10L, TenantContext.getEmpresaId());
        assertEquals("studio-vip", TenantContext.getSlug());
    }

    @Test
    @DisplayName("Deve bloquear usuário tentando acessar tenant de outro salão")
    void deveBloquearUsuarioDeOutroTenant() throws Exception {
        Empresa empresa = Empresa.builder().id(10L).slug("studio-vip").nome("Studio VIP").ativo(true).build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("intruso", "123", java.util.Collections.emptyList())
        );

        when(request.getRequestURI()).thenReturn("/studio-vip/financeiro");
        when(request.getContextPath()).thenReturn("");
        when(empresaRepository.findBySlug("studio-vip")).thenReturn(Optional.of(empresa));
        when(userRepository.findByUsernameAndEmpresaId("intruso", 10L)).thenReturn(Optional.empty());

        boolean result = tenantInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Acesso negado a esta empresa.");
    }
}
