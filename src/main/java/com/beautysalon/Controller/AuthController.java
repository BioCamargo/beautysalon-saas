package com.beautysalon.Controller;

import com.beautysalon.DTO.RegisterEmpresaDTO;
import com.beautysalon.Inteface.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AuthController {

    private final EmpresaService empresaService;

    public AuthController(EmpresaService empresaService) {
        this.empresaService = empresaService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("contentPage", "auth/login");
        model.addAttribute("pageTitle", "Login - Lumora");
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
            response.put("redirectUrl", "/login?registered=true");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", List.of(e.getMessage()));
            return response;
        }
    }
}
