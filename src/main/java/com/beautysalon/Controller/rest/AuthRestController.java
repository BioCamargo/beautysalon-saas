package com.beautysalon.Controller.rest;

import com.beautysalon.DTO.auth.AuthRestDTO;
import com.beautysalon.Implementacao.UserDetailsServiceImpl;
import com.beautysalon.config.jwt.JwtService;
import com.beautysalon.model.User;
import com.beautysalon.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação REST (JWT)", description = "Endpoints para login, autenticação e obtenção de Tokens JWT")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthRestController(AuthenticationManager authenticationManager,
                              UserDetailsServiceImpl userDetailsService,
                              JwtService jwtService,
                              UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário e obter Token JWT")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRestDTO.LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

            User user = userDetails instanceof com.beautysalon.tenant.TenantUserDetails tud
                    ? tud.getUser()
                    : userRepository.findFirstByUsername(request.username())
                    .orElseThrow(() -> new IllegalStateException("Usuário não localizado no sistema."));


            Long empresaId = user.getEmpresa() != null ? user.getEmpresa().getId() : null;
            String empresaSlug = user.getEmpresa() != null ? user.getEmpresa().getSlug() : null;
            String empresaNome = user.getEmpresa() != null ? user.getEmpresa().getNome() : null;
            String role = user.getTenantRole() != null ? user.getTenantRole().name() : "FUNCIONARIO";

            String token = jwtService.generateToken(userDetails, empresaId, empresaSlug, role);

            AuthRestDTO.LoginResponse response = new AuthRestDTO.LoginResponse(
                    token,
                    "Bearer",
                    user.getUsername(),
                    role,
                    empresaId,
                    empresaSlug,
                    empresaNome
            );

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário ou senha inválidos.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro na autenticação: " + e.getMessage());
        }
    }

    @GetMapping("/find-tenants")
    @Operation(summary = "Localizar links exclusivos de salões por e-mail")
    public ResponseEntity<?> findTenantsByEmail(@RequestParam("email") String email) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Informe um e-mail válido."));
        }

        List<User> users = userRepository.findAllByEmail(email.trim().toLowerCase());
        if (users.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Nenhum estúdio encontrado associado a este e-mail."));
        }

        List<Map<String, String>> saloes = users.stream()
                .filter(u -> u.getEmpresa() != null && u.getEmpresa().isAtivo())
                .map(u -> Map.of(
                        "nome", u.getEmpresa().getNome(),
                        "slug", u.getEmpresa().getSlug(),
                        "url", "/" + u.getEmpresa().getSlug() + "/login"
                ))
                .distinct()
                .toList();

        return ResponseEntity.ok(Map.of("saloes", saloes));
    }

    @PostMapping("/tab-logout")
    @Operation(summary = "Invalidar sessão imediatamente ao fechar aba ou janela do navegador")
    public ResponseEntity<Void> tabLogout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
