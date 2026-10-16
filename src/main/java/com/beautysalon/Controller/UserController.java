package com.beautysalon.Controller;

import com.beautysalon.DTO.UserDTO;
import com.beautysalon.Inteface.UserService;
import com.beautysalon.model.AusenciaProfissional;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.ProfissionalService;
import com.beautysalon.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/{slug}/usuarios")
public class UserController {

    private final UserService userService;
    private final ProfissionalService profissionalService;
    private final UserRepository userRepository;

    public UserController(UserService userService,
                          ProfissionalService profissionalService,
                          UserRepository userRepository) {
        this.userService = userService;
        this.profissionalService = profissionalService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listar(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        model.addAttribute("usuarios", userService.listarTodosDaEmpresa(empresaId));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Usuários & Equipe");
        return "usuario/list";
    }

    @org.springframework.web.bind.annotation.InitBinder
    public void initBinder(org.springframework.web.bind.WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new org.springframework.beans.propertyeditors.StringTrimmerEditor(true));
    }

    @GetMapping("/novo")
    public String novoForm(@PathVariable String slug, Model model) {
        model.addAttribute("usuario", new UserDTO());
        model.addAttribute("roles", TenantRole.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Novo Usuário");
        return "usuario/form";
    }

    @PostMapping
    public String salvar(@PathVariable String slug,
                         @Valid @ModelAttribute("usuario") UserDTO dto,
                         BindingResult result,
                         Model model) throws IOException {
        Long empresaId = TenantContext.getEmpresaId();

        if (dto.getPassword() == null || dto.getPassword().isBlank() || dto.getPassword().length() < 6) {
            result.rejectValue("password", "error.usuario", "A senha é obrigatória e deve ter no mínimo 6 caracteres");
        }

        if (userService.existsByUsername(dto.getUsername(), empresaId)) {
            result.rejectValue("username", "error.usuario", "Nome de usuário já existe nesta empresa");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", TenantRole.values());
            model.addAttribute("empresaSlug", slug);
            model.addAttribute("pageTitle", "Novo Usuário");
            return "usuario/form";
        }

        dto.setEmpresaId(empresaId);
        userService.salvar(dto);
        return "redirect:/" + slug + "/usuarios";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable String slug, @PathVariable Long id, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        model.addAttribute("usuario", userService.buscarPorId(id, empresaId));
        model.addAttribute("roles", TenantRole.values());
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Editar Usuário");
        return "usuario/form";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(@PathVariable String slug,
                            @PathVariable Long id,
                            @Valid @ModelAttribute("usuario") UserDTO dto,
                            BindingResult result,
                            Model model) {
        Long empresaId = TenantContext.getEmpresaId();

        // Se uma nova senha for fornecida, ela deve ter no mínimo 6 caracteres
        if (dto.getPassword() != null && !dto.getPassword().isBlank() && dto.getPassword().length() < 6) {
            result.rejectValue("password", "error.usuario", "A nova senha deve ter no mínimo 6 caracteres");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", TenantRole.values());
            model.addAttribute("empresaSlug", slug);
            model.addAttribute("pageTitle", "Editar Usuário");
            return "usuario/form";
        }

        userService.atualizar(id, dto, empresaId);
        return "redirect:/" + slug + "/usuarios";
    }

    @PostMapping("/{id}/deletar")
    public String deletar(@PathVariable String slug, @PathVariable Long id) {
        Long empresaId = TenantContext.getEmpresaId();
        userService.deletar(id, empresaId);
        return "redirect:/" + slug + "/usuarios";
    }

    // ==========================================
    // GESTÃO DE AUSÊNCIAS, FOLGAS & FÉRIAS (TC-USR-03)
    // ==========================================

    @GetMapping("/{id}/ausencias")
    public String listarAusencias(@PathVariable String slug, @PathVariable Long id, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        User profissional = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        List<AusenciaProfissional> ausencias = profissionalService.listarAusenciasPorProfissional(id);

        model.addAttribute("profissional", profissional);
        model.addAttribute("ausencias", ausencias);
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Ausências & Férias — " + profissional.getNome());
        return "usuario/ausencias";
    }

    @PostMapping("/{id}/ausencias")
    public String salvarAusencia(@PathVariable String slug,
                                 @PathVariable Long id,
                                 @RequestParam String tipo,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataEspecifica,
                                 @RequestParam(required = false) DayOfWeek diaSemanaRecorrente,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim,
                                 @RequestParam(required = false) String motivo,
                                 RedirectAttributes redirectAttributes) {
        Long empresaId = TenantContext.getEmpresaId();
        User profissional = userRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Profissional não encontrado"));

        AusenciaProfissional ausencia = AusenciaProfissional.builder()
                .profissional(profissional)
                .tipo(tipo)
                .dataEspecifica(dataEspecifica)
                .diaSemanaRecorrente(diaSemanaRecorrente)
                .horaInicio(horaInicio != null ? horaInicio : LocalTime.of(0, 0))
                .horaFim(horaFim != null ? horaFim : LocalTime.of(23, 59))
                .motivo(motivo != null && !motivo.isBlank() ? motivo : "Folga / Ausência Programada")
                .ativo(true)
                .build();

        profissionalService.salvarAusencia(ausencia);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Período de ausência registrado com sucesso!");
        return "redirect:/" + slug + "/usuarios/" + id + "/ausencias";
    }

    @PostMapping("/{id}/ausencias/{ausenciaId}/deletar")
    public String removerAusencia(@PathVariable String slug,
                                  @PathVariable Long id,
                                  @PathVariable Long ausenciaId,
                                  RedirectAttributes redirectAttributes) {
        profissionalService.removerAusencia(ausenciaId);
        redirectAttributes.addFlashAttribute("mensagemSucesso", "Ausência removida com sucesso!");
        return "redirect:/" + slug + "/usuarios/" + id + "/ausencias";
    }
}
