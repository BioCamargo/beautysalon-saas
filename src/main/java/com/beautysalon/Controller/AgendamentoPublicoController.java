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

    @org.springframework.web.bind.annotation.InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));
    }

    @GetMapping
    public String paginaAgendamento(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        Empresa empresa = empresaRepository.findById(empresaId).orElse(null);

        model.addAttribute("empresa", empresa);
        model.addAttribute("bloqueioSuave", empresa != null && empresa.isBloqueioSuaveAtivo());
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataNascimento,
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

            if (empresa.isBloqueioSuaveAtivo()) {
                redirectAttributes.addFlashAttribute("error", "O agendamento online deste estabelecimento está temporariamente pausado. Entre em contato diretamente pelo WhatsApp.");
                return "redirect:/" + slug + "/agendar";
            }

            LocalTime hora = LocalTime.parse(horario);
            LocalDateTime dataHora = LocalDateTime.of(data, hora);

            // Validação de segurança: Não permitir agendamento no passado
            if (dataHora.isBefore(LocalDateTime.now().minusMinutes(2))) {
                redirectAttributes.addFlashAttribute("erro", "Não é permitido agendar em uma data ou horário que já passou. Por favor, escolha um horário futuro.");
                return "redirect:/" + slug + "/agendar";
            }

            // 1. Localiza ou cria o cliente com base no telefone e grava aniversário
            String telLimpo = clienteTelefone.replaceAll("[^0-9]", "");
            Cliente cliente = clienteRepository.findFirstByTelefoneAndEmpresaId(clienteTelefone, empresaId)
                    .or(() -> clienteRepository.findFirstByTelefoneAndEmpresaId(telLimpo, empresaId))
                    .map(existente -> {
                        if (dataNascimento != null && existente.getDataNascimento() == null) {
                            existente.setDataNascimento(dataNascimento);
                            clienteRepository.save(existente);
                        }
                        return existente;
                    })
                    .orElseGet(() -> {
                        Cliente novo = new Cliente();
                        novo.setNome(clienteNome);
                        novo.setTelefone(clienteTelefone);
                        novo.setEmail(clienteEmail != null && !clienteEmail.isBlank() ? clienteEmail : null);
                        novo.setDataNascimento(dataNascimento);
                        novo.setEmpresa(empresa);
                        return clienteRepository.save(novo);
                    });

            // 2. Monta o DTO de agendamento
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
            redirectAttributes.addFlashAttribute("dataIso", dataHora.toString());
            redirectAttributes.addFlashAttribute("dataApenas", data.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            redirectAttributes.addFlashAttribute("horaApenas", horario);
            redirectAttributes.addFlashAttribute("clienteNome", cliente.getNome());
            redirectAttributes.addFlashAttribute("clienteTelefone", cliente.getTelefone());

            // Dados adicionais para resumo, Google Agenda e WhatsApp
            try {
                com.beautysalon.DTO.ServicoDTO s = servicoService.buscarPorId(servicoId);
                if (s != null) {
                    redirectAttributes.addFlashAttribute("servicoNome", s.getNome());
                    redirectAttributes.addFlashAttribute("servicoPreco", s.getPreco());
                    redirectAttributes.addFlashAttribute("servicoDuracao", s.getDuracaoMinutos());
                }
            } catch (Exception ignored) {
            }

            if (profissionalId != null) {
                try {
                    var p = profissionalService.buscarPorId(profissionalId);
                    if (p != null) {
                        redirectAttributes.addFlashAttribute("profissionalNome", p.getNome());
                    }
                } catch (Exception ignored) {
                }
            }

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
        java.util.Set<String> horasOcupadas = new java.util.LinkedHashSet<>();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        // 1. Horários já ocupados por outros agendamentos
        List<com.beautysalon.DTO.AgendamentoDTO> agendamentos = agendamentoService.listarTodos().stream()
                .filter(a -> a.getDataHora() != null && a.getDataHora().toLocalDate().isEqual(data))
                .filter(a -> !"CANCELADO".equalsIgnoreCase(a.getStatus())
                        && !"NAO_COMPARECEU".equalsIgnoreCase(a.getStatus()))
                .filter(a -> profissionalId == null
                        || (a.getProfissionalId() != null && a.getProfissionalId().equals(profissionalId)))
                .toList();

        for (var a : agendamentos) {
            horasOcupadas.add(a.getDataHora().format(fmt));
        }

        // Grade padrão de horários oferecidos no portal
        List<LocalTime> slotsGrade = List.of(
                LocalTime.of(8, 0), LocalTime.of(8, 30),
                LocalTime.of(9, 0), LocalTime.of(9, 30),
                LocalTime.of(10, 0), LocalTime.of(10, 30),
                LocalTime.of(11, 0), LocalTime.of(11, 30),
                LocalTime.of(13, 0), LocalTime.of(13, 30),
                LocalTime.of(14, 0), LocalTime.of(14, 30),
                LocalTime.of(15, 0), LocalTime.of(15, 30),
                LocalTime.of(16, 0), LocalTime.of(16, 30),
                LocalTime.of(17, 0), LocalTime.of(17, 30),
                LocalTime.of(18, 0), LocalTime.of(18, 30),
                LocalTime.of(19, 0), LocalTime.of(19, 30),
                LocalTime.of(20, 0), LocalTime.of(20, 30)
        );

        // 2. Bloqueio de Ausências, Folgas e Férias do Profissional selecionado
        if (profissionalId != null) {
            var ausencias = profissionalService.listarAusenciasPorProfissional(profissionalId);
            for (var aus : ausencias) {
                boolean coincide = false;
                if (aus.getDataEspecifica() != null && aus.getDataEspecifica().isEqual(data)) {
                    coincide = true;
                } else if (aus.getDiaSemanaRecorrente() != null && aus.getDiaSemanaRecorrente() == data.getDayOfWeek()) {
                    coincide = true;
                }

                if (coincide) {
                    LocalTime ausIni = aus.getHoraInicio() != null ? aus.getHoraInicio() : LocalTime.of(0, 0);
                    LocalTime ausFim = aus.getHoraFim() != null ? aus.getHoraFim() : LocalTime.of(23, 59);

                    for (LocalTime slot : slotsGrade) {
                        LocalTime slotFim = slot.plusMinutes(30);
                        if (slot.isBefore(ausFim) && slotFim.isAfter(ausIni)) {
                            horasOcupadas.add(slot.format(fmt));
                        }
                    }
                }
            }
        }

        // 3. Se a data for HOJE, bloqueia qualquer horário que já passou
        if (data.isEqual(LocalDate.now())) {
            LocalTime agora = LocalTime.now();
            for (LocalTime slot : slotsGrade) {
                if (slot.isBefore(agora)) {
                    horasOcupadas.add(slot.format(fmt));
                }
            }
        }

        return new ArrayList<>(horasOcupadas);
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
