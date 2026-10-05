package com.beautysalon.service;

import com.beautysalon.DTO.ClienteResgateDTO;
import com.beautysalon.DTO.OciosidadeAgendaDTO;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Servico;
import com.beautysalon.model.User;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InteligenciaNegocioServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private InteligenciaNegocioService inteligenciaService;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve identificar cliente em risco de churn quando ultrapassar o ciclo de retorno do serviço")
    void deveIdentificarClientesParaResgate() {
        Cliente cliente = Cliente.builder().id(10L).nome("Fernanda Souza").telefone("11999998888").build();
        Servico corte = Servico.builder().id(5L).nome("Corte Bordado").diasCicloRetorno(30).build();
        User profissional = User.builder().id(2L).nome("Lucas Cabeleireiro").build();

        // Atendimento ocorrido há 45 dias (atraso de 15 dias além do ciclo de 30)
        Agendamento agendamentoPassado = Agendamento.builder()
                .id(101L)
                .dataHora(LocalDateTime.now().minusDays(45))
                .status("CONCLUIDO")
                .servicos(List.of(corte))
                .profissional(profissional)
                .build();

        when(clienteRepository.findAllByEmpresaId(EMPRESA_ID)).thenReturn(List.of(cliente));
        when(agendamentoRepository.findByEmpresaIdAndClienteIdOrderByDataHoraDesc(EMPRESA_ID, 10L))
                .thenReturn(List.of(agendamentoPassado));

        List<ClienteResgateDTO> resgates = inteligenciaService.identificarClientesParaResgate();

        assertNotNull(resgates);
        assertEquals(1, resgates.size());
        ClienteResgateDTO dto = resgates.get(0);
        assertEquals(10L, dto.getClienteId());
        assertEquals("Fernanda Souza", dto.getNomeCliente());
        assertEquals("Corte Bordado", dto.getUltimoServicoNome());
        assertEquals(30, dto.getCicloIdealDias());
        assertTrue(dto.getDiasAtraso() >= 14);
        assertNotNull(dto.getMensagemSugeridaWhatsapp());
        assertTrue(dto.getMensagemSugeridaWhatsapp().contains("Fernanda Souza"));
    }

    @Test
    @DisplayName("Não deve sugerir resgate se cliente já possui agendamento futuro marcado")
    void naoDeveSugerirResgateSePossuiAgendamentoFuturo() {
        Cliente cliente = Cliente.builder().id(11L).nome("Juliana").build();
        Servico corte = Servico.builder().id(5L).nome("Corte").diasCicloRetorno(20).build();

        Agendamento passado = Agendamento.builder()
                .id(102L)
                .dataHora(LocalDateTime.now().minusDays(60))
                .status("CONCLUIDO")
                .servicos(List.of(corte))
                .build();

        Agendamento futuro = Agendamento.builder()
                .id(103L)
                .dataHora(LocalDateTime.now().plusDays(2))
                .status("AGENDADO")
                .servicos(List.of(corte))
                .build();

        when(clienteRepository.findAllByEmpresaId(EMPRESA_ID)).thenReturn(List.of(cliente));
        when(agendamentoRepository.findByEmpresaIdAndClienteIdOrderByDataHoraDesc(EMPRESA_ID, 11L))
                .thenReturn(List.of(futuro, passado));

        List<ClienteResgateDTO> resgates = inteligenciaService.identificarClientesParaResgate();

        assertTrue(resgates.isEmpty());
    }

    @Test
    @DisplayName("Deve analisar ociosidade dos próximos 7 dias e apontar oportunidades de promoção")
    void deveAnalisarOciosidadeProximosDias() {
        when(agendamentoRepository.findByEmpresaIdAndDataHoraBetween(eq(EMPRESA_ID), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        List<OciosidadeAgendaDTO> relatorio = inteligenciaService.analisarOciosidadeProximosDias();

        assertNotNull(relatorio);
        assertEquals(7, relatorio.size());
        OciosidadeAgendaDTO primeiroDia = relatorio.get(0);
        assertEquals(0, primeiroDia.getTotalAgendamentosMarcados());
        assertEquals(0.0, primeiroDia.getTaxaOcupacaoPercentual());
        assertTrue(primeiroDia.isOportunidadePromocao());
        assertFalse(primeiroDia.getHorariosVagos().isEmpty());
    }
}
