package com.beautysalon.Implementacao;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.WhatsAppConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("!prod")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final WhatsAppConfigRepository whatsAppConfigRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataLoader(EmpresaRepository empresaRepository,
                      UserRepository userRepository,
                      WhatsAppConfigRepository whatsAppConfigRepository,
                      BCryptPasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.whatsAppConfigRepository = whatsAppConfigRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            inicializarEmpresaEDadosIniciais();
        } catch (Exception e) {
            log.warn("DataLoader: Inicialização de demonstração ignorada com segurança: {}", e.getMessage());
        }
    }

    private void inicializarEmpresaEDadosIniciais() {
        String slugDemo = "nexipro";

        Empresa empresa = empresaRepository.findBySlug(slugDemo).orElseGet(() -> {
            log.info("Criando empresa padrão de demonstração: {}", slugDemo);
            Empresa novaEmpresa = Empresa.builder()
                    .nome("NEXI PRO Studio")
                    .slug(slugDemo)
                    .email("contato@nexipro.com.br")
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
                    .email("admin@nexipro.com.br")
                    .password(passwordEncoder.encode("admin123"))
                    .tenantRole(TenantRole.OWNER)
                    .ativo(true)
                    .empresa(empresa)
                    .build();
            userRepository.save(admin);
        }

        if (whatsAppConfigRepository.findByEmpresaId(empresa.getId()).isEmpty()) {
            log.info("Inicializando configuração padrão de WhatsApp para empresa: {}", empresa.getNome());
            WhatsAppConfig whatsConfig = WhatsAppConfig.builder()
                    .empresa(empresa)
                    .ativo(true)
                    .provider("EVOLUTION_API")
                    .apiUrl("http://localhost:8080")
                    .instanceName("nexipro-studio")
                    .apiKey("nexipro123")
                    .notificarAgendamento(true)
                    .notificarLembrete(true)
                    .notificarComandaFechada(true)
                    .notificarAniversario(true)
                    .build();
            whatsAppConfigRepository.save(whatsConfig);
        }
    }
}
