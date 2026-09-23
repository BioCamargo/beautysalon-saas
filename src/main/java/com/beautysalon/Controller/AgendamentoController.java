package com.beautysalon.Controller;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.Inteface.AgendamentoService;
import com.beautysalon.Inteface.ClienteService;
import com.beautysalon.Inteface.ServicoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/{slug}/agendamentos")
public class AgendamentoController {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoController.class);

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ServicoService servicoService;
    private final com.beautysalon.repository.UserRepository userRepository;

    public AgendamentoController(AgendamentoService agendamentoService,
                                 ClienteService clienteService,
                                 ServicoService servicoService,
                                 com.beautysalon.repository.UserRepository userRepository) {
        this.agendamentoService = agendamentoService;
        this.clienteService = clienteService;
        this.servicoService = servicoService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listar(@PathVariable String slug, Model model) {
        List<AgendamentoDTO> agendamentos = agendamentoService.listarTodos();
        model.addAttribute("agendamentos", agendamentos);
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionais", userRepository.findAllByEmpresaIdAndAtivoTrue(com.beautysalon.tenant.TenantContext.getEmpresaId()));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Agendamentos");
        return "agendamentos/list";
    }

    @GetMapping("/novo")
    public String novoForm(@PathVariable String slug, Model model) {
        model.addAttribute("agendamento", new AgendamentoDTO());
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionais", userRepository.findAllByEmpresaIdAndAtivoTrue(com.beautysalon.tenant.TenantContext.getEmpresaId()));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Novo Agendamento");
        return "agendamentos/form";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable String slug, @PathVariable Long id, Model model) {
        model.addAttribute("agendamento", agendamentoService.buscarPorId(id));
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionais", userRepository.findAllByEmpresaIdAndAtivoTrue(com.beautysalon.tenant.TenantContext.getEmpresaId()));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Editar Agendamento");
        return "agendamentos/form";
    }

    @PostMapping
    public String salvar(@PathVariable String slug,
                         @Valid @ModelAttribute("agendamento") AgendamentoDTO agendamentoDTO,
                         BindingResult result,
                         Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("servicos", servicoService.listarTodos());
            model.addAttribute("empresaSlug", slug);
            return "agendamentos/form";
        }
        try {
            agendamentoService.salvar(agendamentoDTO);
            return "redirect:/" + slug + "/agendamentos";
        } catch (RuntimeException e) {
            logger.error("Erro ao salvar agendamento: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("servicos", servicoService.listarTodos());
            model.addAttribute("empresaSlug", slug);
            return "agendamentos/form";
        }
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable String slug,
                            @PathVariable Long id,
                            @Valid @ModelAttribute("agendamento") AgendamentoDTO dto,
                            BindingResult result,
                            Model model) {
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarTodos());
            model.addAttribute("servicos", servicoService.listarTodos());
            model.addAttribute("empresaSlug", slug);
            return "agendamentos/form";
        }
        agendamentoService.atualizar(id, dto);
        return "redirect:/" + slug + "/agendamentos";
    }

    @PostMapping("/deletar/{id}")
    @ResponseBody
    public ResponseEntity<?> deletar(@PathVariable String slug, @PathVariable Long id) {
        try {
            agendamentoService.deletar(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/servico/{id}")
    public String listarPorServico(@PathVariable String slug, @PathVariable("id") Long servicoId, Model model) {
        model.addAttribute("agendamentos", agendamentoService.listarPorServico(servicoId));
        model.addAttribute("empresaSlug", slug);
        return "agendamentos/list";
    }
}
