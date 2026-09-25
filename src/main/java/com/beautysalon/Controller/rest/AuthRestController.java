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
import org.springframework.web.bind.annotation.*;

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
            User user = userRepository.findByUsername(request.username())
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
}
