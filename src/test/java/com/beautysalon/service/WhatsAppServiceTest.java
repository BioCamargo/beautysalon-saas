package com.beautysalon.service;

import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppServiceTest {

    @Mock
    private WhatsAppConfigRepository configRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private WhatsAppService whatsAppService;

    private Empresa empresa;
    private Cliente cliente;
    private Agendamento agendamento;
    private WhatsAppConfig config;

    @BeforeEach
    void setUp() {
        empresa = Empresa.builder()
                .id(1L)
                .nome("NEXI PRO Studio")
                .slug("nexipro")
                .build();

        cliente = Cliente.builder()
                .id(10L)
                .nome("Carla Dias")
                .telefone("11999998888")
                .empresa(empresa)
                .build();

        agendamento = Agendamento.builder()
                .id(100L)
                .empresa(empresa)
                .cliente(cliente)
                .dataHora(LocalDateTime.now().plusHours(3))
                .status("AGENDADO")
                .lembreteEnviado(false)
                .build();

        config = WhatsAppConfig.builder()
                .id(1L)
                .empresa(empresa)
                .ativo(true)
                .provider("EVOLUTION_API")
                .apiUrl("http://localhost:8080")
                .instanceName("nexipro-studio")
                .apiKey("nexipro123")
                .build();
    }

    @Test
    @DisplayName("Deve confirmar presença quando o cliente responde '1'")
    void deveConfirmarPresencaComResposta1() {
        when(configRepository.findByInstanceName("nexipro-studio")).thenReturn(Optional.of(config));
        when(configRepository.findByEmpresaId(1L)).thenReturn(Optional.of(config));
        when(agendamentoRepository.findProximosAgendamentosPorEmpresa(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(agendamento));

        boolean processado = whatsAppService.processarRespostaCliente("5511999998888", "1", "nexipro-studio");

        assertTrue(processado);
        assertEquals("CONFIRMADO", agendamento.getStatus());
        verify(agendamentoRepository, times(1)).save(agendamento);
    }

    @Test
    @DisplayName("Deve cancelar agendamento quando o cliente responde '2'")
    void deveCancelarAgendamentoComResposta2() {
        when(configRepository.findByInstanceName("nexipro-studio")).thenReturn(Optional.of(config));
        when(configRepository.findByEmpresaId(1L)).thenReturn(Optional.of(config));
        when(agendamentoRepository.findProximosAgendamentosPorEmpresa(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(agendamento));

        boolean processado = whatsAppService.processarRespostaCliente("5511999998888", "2", "nexipro-studio");

        assertTrue(processado);
        assertEquals("CANCELADO", agendamento.getStatus());
        verify(agendamentoRepository, times(1)).save(agendamento);
    }

    @Test
    @DisplayName("Não deve processar resposta para cliente sem agendamentos futuros")
    void naoDeveProcessarSemAgendamentos() {
        when(configRepository.findByInstanceName("nexipro-studio")).thenReturn(Optional.of(config));
        when(agendamentoRepository.findProximosAgendamentosPorEmpresa(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of());

        boolean processado = whatsAppService.processarRespostaCliente("5511977776666", "1", "nexipro-studio");

        assertFalse(processado);
        verify(agendamentoRepository, never()).save(any());
    }
}
