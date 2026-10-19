package com.beautysalon.tenant;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private final TenantLoggingFilter filter = new TenantLoggingFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("DEV-05: TenantLoggingFilter injeta contexto MDC e limpa incondicionalmente no final")
    void testFilterLifecycleAndCleanMdc() throws ServletException, IOException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("admin-user");
        SecurityContextHolder.setContext(securityContext);

        doAnswer(invocation -> {
            // Durante a execução do filterChain, os valores devem estar no MDC
            org.junit.jupiter.api.Assertions.assertEquals("global", MDC.get(TenantLoggingFilter.MDC_KEY_TENANT));
            org.junit.jupiter.api.Assertions.assertEquals("admin-user", MDC.get(TenantLoggingFilter.MDC_KEY_USUARIO));
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);

        // Após a execução, MDC deve ter sido 100% limpo
        assertNull(MDC.get(TenantLoggingFilter.MDC_KEY_TENANT));
        assertNull(MDC.get(TenantLoggingFilter.MDC_KEY_USUARIO));
    }
}
