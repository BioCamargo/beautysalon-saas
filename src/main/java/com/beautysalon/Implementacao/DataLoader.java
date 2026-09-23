package com.beautysalon.Implementacao;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataLoader(EmpresaRepository empresaRepository,
                      UserRepository userRepository,
                      BCryptPasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        inicializarEmpresaEDadosIniciais();
    }

    private void inicializarEmpresaEDadosIniciais() {
        String slugDemo = "lumora";

        Empresa empresa = empresaRepository.findBySlug(slugDemo).orElseGet(() -> {
            log.info("Criando empresa padrão de demonstração: {}", slugDemo);
            Empresa novaEmpresa = Empresa.builder()
                    .nome("Studio Lumora")
                    .slug(slugDemo)
                    .email("contato@lumora.com")
                    .telefone("(11) 99999-9999")
                    .ativo(true)
                    .build();
            return empresaRepository.save(novaEmpresa);
        });

        if (userRepository.findByUsernameAndEmpresaId("admin", empresa.getId()).isEmpty()) {
            log.info("Criando usuário administrador padrão (admin / admin123) para empresa: {}", empresa.getNome());
            User admin = User.builder()
                    .nome("Administrador")
                    .username("admin")
                    .email("admin@lumora.com")
                    .password(passwordEncoder.encode("admin123"))
                    .tenantRole(TenantRole.OWNER)
                    .ativo(true)
                    .empresa(empresa)
                    .build();
            userRepository.save(admin);
        }
    }
}
