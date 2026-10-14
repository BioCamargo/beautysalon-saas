package com.beautysalon.Controller;

import com.beautysalon.DTO.RegisterEmpresaDTO;
import com.beautysalon.Inteface.EmpresaService;
import com.beautysalon.model.Empresa;
import com.beautysalon.repository.EmpresaRepository;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    private final EmpresaService empresaService;
    private final EmpresaRepository empresaRepository;

    public AuthController(EmpresaService empresaService, EmpresaRepository empresaRepository) {
        this.empresaService = empresaService;
        this.empresaRepository = empresaRepository;
    }

    @GetMapping({"/login", "/{slug}/login"})
    public String login(@PathVariable(required = false) String slug, Model model) {
        if (slug != null && !slug.isBlank()) {
            Optional<Empresa> empresaOpt = empresaRepository.findBySlug(slug);
            if (empresaOpt.isPresent()) {
                Empresa emp = empresaOpt.get();
                model.addAttribute("empresaNome", emp.getNome());
                model.addAttribute("empresaSlug", emp.getSlug());
                model.addAttribute("empresaLogo", emp.getLogoUrl());
                model.addAttribute("pageTitle", "Login - " + emp.getNome());
            } else {
                model.addAttribute("empresaSlug", slug);
                model.addAttribute("pageTitle", "Login - Lumora");
            }
        } else {
            model.addAttribute("pageTitle", "Login - Lumora");
        }
        model.addAttribute("contentPage", "auth/login");
        return "home/index";
    }

    @GetMapping({"/register", "/register-empresa"})
    public String showRegisterEmpresa(Model model) {
        model.addAttribute("contentPage", "auth/register");
        model.addAttribute("pageTitle", "Cadastre seu Salão - Lumora SaaS");
        model.addAttribute("empresaDTO", new RegisterEmpresaDTO());
        return "home/index";
    }

    @PostMapping("/register-empresa")
    @ResponseBody
    public Map<String, Object> registerEmpresa(
            @Valid @ModelAttribute("empresaDTO") RegisterEmpresaDTO dto,
            BindingResult result) {

        Map<String, Object> response = new HashMap<>();

        if (dto.getPassword() != null && dto.getConfirmPassword() != null
                && !dto.getPassword().equals(dto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.empresaDTO", "As senhas não coincidem");
        }

        if (result.hasErrors()) {
            response.put("success", false);
            response.put("errors", result.getAllErrors().stream()
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)
                    .toList());
            return response;
        }

        try {
            String slug = empresaService.registrarNovaEmpresa(dto);
            response.put("success", true);
            response.put("slug", slug);
            response.put("redirectUrl", "/" + slug + "/login?registered=true");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", List.of(e.getMessage()));
            return response;
        }
    }
}
