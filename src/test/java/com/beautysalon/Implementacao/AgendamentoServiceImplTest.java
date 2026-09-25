package com.beautysalon.Implementacao;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.Servico;
import com.beautysalon.model.User;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.ServicoRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.WhatsAppService;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoServiceImplTest {

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WhatsAppService whatsAppService;

    @Mock
    private com.beautysalon.config.messaging.producer.EventMessageProducer eventMessageProducer;

    @InjectMocks
    private AgendamentoServiceImpl agendamentoService;

    private final Long EMPRESA_ID = 1L;
    private Empresa empresaMock;
    private Cliente clienteMock;
    private Servico servicoMock;
    private User profissionalMock;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);
        empresaMock = Empresa.builder().id(EMPRESA_ID).nome("Lumora Studio").build();
        clienteMock = Cliente.builder().id(10L).nome("Camila").telefone("11999998888").empresa(empresaMock).build();
        servicoMock = Servico.builder().id(20L).nome("Corte").preco(new BigDecimal("90.00")).duracaoMinutos(45).empresa(empresaMock).build();
        profissionalMock = User.builder().id(30L).nome("Keilla").empresa(empresaMock).build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve salvar agendamento sem conflitos com sucesso")
    void deveSalvarAgendamentoComSucesso() {
        LocalDateTime dataHora = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setClienteId(10L);
        dto.setServicoId(20L);
        dto.setProfissionalId(30L);
        dto.setDataHora(dataHora);

        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(clienteMock));
        when(servicoRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(servicoMock));
        when(userRepository.findByIdAndEmpresaId(30L, EMPRESA_ID)).thenReturn(Optional.of(profissionalMock));
        when(agendamentoRepository.findByEmpresaIdAndProfissionalIdOrderByDataHoraAsc(EMPRESA_ID, 30L)).thenReturn(List.of());
        when(agendamentoRepository.save(any(Agendamento.class))).thenAnswer(invocation -> {
            Agendamento ag = invocation.getArgument(0);
            ag.setId(1L);
            return ag;
        });

        AgendamentoDTO salvo = agendamentoService.salvar(dto);

        assertNotNull(salvo);
        assertEquals(1L, salvo.getId());
        assertEquals("AGENDADO", salvo.getStatus());
        assertEquals(45, salvo.getDuracaoMinutos());
        verify(agendamentoRepository, times(1)).save(any(Agendamento.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar agendar em horário conflitante para o mesmo profissional")
    void deveLancarExcecaoQuandoConflitoDeHorario() {
        LocalDateTime agExistenteDataHora = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);
        
        Agendamento agExistente = new Agendamento();
        agExistente.setId(99L);
        agExistente.setDataHora(agExistenteDataHora);
        agExistente.setStatus("AGENDADO");
        agExistente.setServicos(List.of(servicoMock)); // Duração 45 min -> vai até 14:45

        LocalDateTime novaDataHora = LocalDateTime.now().plusDays(1).withHour(14).withMinute(30); // Sobrepõe

        AgendamentoDTO dto = new AgendamentoDTO();
        dto.setClienteId(10L);
        dto.setServicoId(20L);
        dto.setProfissionalId(30L);
        dto.setDataHora(novaDataHora);

        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(clienteMock));
        when(servicoRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(servicoMock));
        when(agendamentoRepository.findByEmpresaIdAndProfissionalIdOrderByDataHoraAsc(EMPRESA_ID, 30L)).thenReturn(List.of(agExistente));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> agendamentoService.salvar(dto));

        assertTrue(ex.getMessage().contains("Conflito de agenda"));
        verify(agendamentoRepository, never()).save(any(Agendamento.class));
    }
}
