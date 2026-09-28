package com.beautysalon.Controller;

import com.beautysalon.DTO.ClienteDTO;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.service.SmsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/{slug}/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final SmsService smsService;
    private final com.beautysalon.repository.ClienteAnamneseRepository clienteAnamneseRepository;
    private final com.beautysalon.repository.UserRepository userRepository;
    private final com.beautysalon.repository.ClienteRepository clienteRepository;

    public ClienteController(ClienteService clienteService,
                             SmsService smsService,
                             com.beautysalon.repository.ClienteAnamneseRepository clienteAnamneseRepository,
                             com.beautysalon.repository.UserRepository userRepository,
                             com.beautysalon.repository.ClienteRepository clienteRepository) {
        this.clienteService = clienteService;
        this.smsService = smsService;
        this.clienteAnamneseRepository = clienteAnamneseRepository;
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
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

    @PostMapping("/{id}/enviar-sms-confirmacao")
    public ResponseEntity<String> enviarSmsConfirmacao(@PathVariable String slug, @PathVariable Long id) {
        ClienteDTO cliente = clienteService.buscarPorId(id);
        try {
            String resposta = smsService.enviarSms(
                    cliente.getTelefone(),
                    "Confirmação de cadastro: " + gerarCodigo()
            );
            return ResponseEntity.ok("SMS enviado: " + resposta);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Erro ao enviar SMS");
        }
    }

    // ================= ANAMNESE DIGITAL & FOTOS ANTES/DEPOIS =================

    @GetMapping("/{id}/anamnese")
    public String viewAnamnese(@PathVariable String slug, @PathVariable Long id, Model model) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        com.beautysalon.model.Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        var historico = clienteAnamneseRepository.findByClienteIdAndEmpresaIdOrderByDataRegistroDesc(id, empresaId);
        var profissionais = userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);

        model.addAttribute("cliente", cliente);
        model.addAttribute("historicoAnamnese", historico);
        model.addAttribute("profissionais", profissionais);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Ficha Química & Fotos: " + cliente.getNome());
        return "clientes/anamnese";
    }

    @PostMapping("/{id}/anamnese/salvar")
    public String salvarAnamnese(@PathVariable String slug,
                                 @PathVariable Long id,
                                 @RequestParam String procedimentoRealizado,
                                 @RequestParam(required = false) String formulaQuimica,
                                 @RequestParam(required = false) String historicoCapilarAlergias,
                                 @RequestParam(required = false) String observacoesTecnicas,
                                 @RequestParam(required = false) String fotoAntesUrl,
                                 @RequestParam(required = false) String fotoDepoisUrl,
                                 @RequestParam(required = false) String assinaturaDigitalBase64,
                                 @RequestParam(required = false) Long profissionalId,
                                 org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Long empresaId = com.beautysalon.tenant.TenantContext.getEmpresaId();
        com.beautysalon.model.Cliente cliente = clienteRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        com.beautysalon.model.User prof = null;
        if (profissionalId != null) {
            prof = userRepository.findByIdAndEmpresaId(profissionalId, empresaId).orElse(null);
        }

        com.beautysalon.model.ClienteAnamnese anamnese = com.beautysalon.model.ClienteAnamnese.builder()
                .cliente(cliente)
                .empresa(cliente.getEmpresa())
                .profissional(prof)
                .procedimentoRealizado(procedimentoRealizado)
                .formulaQuimica(formulaQuimica)
                .historicoCapilarAlergias(historicoCapilarAlergias)
                .observacoesTecnicas(observacoesTecnicas)
                .fotoAntesUrl(fotoAntesUrl)
                .fotoDepoisUrl(fotoDepoisUrl)
                .assinaturaDigitalBase64(assinaturaDigitalBase64)
                .termoConsentimentoAceito(true)
                .dataRegistro(java.time.LocalDateTime.now())
                .build();

        clienteAnamneseRepository.save(anamnese);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Ficha química e fotos registradas com sucesso!");
        return "redirect:/" + slug + "/clientes/" + id + "/anamnese";
    }

    private String gerarCodigo() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
