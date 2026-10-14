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
    private final com.beautysalon.repository.UserRepository userRepository;
    private final org.springframework.security.web.context.SecurityContextRepository securityContextRepository =
            new org.springframework.security.web.context.HttpSessionSecurityContextRepository();

    public AuthController(EmpresaService empresaService,
                          EmpresaRepository empresaRepository,
                          com.beautysalon.repository.UserRepository userRepository) {
        this.empresaService = empresaService;
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
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

    @GetMapping("/select-tenant")
    public String showSelectTenant(jakarta.servlet.http.HttpSession session, Model model) {
        @SuppressWarnings("unchecked")
        List<com.beautysalon.DTO.TenantCandidateDTO> candidates =
                (List<com.beautysalon.DTO.TenantCandidateDTO>) session.getAttribute("PENDING_TENANT_CANDIDATES");

        if (candidates == null || candidates.isEmpty()) {
            return "redirect:/login";
        }

        String username = (String) session.getAttribute("PENDING_TENANT_USERNAME");
        model.addAttribute("candidates", candidates);
        model.addAttribute("username", username);
        model.addAttribute("pageTitle", "Selecione o Estabelecimento - Lumora");
        model.addAttribute("contentPage", "auth/select-tenant");
        return "home/index";
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @PostMapping("/select-tenant")
    public String selectTenant(@RequestParam("userId") Long selectedUserId,
                               jakarta.servlet.http.HttpServletRequest request,
                               jakarta.servlet.http.HttpServletResponse response,
                               jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        List<com.beautysalon.DTO.TenantCandidateDTO> candidates =
                (List<com.beautysalon.DTO.TenantCandidateDTO>) session.getAttribute("PENDING_TENANT_CANDIDATES");

        if (candidates == null || candidates.isEmpty()) {
            return "redirect:/login";
        }

        Optional<com.beautysalon.DTO.TenantCandidateDTO> matchOpt = candidates.stream()
                .filter(c -> c.getUserId().equals(selectedUserId))
                .findFirst();

        if (matchOpt.isEmpty()) {
            return "redirect:/select-tenant";
        }

        com.beautysalon.DTO.TenantCandidateDTO selected = matchOpt.get();
        com.beautysalon.model.User user = userRepository.findById(selected.getUserId())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        if (!user.isAtivo() || (user.getEmpresa() != null && !user.getEmpresa().isAtivo())) {
            return "redirect:/login?error";
        }

        com.beautysalon.tenant.TenantUserDetails tenantUserDetails = new com.beautysalon.tenant.TenantUserDetails(user);
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken auth =
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        tenantUserDetails,
                        null,
                        tenantUserDetails.getAuthorities()
                );

        org.springframework.security.core.context.SecurityContext context =
                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        org.springframework.security.core.context.SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, request, response);

        session.removeAttribute("PENDING_TENANT_CANDIDATES");
        session.removeAttribute("PENDING_TENANT_USERNAME");

        String slug = selected.getEmpresaSlug();
        return "redirect:/" + (slug != null && !slug.isBlank() ? slug : "lumora") + "/home";
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
            response.put("empresaNome", dto.getNomeEmpresa());
            response.put("email", dto.getEmailUsuario());
            response.put("redirectUrl", "/" + slug + "/login?registered=true");
            return response;
        } catch (Exception e) {
            response.put("success", false);
            response.put("errors", List.of(e.getMessage()));
            return response;
        }
    }
}
