package com.beautysalon.service;

import com.beautysalon.DTO.ImportacaoResultadoDTO;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteImportacaoServiceTest {

    private static final Long EMPRESA_ID = 1L;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EmpresaRepository empresaRepository;

    @InjectMocks
    private ClienteImportacaoService importacaoService;

    private Empresa empresaMock;

    @BeforeEach
    void setUp() {
        empresaMock = Empresa.builder()
                .id(EMPRESA_ID)
                .nome("Studio Glow VIP")
                .slug("studio-glow-vip")
                .build();
    }

    @Test
    @DisplayName("Deve gerar modelo Excel (.xlsx) com cabeçalhos e exemplos válidos")
    void deveGerarModeloXlsxComSucesso() throws IOException {
        byte[] bytes = importacaoService.gerarModeloXlsx();

        assertNotNull(bytes);
        assertTrue(bytes.length > 0);

        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            Sheet sheet = wb.getSheet("Clientes");
            assertNotNull(sheet);
            Row headerRow = sheet.getRow(0);
            assertEquals("Nome Completo*", headerRow.getCell(0).getStringCellValue());
            assertEquals("WhatsApp / Celular", headerRow.getCell(1).getStringCellValue());
            assertEquals("E-mail", headerRow.getCell(2).getStringCellValue());
            assertTrue(sheet.getLastRowNum() >= 3, "Deveria ter pelo menos 3 linhas de exemplo");
        }
    }

    @Test
    @DisplayName("Deve gerar modelo CSV com cabeçalho delimitado por ponto-e-vírgula e exemplos")
    void deveGerarModeloCsvComSucesso() {
        byte[] bytes = importacaoService.gerarModeloCsv();

        assertNotNull(bytes);
        String conteudo = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(conteudo.contains("Nome Completo*;WhatsApp / Celular;E-mail"));
        assertTrue(conteudo.contains("Mariana Silva"));
    }

    @Test
    @DisplayName("Deve importar clientes com sucesso via CSV")
    void deveImportarClientesViaCsvComSucesso() throws Exception {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(clienteRepository.existsByTelefoneAndEmpresaId(any(), eq(EMPRESA_ID))).thenReturn(false);
        when(clienteRepository.existsByEmailAndEmpresaId(any(), eq(EMPRESA_ID))).thenReturn(false);

        String csvContent = "Nome Completo*;WhatsApp / Celular;E-mail;Data de Nascimento;Observações;Alergias\n" +
                "Larissa Manoela;11999990001;larissa@email.com;10/05/1998;Gosta de loiro;Nenhuma\n" +
                "Bruna Marquezine;11999990002;bruna@email.com;04/08/1995;Tratamento capilar;Sensibilidade";

        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "clientes.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacaoResultadoDTO resultado = importacaoService.importarClientes(file, EMPRESA_ID);

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalLinhasLidas());
        assertEquals(2, resultado.getTotalImportados());
        assertEquals(0, resultado.getTotalDuplicadosIgnorados());
        assertEquals(0, resultado.getTotalErros());
        verify(clienteRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve ignorar clientes com telefone ou e-mail duplicado durante importação CSV")
    void deveIgnorarClientesDuplicados() throws Exception {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        // Primeiro cliente não existe; segundo cliente existe por telefone
        when(clienteRepository.existsByTelefoneAndEmpresaId("11999990001", EMPRESA_ID)).thenReturn(false);
        when(clienteRepository.existsByTelefoneAndEmpresaId("11999990002", EMPRESA_ID)).thenReturn(true);

        String csvContent = "Nome Completo*;WhatsApp / Celular;E-mail;Data de Nascimento;Observações;Alergias\n" +
                "Cliente Novo;11999990001;novo@email.com;10/05/1998;;\n" +
                "Cliente Duplicado;11999990002;duplicado@email.com;04/08/1995;;";

        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "clientes.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacaoResultadoDTO resultado = importacaoService.importarClientes(file, EMPRESA_ID);

        assertNotNull(resultado);
        assertEquals(2, resultado.getTotalLinhasLidas());
        assertEquals(1, resultado.getTotalImportados());
        assertEquals(1, resultado.getTotalDuplicadosIgnorados());
        assertEquals(0, resultado.getTotalErros());
        assertFalse(resultado.getAlertas().isEmpty());
        verify(clienteRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve registrar erro quando linha do arquivo não tiver nome preenchido")
    void deveRegistrarErroQuandoNomeVazio() throws Exception {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));

        String csvContent = "Nome Completo*;WhatsApp / Celular;E-mail;Data de Nascimento;Observações;Alergias\n" +
                ";11999990001;semnome@email.com;;;\n";

        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "clientes.csv",
                "text/csv",
                csvContent.getBytes(StandardCharsets.UTF_8)
        );

        ImportacaoResultadoDTO resultado = importacaoService.importarClientes(file, EMPRESA_ID);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalLinhasLidas());
        assertEquals(0, resultado.getTotalImportados());
        assertEquals(1, resultado.getTotalErros());
        verify(clienteRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve importar clientes com sucesso via planilha Excel (.xlsx)")
    void deveImportarClientesViaExcelComSucesso() throws Exception {
        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(empresaMock));
        when(clienteRepository.existsByTelefoneAndEmpresaId(any(), eq(EMPRESA_ID))).thenReturn(false);
        when(clienteRepository.existsByEmailAndEmpresaId(any(), eq(EMPRESA_ID))).thenReturn(false);

        byte[] xlsxBytes;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("Clientes");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nome Completo*");
            header.createCell(1).setCellValue("WhatsApp / Celular");
            header.createCell(2).setCellValue("E-mail");

            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Ana Paula");
            r1.createCell(1).setCellValue("(11) 98888-7777");
            r1.createCell(2).setCellValue("ana@email.com");

            wb.write(out);
            xlsxBytes = out.toByteArray();
        }

        MockMultipartFile file = new MockMultipartFile(
                "arquivo",
                "clientes.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                xlsxBytes
        );

        ImportacaoResultadoDTO resultado = importacaoService.importarClientes(file, EMPRESA_ID);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalLinhasLidas());
        assertEquals(1, resultado.getTotalImportados());
        verify(clienteRepository, times(1)).saveAll(anyList());
    }

    @Test
    @DisplayName("Deve lançar exceção se arquivo for nulo ou vazio")
    void deveLancarExcecaoArquivoVazio() {
        MockMultipartFile fileVazio = new MockMultipartFile("arquivo", "vazio.xlsx", "text/plain", new byte[0]);

        assertThrows(IllegalArgumentException.class, () ->
                importacaoService.importarClientes(fileVazio, EMPRESA_ID)
        );
        assertThrows(IllegalArgumentException.class, () ->
                importacaoService.importarClientes(null, EMPRESA_ID)
        );
    }
}
