package com.beautysalon.config;

import com.beautysalon.model.Empresa;
import com.beautysalon.model.PlanoSaaS;
import com.beautysalon.model.SegmentoEmpresa;
import com.beautysalon.model.StatusAssinatura;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.service.WhatsAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Provisionador automático de Contratos Vitalícios VIP (Isentos de Planos e Mensalidades).
 * Garante que a empresa Studio Helen Coltro (CNPJ 46.101.466/0001-84) esteja criada e
 * permanentemente configurada como VITALICIO em qualquer ambiente (Local ou Produção/Render).
 */
@Component
public class VitalicioTenantInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(VitalicioTenantInitializer.class);

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;
    private final PasswordEncoder passwordEncoder;

    public VitalicioTenantInitializer(EmpresaRepository empresaRepository,
                                      UserRepository userRepository,
                                      WhatsAppService whatsAppService,
                                      PasswordEncoder passwordEncoder) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
        this.whatsAppService = whatsAppService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        try {
            garantirContratoVitalicioHelenColtro();
        } catch (Exception e) {
            log.warn("Aviso ao verificar/provisionar contrato vitalício: {}", e.getMessage());
        }
    }

    private void garantirContratoVitalicioHelenColtro() {
        final String slugOficial = "studiohelencoltro";
        final String cnpjLimpo = "46101466000184";

        // 1. Verifica todas as empresas existentes que batam com os dados da Helen Coltro e promove para VITALICIO
        List<Empresa> todas = empresaRepository.findAll();
        Empresa empresaAlvo = null;

        for (Empresa emp : todas) {
            if (com.beautysalon.Implementacao.EmpresaServiceImpl.isEmpresaVitalicia(
                    emp.getNome(), emp.getSlug(), emp.getCnpj(), emp.getEmail(), null, null)) {
                if (!emp.isVitalicio()) {
                    emp.setPlano(PlanoSaaS.VITALICIO);
                    emp.setStatusAssinatura(StatusAssinatura.VITALICIO);
                    emp.setDataExpiracaoTrial(null);
                    emp.setAtivo(true);
                    empresaRepository.save(emp);
                    log.info("💎 Empresa '{}' (slug={}) promovida para CONTRATO VITALÍCIO VIP!", emp.getNome(), emp.getSlug());
                }
                if (slugOficial.equalsIgnoreCase(emp.getSlug())) {
                    empresaAlvo = emp;
                }
            }
        }

        // 2. Se ainda não existir o tenant oficial 'studiohelencoltro', cria já com Contrato Vitalício VIP
        if (empresaAlvo == null) {
            Optional<Empresa> porSlug = empresaRepository.findBySlug(slugOficial);
            if (porSlug.isPresent()) {
                empresaAlvo = porSlug.get();
                empresaAlvo.setPlano(PlanoSaaS.VITALICIO);
                empresaAlvo.setStatusAssinatura(StatusAssinatura.VITALICIO);
                empresaAlvo.setDataExpiracaoTrial(null);
                empresaAlvo.setAtivo(true);
                empresaAlvo = empresaRepository.save(empresaAlvo);
            } else {
                empresaAlvo = Empresa.builder()
                        .nome("Studio Helen Coltro")
                        .slug(slugOficial)
                        .cnpj(cnpjLimpo)
                        .email("contato@studiohelencoltro.com.br")
                        .segmento(SegmentoEmpresa.LASH_BROW)
                        .plano(PlanoSaaS.VITALICIO)
                        .statusAssinatura(StatusAssinatura.VITALICIO)
                        .dataExpiracaoTrial(null)
                        .ativo(true)
                        .build();
                empresaAlvo = empresaRepository.save(empresaAlvo);
                whatsAppService.obterOuCriarConfiguracaoPadrao(empresaAlvo);
                log.info("💎 Empresa Vitalícia provisionada: Studio Helen Coltro (/{}/login | CNPJ {})", slugOficial, cnpjLimpo);
            }
        }

        // 3. Garante que a usuária proprietária (Helen Coltro) exista no Studio Helen Coltro
        Long empresaId = empresaAlvo.getId();
        if (userRepository.findByUsernameIgnoreCaseAndEmpresaId("helencoltro", empresaId).isEmpty()
                && userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId).isEmpty()) {
            User owner = User.builder()
                    .nome("Helen Caroline Mendes Coltro")
                    .username("helencoltro")
                    .email("helen@studiohelencoltro.com.br")
                    .password(passwordEncoder.encode("Helen@2026"))
                    .especialidade("Micropigmentadora, Lash & Estética")
                    .ativo(true)
                    .tenantRole(TenantRole.OWNER)
                    .empresa(empresaAlvo)
                    .build();
            userRepository.save(owner);
            log.info("👑 Usuária Proprietária Vitalícia criada: username='helencoltro' para '/{}/login'", slugOficial);
        }
    }
}
