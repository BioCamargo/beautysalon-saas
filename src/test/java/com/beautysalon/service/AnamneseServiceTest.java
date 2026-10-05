package com.beautysalon.service;

import com.beautysalon.model.Cliente;
import com.beautysalon.model.ClienteAnamnese;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.User;
import com.beautysalon.repository.ClienteAnamneseRepository;
import com.beautysalon.repository.ClienteRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnamneseServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ClienteAnamneseRepository anamneseRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AnamneseService anamneseService;

    private Empresa empresaMock;
    private Cliente clienteMock;
    private User profissionalMock;

    @BeforeEach
    void setUp() {
        TenantContext.setEmpresaId(EMPRESA_ID);

        empresaMock = Empresa.builder().id(EMPRESA_ID).nome("Studio Beauty").slug("studio-beauty").build();
        clienteMock = Cliente.builder().id(10L).nome("Camila Silva").empresa(empresaMock).build();
        profissionalMock = User.builder().id(20L).nome("Dra. Beatriz").empresa(empresaMock).build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve buscar histórico de anamnese por cliente filtrando pelo tenant atual")
    void deveBuscarHistoricoPorCliente() {
        ClienteAnamnese a1 = ClienteAnamnese.builder().id(1L).cliente(clienteMock).procedimentoRealizado("Microblading").build();
        ClienteAnamnese a2 = ClienteAnamnese.builder().id(2L).cliente(clienteMock).procedimentoRealizado("Peeling Químico").build();

        when(anamneseRepository.findByClienteIdAndEmpresaIdOrderByDataRegistroDesc(10L, EMPRESA_ID))
                .thenReturn(List.of(a2, a1));

        List<ClienteAnamnese> historico = anamneseService.buscarHistoricoPorCliente(10L);

        assertEquals(2, historico.size());
        assertEquals("Peeling Químico", historico.get(0).getProcedimentoRealizado());
        verify(anamneseRepository, times(1)).findByClienteIdAndEmpresaIdOrderByDataRegistroDesc(10L, EMPRESA_ID);
    }

    @Test
    @DisplayName("Deve salvar anamnese com sucesso com fotos e assinatura digital")
    void deveSalvarAnamneseComSucesso() {
        when(clienteRepository.findByIdAndEmpresaId(10L, EMPRESA_ID)).thenReturn(Optional.of(clienteMock));
        when(userRepository.findByIdAndEmpresaId(20L, EMPRESA_ID)).thenReturn(Optional.of(profissionalMock));
        when(anamneseRepository.save(any(ClienteAnamnese.class))).thenAnswer(invocation -> {
            ClienteAnamnese arg = invocation.getArgument(0);
            arg.setId(100L);
            return arg;
        });

        ClienteAnamnese salva = anamneseService.salvarAnamnese(
                10L,
                "Lash Lifting",
                "Solução Permanente Step 1 e Neutralizante Step 2",
                "Sem alergias conhecidas",
                "Cílios curvados com molde M",
                "https://cdn.studio.com/antes.jpg",
                "https://cdn.studio.com/depois.jpg",
                "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
                20L
        );

        assertNotNull(salva);
        assertEquals(100L, salva.getId());
        assertEquals("Lash Lifting", salva.getProcedimentoRealizado());
        assertEquals(clienteMock, salva.getCliente());
        assertEquals(empresaMock, salva.getEmpresa());
        assertEquals(profissionalMock, salva.getProfissional());
        assertTrue(salva.isTermoConsentimentoAceito());
        assertNotNull(salva.getDataRegistro());

        verify(anamneseRepository, times(1)).save(any(ClienteAnamnese.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar salvar anamnese para cliente de outro tenant ou inexistente")
    void deveLancarExcecaoClienteInexistente() {
        when(clienteRepository.findByIdAndEmpresaId(999L, EMPRESA_ID)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                anamneseService.salvarAnamnese(
                        999L,
                        "Procedimento Teste",
                        null, null, null, null, null, null, null
                )
        );

        assertTrue(ex.getMessage().contains("Cliente não encontrado para esta empresa"));
        verify(anamneseRepository, never()).save(any());
    }
}
