package com.beautysalon.service;

import com.beautysalon.model.AuditoriaEvento;
import com.beautysalon.model.User;
import com.beautysalon.repository.AuditoriaEventoRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantContext;
import com.beautysalon.tenant.TenantLoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Serviço Corporativo de Trilha de Auditoria (Audit Trail).
 * Registra eventos de conformidade, alterações financeiras e operações sensíveis de ponta a ponta.
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    private final AuditoriaEventoRepository auditoriaRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registra um evento de auditoria simples com detalhes textuais.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaEvento registrar(String tipoAcao, String tabelaNome, String registroId, String detalhes) {
        return registrarInterno(tipoAcao, tabelaNome, registroId, detalhes, null, null);
    }

    /**
     * Registra um evento de auditoria completo com snapshot de estado anterior e novo (Diff).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditoriaEvento registrarComDiff(String tipoAcao, String tabelaNome, String registroId,
                                           Object dadosAnteriores, Object dadosNovos, String justificativa) {
        String jsonAntes = converterParaJson(dadosAnteriores);
        String jsonDepois = converterParaJson(dadosNovos);
        return registrarInterno(tipoAcao, tabelaNome, registroId, justificativa, jsonAntes, jsonDepois);
    }

    private AuditoriaEvento registrarInterno(String tipoAcao, String tabelaNome, String registroId,
                                            String detalhes, String dadosAnteriores, String dadosNovos) {
        try {
            Long empresaId = TenantContext.getEmpresaId();
            String clientIp = MDC.get(TenantLoggingFilter.MDC_KEY_CLIENT_IP);
            if (clientIp == null || clientIp.isBlank()) {
                clientIp = "127.0.0.1";
            }

            Long usuarioId = null;
            String nomeUsuario = "SISTEMA";

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                nomeUsuario = auth.getName();
                Optional<User> userOpt = userRepository.findByEmail(nomeUsuario);
                if (userOpt.isPresent()) {
                    usuarioId = userOpt.get().getId();
                    if (empresaId == null && userOpt.get().getEmpresa() != null) {
                        empresaId = userOpt.get().getEmpresa().getId();
                    }
                }
            }

            AuditoriaEvento evento = AuditoriaEvento.builder()
                    .empresaId(empresaId)
                    .usuarioId(usuarioId)
                    .nomeUsuario(nomeUsuario)
                    .ipOrigem(clientIp)
                    .tipoAcao(tipoAcao != null ? tipoAcao.toUpperCase() : "ACAO_GERAL")
                    .tabelaNome(tabelaNome)
                    .registroId(registroId)
                    .detalhes(detalhes)
                    .dadosAnteriores(dadosAnteriores)
                    .dadosNovos(dadosNovos)
                    .build();

            AuditoriaEvento salvo = auditoriaRepository.save(evento);
            log.info("[AUDITORIA] Ação: {} | Tabela: {} | ID: {} | Usuário: {} | IP: {} | Detalhes: {}",
                    tipoAcao, tabelaNome, registroId, nomeUsuario, clientIp, detalhes);
            return salvo;
        } catch (Exception e) {
            log.error("[AUDITORIA_ERRO] Falha ao persistir evento de auditoria: {}", e.getMessage(), e);
            return null;
        }
    }

    private String converterParaJson(Object objeto) {
        if (objeto == null) {
            return null;
        }
        if (objeto instanceof String) {
            return (String) objeto;
        }
        try {
            return objectMapper.writeValueAsString(objeto);
        } catch (Exception e) {
            log.warn("Falha ao serializar objeto para auditoria JSON: {}", e.getMessage());
            return String.valueOf(objeto);
        }
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaEvento> listarPorEmpresa(Long empresaId, Pageable pageable) {
        return auditoriaRepository.findByEmpresaIdOrderByCriadoEmDesc(empresaId, pageable);
    }
}
