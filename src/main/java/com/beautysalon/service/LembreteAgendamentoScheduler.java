package com.beautysalon.service;

import com.beautysalon.model.Agendamento;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Rotina automática para envio de lembretes de agendamento via WhatsApp
 * para clientes com horários marcados para o dia e lembrete inteligente 2 horas antes (Anti-No-Show).
 */
@Service
public class LembreteAgendamentoScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LembreteAgendamentoScheduler.class);

    private final AgendamentoRepository agendamentoRepository;
    private final WhatsAppConfigRepository whatsAppConfigRepository;
    private final WhatsAppService whatsAppService;

    public LembreteAgendamentoScheduler(AgendamentoRepository agendamentoRepository,
                                        WhatsAppConfigRepository whatsAppConfigRepository,
                                        WhatsAppService whatsAppService) {
        this.agendamentoRepository = agendamentoRepository;
        this.whatsAppConfigRepository = whatsAppConfigRepository;
        this.whatsAppService = whatsAppService;
    }

    /**
     * Executa todos os dias às 08:00 para enviar lembretes matinais do dia aos clientes.
     */
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void dispararLembretesDoDia() {
        logger.info("Iniciando rotina diária de lembretes matinais de agendamento...");
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio = hoje.atStartOfDay();
        LocalDateTime fim = hoje.atTime(LocalTime.MAX);

        List<WhatsAppConfig> configs = whatsAppConfigRepository.findAll();
        for (WhatsAppConfig config : configs) {
            if (!config.isAtivo() || !config.isNotificarLembrete() || config.getEmpresa() == null) {
                continue;
            }

            Long empresaId = config.getEmpresa().getId();
            List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);

            for (Agendamento ag : agendamentos) {
                if (("AGENDADO".equals(ag.getStatus()) || "CONFIRMADO".equals(ag.getStatus()))
                        && !ag.isLembreteEnviado()
                        && ag.getCliente() != null && ag.getCliente().getTelefone() != null) {
                    try {
                        String servicoNome = (ag.getServicos() != null && !ag.getServicos().isEmpty())
                                ? ag.getServicos().get(0).getNome() : "Atendimento";
                        String profissionalNome = ag.getProfissional() != null ? " com " + ag.getProfissional().getNome() : "";
                        String horaFormatada = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                        String empresaNome = config.getEmpresa().getNome();

                        String msg;
                        if (config.getMensagemLembreteCustom() != null && !config.getMensagemLembreteCustom().isBlank()) {
                            msg = config.getMensagemLembreteCustom()
                                    .replace("{empresa}", empresaNome)
                                    .replace("{cliente}", ag.getCliente().getNome())
                                    .replace("{servico}", servicoNome)
                                    .replace("{data}", "hoje")
                                    .replace("{hora}", horaFormatada);
                        } else {
                            msg = String.format(
                                    "⏰ *Lembrete de Atendimento - %s*\n\n" +
                                    "Olá, *%s*! ✨\n" +
                                    "Passando para lembrar do seu horário hoje para *%s*%s às *%s*.\n\n" +
                                    "👉 *Responda:* \n*1* para Confirmar sua Presença \n*2* para Remarcar ou Cancelar\n\n" +
                                    "Estamos preparando tudo para te receber! 💕",
                                    empresaNome,
                                    ag.getCliente().getNome(),
                                    servicoNome,
                                    profissionalNome,
                                    horaFormatada
                            );
                        }

                        boolean enviado = whatsAppService.enviarMensagemTexto(config, ag.getCliente().getTelefone(), msg);
                        if (enviado) {
                            ag.setLembreteEnviado(true);
                            ag.setLembreteEnviadoEm(LocalDateTime.now());
                            agendamentoRepository.save(ag);
                            logger.info("Lembrete diário enviado para {} ({})", ag.getCliente().getNome(), ag.getCliente().getTelefone());
                        }
                    } catch (Exception e) {
                        logger.error("Erro ao enviar lembrete matinal para agendamento #{}: {}", ag.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Lembrete Inteligente Anti-No-Show: Executa a cada 15 minutos entre as 07h e as 21h.
     * Identifica agendamentos cuja dataHora ocorrerá nas próximas 2 horas (janela entre 1h45m e 2h15m)
     * e que ainda não receberam lembrete.
     */
    @Scheduled(cron = "0 */15 7-21 * * *")
    @Transactional
    public void dispararLembretesDuasHorasAntes() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime janelaInicio = agora.plusMinutes(105); // 1h45m à frente
        LocalDateTime janelaFim = agora.plusMinutes(135);    // 2h15m à frente

        List<Agendamento> agendamentosProximos = agendamentoRepository.findByStatusAndLembreteEnviadoFalseAndDataHoraBetween(
                "AGENDADO", janelaInicio, janelaFim
        );

        if (agendamentosProximos.isEmpty()) {
            return;
        }

        logger.info("Encontrados {} agendamentos na janela de 2 horas para disparo anti-no-show", agendamentosProximos.size());

        for (Agendamento ag : agendamentosProximos) {
            if (ag.getCliente() == null || ag.getCliente().getTelefone() == null || ag.getEmpresa() == null) {
                continue;
            }

            WhatsAppConfig config = whatsAppConfigRepository.findByEmpresaId(ag.getEmpresa().getId()).orElse(null);
            if (config == null || !config.isAtivo() || !config.isNotificarLembrete()) {
                continue;
            }

            try {
                String servicoNome = (ag.getServicos() != null && !ag.getServicos().isEmpty())
                        ? ag.getServicos().get(0).getNome() : "Atendimento";
                String profissionalNome = ag.getProfissional() != null ? ag.getProfissional().getNome() : "Nossa equipe";
                String horaFormatada = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));
                String empresaNome = ag.getEmpresa().getNome();

                String msg = String.format(
                        "⏰ *Lembrete NEXI PRO - Faltam 2 horas!*\n\n" +
                        "Olá, *%s*! ✨\n" +
                        "Faltam aproximadamente *2 horas* para o seu atendimento:\n\n" +
                        "✂️ *Procedimento:* %s\n" +
                        "👤 *Profissional:* %s\n" +
                        "⏰ *Horário:* %s\n" +
                        "📍 *Local:* %s\n\n" +
                        "👉 *Por favor, responda com rapidez:*\n" +
                        "Digite *1* para *Confirmar Presença*\n" +
                        "Digite *2* para *Avisar Atraso / Remarcar*\n\n" +
                        "Te esperamos em breve! 💕",
                        ag.getCliente().getNome(),
                        servicoNome,
                        profissionalNome,
                        horaFormatada,
                        empresaNome
                );

                boolean enviado = whatsAppService.enviarMensagemTexto(config, ag.getCliente().getTelefone(), msg);
                if (enviado) {
                    ag.setLembreteEnviado(true);
                    ag.setLembreteEnviadoEm(LocalDateTime.now());
                    agendamentoRepository.save(ag);
                    logger.info("Lembrete anti-no-show (2h antes) enviado com sucesso para {} - Agendamento #{}",
                            ag.getCliente().getNome(), ag.getId());
                }
            } catch (Exception e) {
                logger.error("Erro ao disparar lembrete 2h antes para agendamento #{}: {}", ag.getId(), e.getMessage());
            }
        }
    }
}
