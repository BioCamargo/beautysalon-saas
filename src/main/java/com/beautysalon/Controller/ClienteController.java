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

    public ClienteController(ClienteService clienteService, SmsService smsService) {
        this.clienteService = clienteService;
        this.smsService = smsService;
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

    private String gerarCodigo() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}
