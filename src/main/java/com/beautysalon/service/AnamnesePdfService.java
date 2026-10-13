package com.beautysalon.service;

import com.beautysalon.model.ClienteAnamnese;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteAnamneseRepository;
import com.beautysalon.tenant.TenantContext;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class AnamnesePdfService {

    private final ClienteAnamneseRepository anamneseRepository;

    public AnamnesePdfService(ClienteAnamneseRepository anamneseRepository) {
        this.anamneseRepository = anamneseRepository;
    }

    @Transactional(readOnly = true)
    public byte[] gerarPdfProntuario(Long anamneseId) {
        Long empresaId = TenantContext.getEmpresaId();
        ClienteAnamnese anamnese = anamneseRepository.findById(anamneseId)
                .filter(a -> a.getEmpresa() != null && a.getEmpresa().getId().equals(empresaId))
                .orElseThrow(() -> new IllegalArgumentException("Ficha de anamnese não encontrada ou acesso negado."));

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Fontes
            Font fontHeaderTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(184, 134, 11)); // Ouro
            Font fontSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            Font fontSectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42)); // Azul Escuro
            Font fontBodyBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fontBody = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(51, 65, 85));
            Font fontLegal = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

            Empresa empresa = anamnese.getEmpresa();

            // 1. Cabeçalho da Empresa
            Paragraph header = new Paragraph();
            header.setAlignment(Element.ALIGN_CENTER);
            header.add(new Chunk(empresa != null ? empresa.getNome().toUpperCase() : "BEAUTY SALON", fontHeaderTitle));
            header.add(Chunk.NEWLINE);
            header.add(new Chunk("PRONTUÁRIO TÉCNICO & TERMO DE CONSENTIMENTO LIVRE E ESCLARECIDO", fontSubtitle));
            header.add(Chunk.NEWLINE);
            if (empresa != null && empresa.getTelefone() != null) {
                header.add(new Chunk("Contato: " + empresa.getTelefone() + (empresa.getEmail() != null ? " | " + empresa.getEmail() : ""), fontSubtitle));
                header.add(Chunk.NEWLINE);
            }
            document.add(header);

            LineSeparator ls = new LineSeparator();
            ls.setLineColor(new Color(226, 232, 240));
            document.add(new Paragraph(" "));
            document.add(ls);
            document.add(new Paragraph(" "));

            // 2. Tabela de Identificação do Cliente e Registro
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{60, 40});

            PdfPCell cellCliente = new PdfPCell();
            cellCliente.setBorder(Rectangle.NO_BORDER);
            cellCliente.addElement(new Paragraph("Cliente: " + anamnese.getCliente().getNome(), fontBodyBold));
            cellCliente.addElement(new Paragraph("Telefone: " + (anamnese.getCliente().getTelefone() != null ? anamnese.getCliente().getTelefone() : "Não informado"), fontBody));
            cellCliente.addElement(new Paragraph("E-mail: " + (anamnese.getCliente().getEmail() != null ? anamnese.getCliente().getEmail() : "Não informado"), fontBody));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            PdfPCell cellData = new PdfPCell();
            cellData.setBorder(Rectangle.NO_BORDER);
            cellData.addElement(new Paragraph("Data do Registro: " + (anamnese.getDataRegistro() != null ? anamnese.getDataRegistro().format(dtf) : "-"), fontBody));
            cellData.addElement(new Paragraph("Profissional: " + (anamnese.getProfissional() != null ? anamnese.getProfissional().getNome() : "Não especificado"), fontBody));
            cellData.addElement(new Paragraph("Segmento: " + (anamnese.getSegmento() != null ? anamnese.getSegmento().getDescricao() : "Geral"), fontBody));

            infoTable.addCell(cellCliente);
            infoTable.addCell(cellData);
            document.add(infoTable);

            document.add(new Paragraph(" "));

            // 3. Detalhes do Procedimento e Fórmulas
            Paragraph secProc = new Paragraph("1. Procedimento Realizado & Protocolo Técnico", fontSectionTitle);
            document.add(secProc);

            PdfPTable tableProc = new PdfPTable(1);
            tableProc.setWidthPercentage(100);
            tableProc.setSpacingBefore(5);

            PdfPCell cellProc = new PdfPCell();
            cellProc.setBackgroundColor(new Color(248, 250, 252));
            cellProc.setPadding(8);
            cellProc.setBorderColor(new Color(203, 213, 225));

            cellProc.addElement(new Paragraph("Procedimento: " + anamnese.getProcedimentoRealizado(), fontBodyBold));
            if (anamnese.getFormulaQuimica() != null && !anamnese.getFormulaQuimica().isBlank()) {
                cellProc.addElement(new Paragraph("Produtos / Fórmulas / Parâmetros:", fontBodyBold));
                cellProc.addElement(new Paragraph(anamnese.getFormulaQuimica(), fontBody));
            }
            tableProc.addCell(cellProc);
            document.add(tableProc);

            document.add(new Paragraph(" "));

            // 4. Avaliação Clínica Estruturada (se houver)
            if (anamnese.getDadosClinicosJson() != null && !anamnese.getDadosClinicosJson().isBlank()) {
                Paragraph secClin = new Paragraph("2. Parâmetros da Avaliação Técnica Especializada", fontSectionTitle);
                document.add(secClin);

                PdfPTable tableClin = new PdfPTable(2);
                tableClin.setWidthPercentage(100);
                tableClin.setSpacingBefore(5);

                try {
                    JSONObject json = new JSONObject(anamnese.getDadosClinicosJson());
                    for (String key : json.keySet()) {
                        String val = json.optString(key);
                        if (val != null && !val.isBlank()) {
                            PdfPCell cKey = new PdfPCell(new Phrase(key, fontBodyBold));
                            cKey.setBackgroundColor(new Color(241, 245, 249));
                            cKey.setPadding(5);
                            cKey.setBorderColor(new Color(226, 232, 240));

                            PdfPCell cVal = new PdfPCell(new Phrase(val, fontBody));
                            cVal.setPadding(5);
                            cVal.setBorderColor(new Color(226, 232, 240));

                            tableClin.addCell(cKey);
                            tableClin.addCell(cVal);
                        }
                    }
                    document.add(tableClin);
                } catch (Exception ignored) {
                }
                document.add(new Paragraph(" "));
            }

            // 5. Histórico de Alergias & Observações
            if ((anamnese.getHistoricoCapilarAlergias() != null && !anamnese.getHistoricoCapilarAlergias().isBlank())
                    || (anamnese.getObservacoesTecnicas() != null && !anamnese.getObservacoesTecnicas().isBlank())) {

                Paragraph secObs = new Paragraph("3. Histórico de Sensibilidade & Recomendações Técnicas", fontSectionTitle);
                document.add(secObs);

                Paragraph pObs = new Paragraph();
                pObs.setSpacingBefore(4);
                if (anamnese.getHistoricoCapilarAlergias() != null && !anamnese.getHistoricoCapilarAlergias().isBlank()) {
                    pObs.add(new Chunk("Sensibilidade / Testes Prévios / Alergias: ", fontBodyBold));
                    pObs.add(new Chunk(anamnese.getHistoricoCapilarAlergias() + "\n", fontBody));
                }
                if (anamnese.getObservacoesTecnicas() != null && !anamnese.getObservacoesTecnicas().isBlank()) {
                    pObs.add(new Chunk("Observações e Orientações Pós-Procedimento: ", fontBodyBold));
                    pObs.add(new Chunk(anamnese.getObservacoesTecnicas(), fontBody));
                }
                document.add(pObs);
                document.add(new Paragraph(" "));
            }

            // 6. Termo de Consentimento Livre e Esclarecido (Jurídico / LGPD)
            Paragraph secTermo = new Paragraph("4. Termo de Responsabilidade e Consentimento Informado", fontSectionTitle);
            document.add(secTermo);

            String termoTexto = "Declaro que todas as informações prestadas por mim nesta ficha de anamnese são verdadeiras e que não omiti " +
                    "nenhum dado sobre minha saúde, tratamentos anteriores ou sensibilidades. Fui previamente informado(a) sobre a natureza do " +
                    "procedimento, possíveis reações esperadas e os cuidados obrigatórios de manutenção pós-procedimento. " +
                    "Autorizo a realização do procedimento acima especificado sob os cuidados dos profissionais deste estabelecimento.";

            Paragraph pTermo = new Paragraph(termoTexto, fontLegal);
            pTermo.setAlignment(Element.ALIGN_JUSTIFIED);
            pTermo.setSpacingBefore(4);
            document.add(pTermo);

            document.add(new Paragraph(" "));

            // 7. Bloco de Assinatura Digital
            PdfPTable tableSign = new PdfPTable(2);
            tableSign.setWidthPercentage(100);
            tableSign.setSpacingBefore(15);

            // Assinatura do Cliente
            PdfPCell cellSignCliente = new PdfPCell();
            cellSignCliente.setBorder(Rectangle.NO_BORDER);
            cellSignCliente.setHorizontalAlignment(Element.ALIGN_CENTER);

            if (anamnese.getAssinaturaDigitalBase64() != null && anamnese.getAssinaturaDigitalBase64().contains("base64,")) {
                try {
                    String base64Data = anamnese.getAssinaturaDigitalBase64().split("base64,")[1];
                    byte[] imgBytes = Base64.getDecoder().decode(base64Data);
                    Image signImg = Image.getInstance(imgBytes);
                    signImg.scaleToFit(140, 45);
                    signImg.setAlignment(Element.ALIGN_CENTER);
                    cellSignCliente.addElement(signImg);
                } catch (Exception ignored) {
                }
            }

            Paragraph pNomeCli = new Paragraph("_____________________________________\n" + anamnese.getCliente().getNome() + "\n(Cliente)", fontBody);
            pNomeCli.setAlignment(Element.ALIGN_CENTER);
            cellSignCliente.addElement(pNomeCli);

            // Assinatura do Profissional
            PdfPCell cellSignProf = new PdfPCell();
            cellSignProf.setBorder(Rectangle.NO_BORDER);
            cellSignProf.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph pNomeProf = new Paragraph("\n\n_____________________________________\n" +
                    (anamnese.getProfissional() != null ? anamnese.getProfissional().getNome() : "Profissional Responsável") +
                    "\n(Responsável Técnico)", fontBody);
            pNomeProf.setAlignment(Element.ALIGN_CENTER);
            cellSignProf.addElement(pNomeProf);

            tableSign.addCell(cellSignCliente);
            tableSign.addCell(cellSignProf);
            document.add(tableSign);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF da ficha de anamnese: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }
}
