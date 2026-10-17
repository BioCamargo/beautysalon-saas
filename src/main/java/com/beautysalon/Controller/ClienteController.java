package com.beautysalon.Controller;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.Inteface.ClienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/{slug}/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final com.beautysalon.service.AnamneseService anamneseService;
    private final com.beautysalon.service.AnamnesePdfService anamnesePdfService;
    private final com.beautysalon.service.AnamneseFormBuilderService anamneseFormBuilderService;
    private final com.beautysalon.repository.UserRepository userRepository;
    private final com.beautysalon.repository.ClienteRepository clienteRepository;
    private final com.beautysalon.service.ClienteImportacaoService clienteImportacaoService;

    public ClienteController(ClienteService clienteService,
                             com.beautysalon.service.AnamneseService anamneseService,
                             com.beautysalon.service.AnamnesePdfService anamnesePdfService,
                             com.beautysalon.service.AnamneseFormBuilderService anamneseFormBuilderService,
                             com.beautysalon.repository.UserRepository userRepository,
                             com.beautysalon.repository.ClienteRepository clienteRepository,
                             com.beautysalon.service.ClienteImportacaoService clienteImportacaoService) {
        this.clienteService = clienteService;
        this.anamneseService = anamneseService;
        this.anamnesePdfService = anamnesePdfService;
        this.anamneseFormBuilderService = anamneseFormBuilderService;
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
        this.clienteImportacaoService = clienteImportacaoService;
    }

    @GetMapping
    public String listar(@PathVariable String slug, Model model) {
        List<ClienteDTO> clientes = clienteService.listarTodos();
        model.addAttribute("clientes", clientes);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Lista de Clientes");
        return "clientes/list";
    }

    @GetMapping("/novo")
    public String novoCliente(@PathVariable String slug, Model model) {
        model.addAttribute("cliente", new ClienteDTO());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Novo Cliente");
        return "clientes/form";
    }

    @PostMapping
    public String salvar(@PathVariable String slug,
                         @Valid @ModelAttribute("cliente") ClienteDTO clienteDTO,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("empresaSlug", slug);
            return "clientes/form";
        }
        clienteService.salvar(clienteDTO);
        return "redirect:/" + slug + "/clientes";
    }

    @GetMapping("/edit/{id}")
    public String exibirEdicao(@PathVariable String slug, @PathVariable Long id, Model model) {
        ClienteDTO cliente = clienteService.buscarPorId(id);
        model.addAttribute("cliente", cliente);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Editar Cliente");
        return "clientes/form";
    }

    @PostMapping("/edit/{id}")
    public String atualizar(@PathVariable String slug,
                            @PathVariable Long id,
                            @Valid @ModelAttribute("cliente") ClienteDTO clienteDTO,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("empresaSlug", slug);
            return "clientes/form";
        }
        clienteService.atualizar(id, clienteDTO);
        return "redirect:/" + slug + "/clientes";
    }

    @GetMapping("/delete/{id}")
    public String deletar(@PathVariable String slug, @PathVariable Long id) {
        clienteService.deletar(id);
        return "redirect:/" + slug + "/clientes";
    }

    @GetMapping("/pesquisar")
    public String pesquisar(@PathVariable String slug, @RequestParam("nome") String nome, Model model) {
        List<ClienteDTO> resultados = clienteService.buscarPorNomeParcial(nome);
        model.addAttribute("clientes", resultados);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Pesquisa de Clientes");
        return "clientes/list";
    }

    // ================= ANAMNESE DIGITAL & FOTOS ANTES/DEPOIS =================

    @GetMapping("/{id}/anamnese")
    public String viewAnamnese(@PathVariable String slug, @PathVariable Long id, Model model) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        com.beautysalon.model.Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        var historico = anamneseService.buscarHistoricoPorCliente(id);
        var profissionais = userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);
        var camposCustomizados = anamneseFormBuilderService.listarCamposAtivosDoTenant();

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicoAnamnese", historico);
        model.addAttribute("profissionais", profissionais);
        model.addAttribute("camposCustomizados", camposCustomizados);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Ficha de Anamnese & Evolução: " + cliente.getNome());
        return "clientes/anamnese";
    }

    @PostMapping("/{id}/anamnese/salvar")
    public String salvarAnamnese(@PathVariable String slug,
                                 @PathVariable Long id,
                                 @RequestParam String procedimentoRealizado,
                                 @RequestParam(required = false) String formulaQuimica,
                                 @RequestParam(required = false) String historicoCapilarAlergias,
                                 @RequestParam(required = false) String observacoesTecnicas,
                                 @RequestParam(required = false) String dadosClinicosJson,
                                 @RequestParam(required = false) String fotoAntesUrl,
                                 @RequestParam(required = false) String fotoDepoisUrl,
                                 @RequestParam(required = false) String assinaturaDigitalBase64,
                                 @RequestParam(required = false) Long profissionalId,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {

        anamneseService.salvarAnamnese(
                id,
                procedimentoRealizado,
                formulaQuimica,
                historicoCapilarAlergias,
                observacoesTecnicas,
                dadosClinicosJson,
                fotoAntesUrl,
                fotoDepoisUrl,
                assinaturaDigitalBase64,
                profissionalId
        );

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Ficha técnica, avaliação clínica e fotos registradas com sucesso!");
        return "redirect:/" + slug + "/clientes/" + id + "/anamnese";
    }

    @GetMapping("/{id}/anamnese/{anamneseId}/pdf")
    public ResponseEntity<byte[]> downloadAnamnesePdf(@PathVariable String slug,
                                                      @PathVariable Long id,
                                                      @PathVariable Long anamneseId) {
        byte[] pdfBytes = anamnesePdfService.gerarPdfProntuario(anamneseId);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "prontuario-anamnese-" + id + "-" + anamneseId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    @GetMapping("/importar/modelo")
    public ResponseEntity<byte[]> baixarModeloPlanilha(@PathVariable String slug,
                                                       @RequestParam(defaultValue = "xlsx") String formato) throws IOException {
        byte[] arquivoBytes;
        String contentType;
        String nomeArquivo;

        if ("csv".equalsIgnoreCase(formato)) {
            arquivoBytes = clienteImportacaoService.gerarModeloCsv();
            contentType = "text/csv; charset=UTF-8";
            nomeArquivo = "modelo_importacao_clientes_nexipro.csv";
        } else {
            arquivoBytes = clienteImportacaoService.gerarModeloXlsx();
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            nomeArquivo = "modelo_importacao_clientes_nexipro.xlsx";
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nomeArquivo + "\"")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .body(arquivoBytes);
    }

    @PostMapping("/importar")
    public String importarClientes(@PathVariable String slug,
                                   @RequestParam("arquivo") org.springframework.web.multipart.MultipartFile arquivo,
                                   org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        try {
            com.beautysalon.DTO.ImportacaoResultadoDTO resultado = clienteImportacaoService.importarClientes(arquivo, empresaId);
            if (resultado.isSucessoGeral()) {
                String msg = String.format("Importação concluída: %d clientes importados com sucesso! (%d duplicados ignorados)",
                        resultado.getTotalImportados(), resultado.getTotalDuplicadosIgnorados());
                redirectAttributes.addFlashAttribute("mensagemSucesso", msg);
            } else if (resultado.getTotalDuplicadosIgnorados() > 0) {
                redirectAttributes.addFlashAttribute("mensagemAlerta",
                        "Todos os " + resultado.getTotalDuplicadosIgnorados() + " clientes da planilha já estavam cadastrados no salão.");
            } else {
                redirectAttributes.addFlashAttribute("mensagemErro",
                        "Nenhum cliente válido encontrado na planilha. Verifique as colunas e tente novamente.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao processar planilha: " + e.getMessage());
        }
        return "redirect:/" + slug + "/clientes";
    }
}
