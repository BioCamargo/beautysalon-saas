package com.beautysalon.Controller;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.DTO.ServicoDTO;
import com.beautysalon.DTO.UserDTO;
import com.beautysalon.Inteface.AgendamentoService;
import com.beautysalon.Inteface.ServicoService;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.service.ProfissionalService;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgendamentoPublicoControllerTest {

    private static final Long EMPRESA_ID = 1L;
    private static final String SLUG = "studio-vip";

    @Mock
    private ServicoService servicoService;

    @Mock
    private ProfissionalService profissionalService;

    @Mock
    private AgendamentoService agendamentoService;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private AgendamentoPublicoController controller;

    private Empresa empresaMock;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant(SLUG, EMPRESA_ID, "Studio VIP");
        empresaMock = new Empresa();
        empresaMock.setId(EMPRESA_ID);
        empresaMock.setNome("Studio VIP");
        empresaMock.setSlug(SLUG);
        empresaMock.setAtivo(true);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve renderizar página pública de agendamento com serviços e profissionais")
    void deveRenderizarPaginaAgendamento() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(servicoService.listarTodos()).thenReturn(Collections.emptyList());
        when(profissionalService.listarProfissionaisAtivos()).thenReturn(Collections.emptyList());

        Model model = new ConcurrentModel();
        String view = controller.paginaAgendamento(SLUG, model);

        assertEquals("publico/agendamento", view);
        assertEquals(empresaMock, model.getAttribute("empresa"));
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
        assertTrue(model.containsAttribute("servicos"));
        assertTrue(model.containsAttribute("profissionais"));
    }

    @Test
    @DisplayName("Deve confirmar agendamento público e criar cliente novo se não existir")
    void deveConfirmarAgendamentoCriandoNovoCliente() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(clienteRepository.findFirstByTelefoneAndEmpresaId("11999998888", EMPRESA_ID)).thenReturn(Optional.empty());
        when(clienteRepository.findFirstByTelefoneAndEmpresaId("11999998888", EMPRESA_ID)).thenReturn(Optional.empty());

        Cliente novoCliente = new Cliente();
        novoCliente.setId(10L);
        novoCliente.setNome("Maria Silva");
        novoCliente.setTelefone("11999998888");
        novoCliente.setEmpresa(empresaMock);

        when(clienteRepository.save(any(Cliente.class))).thenReturn(novoCliente);

        ServicoDTO servicoDTO = new ServicoDTO();
        servicoDTO.setId(5L);
        servicoDTO.setNome("Corte & Escova");
        servicoDTO.setPreco(new BigDecimal("120.00"));
        when(servicoService.buscarPorId(5L)).thenReturn(servicoDTO);

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        String view = controller.confirmarAgendamento(
                SLUG,
                "Maria Silva",
                "11999998888",
                "maria@exemplo.com",
                5L,
                2L,
                LocalDate.now().plusDays(1),
                "14:00",
                "Prefiro corte em camadas",
                redirectAttributes
        );

        assertEquals("redirect:/studio-vip/agendar/sucesso", view);
        verify(agendamentoService, times(1)).salvar(any(AgendamentoDTO.class));
        assertEquals(true, redirectAttributes.getFlashAttributes().get("sucesso"));
        assertEquals("Maria Silva", redirectAttributes.getFlashAttributes().get("clienteNome"));
        assertEquals("Corte & Escova", redirectAttributes.getFlashAttributes().get("servicoNome"));
    }

    @Test
    @DisplayName("Deve retornar horários ocupados para a data e profissional selecionados")
    void deveRetornarHorariosOcupados() {
        LocalDate data = LocalDate.of(2026, 10, 20);

        AgendamentoDTO a1 = new AgendamentoDTO();
        a1.setProfissionalId(2L);
        a1.setDataHora(LocalDateTime.of(data, LocalTime.of(10, 0)));
        a1.setStatus("AGENDADO");

        AgendamentoDTO a2 = new AgendamentoDTO();
        a2.setProfissionalId(2L);
        a2.setDataHora(LocalDateTime.of(data, LocalTime.of(15, 30)));
        a2.setStatus("CONFIRMADO");

        AgendamentoDTO aCancelado = new AgendamentoDTO();
        aCancelado.setProfissionalId(2L);
        aCancelado.setDataHora(LocalDateTime.of(data, LocalTime.of(11, 0)));
        aCancelado.setStatus("CANCELADO");

        when(agendamentoService.listarTodos()).thenReturn(List.of(a1, a2, aCancelado));

        List<String> horarios = controller.obterHorariosOcupados(SLUG, 2L, data);

        assertEquals(2, horarios.size());
        assertTrue(horarios.contains("10:00"));
        assertTrue(horarios.contains("15:30"));
        assertFalse(horarios.contains("11:00"));
    }

    @Test
    @DisplayName("Deve renderizar página de sucesso de agendamento")
    void deveRenderizarPaginaSucesso() {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));

        Model model = new ConcurrentModel();
        String view = controller.sucessoAgendamento(SLUG, model);

        assertEquals("publico/sucesso", view);
        assertEquals(empresaMock, model.getAttribute("empresa"));
        assertEquals(SLUG, model.getAttribute("empresaSlug"));
    }
}
