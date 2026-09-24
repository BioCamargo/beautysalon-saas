package com.beautysalon.Controller;

import com.beautysalon.DTO.AgendamentoDTO;
import com.beautysalon.Inteface.AgendamentoService;
import com.beautysalon.Inteface.ServicoService;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.service.ProfissionalService;
import com.beautysalon.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/{slug}/agendar")
public class AgendamentoPublicoController {

    private final ServicoService servicoService;
    private final ProfissionalService profissionalService;
    private final AgendamentoService agendamentoService;
    private final ClienteRepository clienteRepository;
    private final EmpresaRepository empresaRepository;

    public AgendamentoPublicoController(ServicoService servicoService,
            ProfissionalService profissionalService,
            AgendamentoService agendamentoService,
            ClienteRepository clienteRepository,
            EmpresaRepository empresaRepository) {
        this.servicoService = servicoService;
        this.profissionalService = profissionalService;
        this.agendamentoService = agendamentoService;
        this.clienteRepository = clienteRepository;
        this.empresaRepository = empresaRepository;
    }

    @GetMapping
    public String paginaAgendamento(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);

        model.addAttribute("empresa", empresa);
        model.addAttribute("servicos", servicoService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarProfissionaisAtivos());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle",
                "Agendamento Online | " + (empresa != null ? empresa.getNome() : "Beauty Salon"));
        return "publico/agendamento";
    }

    @PostMapping("/confirmar")
    public String confirmarAgendamento(@PathVariable String slug,
            @RequestParam String clienteNome,
            @RequestParam String clienteTelefone,
            @RequestParam(required = false) String clienteEmail,
            @RequestParam Long servicoId,
            @RequestParam(required = false) Long profissionalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam String horario,
            @RequestParam(required = false) String observacoes,
            RedirectAttributes redirectAttributes) {
        try {
            Long empresaId = TenantContext.getEmpresaId();
            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalStateException("Empresa não encontrada"));

            // 1. Localiza ou cria o cliente com base no telefone
            String telLimpo = clienteTelefone.replaceAll("[^0-9]", "");
            Cliente cliente = clienteRepository.findFirstByTelefoneAndEmpresaId(clienteTelefone, empresaId)
                    .or(() -> clienteRepository.findFirstByTelefoneAndEmpresaId(telLimpo, empresaId))
                    .orElseGet(() -> {
                        Cliente novo = new Cliente();
                        novo.setNome(clienteNome);
                        novo.setTelefone(clienteTelefone);
                        novo.setEmail(clienteEmail != null && !clienteEmail.isBlank() ? clienteEmail : null);
                        novo.setEmpresa(empresa);
                        return clienteRepository.save(novo);
                    });

            // 2. Monta o DTO de agendamento
            LocalTime hora = LocalTime.parse(horario);
            LocalDateTime dataHora = LocalDateTime.of(data, hora);

            AgendamentoDTO dto = new AgendamentoDTO();
            dto.setClienteId(cliente.getId());
            dto.setProfissionalId(profissionalId);
            dto.setServicoId(servicoId);
            dto.setDataHora(dataHora);
            dto.setObservacoes(
                    observacoes != null && !observacoes.isBlank() ? "Agendado online pelo cliente: " + observacoes
                            : "Agendado online pelo cliente");
            dto.setStatus("AGENDADO");

            agendamentoService.salvar(dto);

            redirectAttributes.addFlashAttribute("sucesso", true);
            redirectAttributes.addFlashAttribute("dataAgendamento",
                    dataHora.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")));
            redirectAttributes.addFlashAttribute("clienteNome", cliente.getNome());
            return "redirect:/" + slug + "/agendar/sucesso";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erro", "Erro ao agendar: " + e.getMessage());
            return "redirect:/" + slug + "/agendar";
        }
    }

    @GetMapping("/horarios-ocupados")
    @ResponseBody
    public List<String> obterHorariosOcupados(@PathVariable String slug,
                                              @RequestParam(required = false) Long profissionalId,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        List<com.beautysalon.model.Agendamento> agendamentos = agendamentoService.listarTodos().stream()
                .filter(a -> a.getDataHora() != null && a.getDataHora().toLocalDate().isEqual(data))
                .filter(a -> !"CANCELADO".equalsIgnoreCase(a.getStatus()) && !"NAO_COMPARECEU".equalsIgnoreCase(a.getStatus()))
                .filter(a -> profissionalId == null || (a.getProfissionalId() != null && a.getProfissionalId().equals(profissionalId)))
                .map(dto -> {
                    com.beautysalon.model.Agendamento ag = new com.beautysalon.model.Agendamento();
                    ag.setDataHora(dto.getDataHora());
                    return ag;
                })
                .toList();

        List<String> horasOcupadas = new ArrayList<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
        for (com.beautysalon.model.Agendamento a : agendamentos) {
            horasOcupadas.add(a.getDataHora().format(fmt));
        }
        return horasOcupadas;
    }

    @GetMapping("/sucesso")
    public String sucessoAgendamento(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
        model.addAttribute("empresa", empresa);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Agendamento Confirmado!");
        return "publico/sucesso";
    }
}
