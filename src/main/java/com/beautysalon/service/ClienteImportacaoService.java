package com.beautysalon.service;

import com.beautysalon.DTO.ImportacaoResultadoDTO;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class ClienteImportacaoService {

    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    public ClienteImportacaoService(ClienteRepository clienteRepository, EmpresaRepository empresaRepository) {
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    /**
     * Gera planilha modelo em formato Excel (.xlsx) com cabeçalhos e exemplos didáticos.
     */
    public byte[] gerarModeloXlsx() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // Estilo do Cabeçalho
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Cabeçalhos
            String[] headers = {
                    "Nome Completo*",
                    "WhatsApp / Celular",
                    "E-mail",
                    "Data de Nascimento",
                    "Observações / Preferências",
                    "Alergias / Restrições"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Linhas de Exemplo
            String[][] exemplos = {
                    {"Mariana Silva", "(11) 98765-4321", "mariana.silva@email.com", "15/04/1992", "Prefere atendimento aos sábados", "Nenhuma"},
                    {"Carlos Eduardo", "(11) 91234-5678", "carlos.eduardo@email.com", "20/08/1988", "Corte degradê navalhado", "Sensibilidade a lâmina"},
                    {"Juliana Mendes", "(11) 99887-1122", "juliana.mendes@email.com", "03/11/1995", "Micropigmentação e Lash", "Alérgica a amônia"}
            };

            int rowIdx = 1;
            for (String[] ex : exemplos) {
                Row row = sheet.createRow(rowIdx++);
                for (int c = 0; c < ex.length; c++) {
                    row.createCell(c).setCellValue(ex[c]);
                }
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /**
     * Gera planilha modelo em formato CSV (separador ponto-e-vírgula com BOM UTF-8).
     */
    public byte[] gerarModeloCsv() {
        StringBuilder csv = new StringBuilder();
        // BOM UTF-8 para abrir com acentuação correta no Microsoft Excel
        csv.append('\ufeff');
        csv.append("Nome Completo*;WhatsApp / Celular;E-mail;Data de Nascimento;Observações / Preferências;Alergias / Restrições\r\n");
        csv.append("Mariana Silva;(11) 98765-4321;mariana.silva@email.com;15/04/1992;Prefere atendimento aos sábados;Nenhuma\r\n");
        csv.append("Carlos Eduardo;(11) 91234-5678;carlos.eduardo@email.com;20/08/1988;Corte degradê navalhado;Sensibilidade a lâmina\r\n");
        csv.append("Juliana Mendes;(11) 99887-1122;juliana.mendes@email.com;03/11/1995;Micropigmentação e Lash;Alérgica a amônia\r\n");
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Processa a importação de clientes a partir de arquivo Excel (.xlsx) ou CSV.
     */
    @Transactional
    public ImportacaoResultadoDTO importarClientes(MultipartFile arquivo, Long empresaId) throws Exception {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Nenhum arquivo enviado para importação.");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa não encontrada para o ID: " + empresaId));

        String nomeOriginal = Optional.ofNullable(arquivo.getOriginalFilename()).orElse("").toLowerCase();
        List<Cliente> clientesParaSalvar = new ArrayList<>();
        ImportacaoResultadoDTO resultado = ImportacaoResultadoDTO.builder().build();

        Set<String> telefonesVistos = new HashSet<>();
        Set<String> emailsVistos = new HashSet<>();

        if (nomeOriginal.endsWith(".xlsx") || nomeOriginal.endsWith(".xls")) {
            processarExcel(arquivo.getInputStream(), empresa, clientesParaSalvar, telefonesVistos, emailsVistos, resultado);
        } else if (nomeOriginal.endsWith(".csv") || nomeOriginal.endsWith(".txt")) {
            processarCsv(arquivo.getInputStream(), empresa, clientesParaSalvar, telefonesVistos, emailsVistos, resultado);
        } else {
            // Tenta como Excel, se falhar tenta como CSV
            try {
                processarExcel(arquivo.getInputStream(), empresa, clientesParaSalvar, telefonesVistos, emailsVistos, resultado);
            } catch (Exception e) {
                processarCsv(arquivo.getInputStream(), empresa, clientesParaSalvar, telefonesVistos, emailsVistos, resultado);
            }
        }

        if (!clientesParaSalvar.isEmpty()) {
            clienteRepository.saveAll(clientesParaSalvar);
            resultado.setTotalImportados(clientesParaSalvar.size());
        }

        log.info("Importação de clientes finalizada para empresa ID {}: {} importados, {} duplicados ignorados, {} erros.",
                empresaId, resultado.getTotalImportados(), resultado.getTotalDuplicadosIgnorados(), resultado.getTotalErros());

        return resultado;
    }

    private void processarExcel(InputStream is, Empresa empresa, List<Cliente> destino,
                                Set<String> telefonesVistos, Set<String> emailsVistos,
                                ImportacaoResultadoDTO resultado) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalLinhas = sheet.getLastRowNum();
            resultado.setTotalLinhasLidas(Math.max(0, totalLinhas));

            // Linha 0 é cabeçalho
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nome = getCellValue(row.getCell(0));
                if (nome == null || nome.isBlank()) {
                    resultado.setTotalErros(resultado.getTotalErros() + 1);
                    resultado.getAlertas().add("Linha " + (i + 1) + ": Nome do cliente não informado.");
                    continue;
                }

                String telefone = getCellValue(row.getCell(1));
                String email = getCellValue(row.getCell(2));
                LocalDate dataNasc = parseDateCell(row.getCell(3));
                String observacoes = getCellValue(row.getCell(4));
                String alergias = getCellValue(row.getCell(5));

                criarEValidarCliente(empresa, nome, telefone, email, dataNasc, observacoes, alergias,
                        destino, telefonesVistos, emailsVistos, resultado, i + 1);
            }
        }
    }

    private void processarCsv(InputStream is, Empresa empresa, List<Cliente> destino,
                              Set<String> telefonesVistos, Set<String> emailsVistos,
                              ImportacaoResultadoDTO resultado) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String header = reader.readLine();
            if (header == null) return;

            // Detectar delimitador (';' ou ',')
            String delimitador = header.contains(";") ? ";" : ",";

            String linha;
            int numLinha = 1;
            while ((linha = reader.readLine()) != null) {
                numLinha++;
                if (linha.trim().isEmpty()) continue;
                resultado.setTotalLinhasLidas(resultado.getTotalLinhasLidas() + 1);

                String[] colunas = linha.split(delimitador, -1);
                String nome = colunas.length > 0 ? colunas[0].trim().replace("\"", "") : "";
                if (nome.isBlank()) {
                    resultado.setTotalErros(resultado.getTotalErros() + 1);
                    resultado.getAlertas().add("Linha " + numLinha + ": Nome não informado.");
                    continue;
                }

                String telefone = colunas.length > 1 ? colunas[1].trim().replace("\"", "") : "";
                String email = colunas.length > 2 ? colunas[2].trim().replace("\"", "") : "";
                LocalDate dataNasc = colunas.length > 3 ? parseLocalDate(colunas[3].trim().replace("\"", "")) : null;
                String observacoes = colunas.length > 4 ? colunas[4].trim().replace("\"", "") : "";
                String alergias = colunas.length > 5 ? colunas[5].trim().replace("\"", "") : "";

                criarEValidarCliente(empresa, nome, telefone, email, dataNasc, observacoes, alergias,
                        destino, telefonesVistos, emailsVistos, resultado, numLinha);
            }
        }
    }

    private void criarEValidarCliente(Empresa empresa, String nome, String telefone, String email,
                                      LocalDate dataNascimento, String observacoes, String alergias,
                                      List<Cliente> destino, Set<String> telefonesVistos, Set<String> emailsVistos,
                                      ImportacaoResultadoDTO resultado, int linhaNum) {
        String telSanitizado = (telefone != null && !telefone.isBlank()) ? telefone.trim() : null;
        String emailSanitizado = (email != null && !email.isBlank()) ? email.trim().toLowerCase() : null;

        // Verificar duplicidade por telefone no banco ou no próprio lote
        if (telSanitizado != null) {
            String telLimpo = telSanitizado.replaceAll("\\D", "");
            if (!telLimpo.isEmpty() && (telefonesVistos.contains(telLimpo) || clienteRepository.existsByTelefoneAndEmpresaId(telSanitizado, empresa.getId()))) {
                resultado.setTotalDuplicadosIgnorados(resultado.getTotalDuplicadosIgnorados() + 1);
                resultado.getAlertas().add("Linha " + linhaNum + ": Cliente '" + nome + "' com telefone já existente ignorado.");
                return;
            }
            if (!telLimpo.isEmpty()) {
                telefonesVistos.add(telLimpo);
            }
        }

        // Verificar duplicidade por e-mail
        if (emailSanitizado != null) {
            if (emailsVistos.contains(emailSanitizado) || clienteRepository.existsByEmailAndEmpresaId(emailSanitizado, empresa.getId())) {
                resultado.setTotalDuplicadosIgnorados(resultado.getTotalDuplicadosIgnorados() + 1);
                resultado.getAlertas().add("Linha " + linhaNum + ": Cliente '" + nome + "' com e-mail já existente ignorado.");
                return;
            }
            emailsVistos.add(emailSanitizado);
        }

        Cliente cliente = Cliente.builder()
                .nome(nome.trim())
                .telefone(telSanitizado)
                .email(emailSanitizado)
                .dataNascimento(dataNascimento)
                .observacoesTecnicas(observacoes != null && !observacoes.isBlank() ? observacoes.trim() : null)
                .alergias(alergias != null && !alergias.isBlank() ? alergias.trim() : null)
                .empresa(empresa)
                .build();

        destino.add(cliente);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                }
                double num = cell.getNumericCellValue();
                if (num == (long) num) {
                    yield String.valueOf((long) num);
                }
                yield String.valueOf(num);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private LocalDate parseDateCell(Cell cell) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String texto = getCellValue(cell);
        return parseLocalDate(texto);
    }

    private LocalDate parseLocalDate(String texto) {
        if (texto == null || texto.isBlank()) return null;
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(texto.trim(), fmt);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
