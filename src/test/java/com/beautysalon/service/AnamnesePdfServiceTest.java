package com.beautysalon.service;

import com.beautysalon.model.Cliente;
import com.beautysalon.model.ClienteAnamnese;
import com.beautysalon.model.Empresa;
import com.beautysalon.model.SegmentoEmpresa;
import com.beautysalon.repository.ClienteAnamneseRepository;
import com.beautysalon.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnamnesePdfServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ClienteAnamneseRepository anamneseRepository;

    @InjectMocks
    private AnamnesePdfService pdfService;

    private ClienteAnamnese anamneseMock;

    @BeforeEach
    void setUp() {
        TenantContext.setTenant("studio-vip", EMPRESA_ID, "Studio VIP");

        Empresa empresa = new Empresa();
        empresa.setId(EMPRESA_ID);
        empresa.setNome("Studio VIP Hair & Beauty");
        empresa.setTelefone("11999998888");

        Cliente cliente = new Cliente();
        cliente.setId(10L);
        cliente.setNome("Fernanda Lima");
        cliente.setTelefone("11988887777");
        cliente.setEmail("fernanda@teste.com");

        anamneseMock = ClienteAnamnese.builder()
                .id(100L)
                .empresa(empresa)
                .cliente(cliente)
                .procedimentoRealizado("Mechas Loiras & Matização")
                .formulaQuimica("50g Pó + 100ml OX 30vol")
                .historicoCapilarAlergias("Sem sensibilidade relatada")
                .observacoesTecnicas("Hidratação semanal recomendada")
                .dadosClinicosJson("{\"Elasticidade\":\"Normal\",\"Porosidade\":\"Média\"}")
                .segmento(SegmentoEmpresa.SALAO_BELEZA)
                .dataRegistro(LocalDateTime.now())
                .termoConsentimentoAceito(true)
                .build();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    @DisplayName("Deve gerar bytes válidos do PDF de prontuário e termo de consentimento")
    void deveGerarPdfProntuarioComSucesso() {
        when(anamneseRepository.findById(100L)).thenReturn(Optional.of(anamneseMock));

        byte[] pdfBytes = pdfService.gerarPdfProntuario(100L);

        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Assinatura de arquivo PDF (%PDF-)
        String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 5));
        assertEquals("%PDF-", header);
    }

    @Test
    @DisplayName("Deve lançar exceção se a anamnese pertencer a outro tenant")
    void deveBloquearAcessoAnamneseDeOutroTenant() {
        Empresa outraEmpresa = new Empresa();
        outraEmpresa.setId(999L);
        anamneseMock.setEmpresa(outraEmpresa);

        when(anamneseRepository.findById(100L)).thenReturn(Optional.of(anamneseMock));

        assertThrows(IllegalArgumentException.class, () -> pdfService.gerarPdfProntuario(100L));
    }
}
