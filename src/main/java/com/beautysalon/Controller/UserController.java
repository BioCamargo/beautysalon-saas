package com.beautysalon.Controller;

import com.beautysalon.DTO.UserDTO;
import com.beautysalon.Inteface.UserService;
import com.beautysalon.model.TenantRole;
import com.beautysalon.tenant.TenantContext;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/{slug}/usuarios")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String listar(@PathVariable String slug, Model model) {
        Long empresaId = TenantContext.getEmpresaId();
        model.addAttribute("usuarios", userService.listarTodosDaEmpresa(empresaId));
        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Usuários do Estúdio");
        return "usuario/list";
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

        if (userService.existsByUsername(dto.getUsername(), empresaId)) {
            result.rejectValue("username", "error.usuario", "Nome de usuário já existe nesta empresa");
        }

        if (userService.existsByEmail(dto.getEmail(), empresaId)) {
            result.rejectValue("email", "error.usuario", "Email já cadastrado nesta empresa");
        }

        if (result.hasErrors()) {
            model.addAttribute("roles", TenantRole.values());
            model.addAttribute("empresaSlug", slug);
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

        if (result.hasErrors()) {
            model.addAttribute("roles", TenantRole.values());
            model.addAttribute("empresaSlug", slug);
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
}
