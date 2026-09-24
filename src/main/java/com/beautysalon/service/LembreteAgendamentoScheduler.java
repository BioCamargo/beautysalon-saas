package com.beautysalon.service;

import com.beautysalon.model.Agendamento;
import com.beautysalon.model.WhatsAppConfig;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.WhatsAppConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Rotina automática para envio de lembretes de agendamento via WhatsApp
 * para clientes com horários marcados para o dia seguinte ou nas próximas horas.
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
     * Executa todos os dias às 08:00 para enviar lembretes do dia aos clientes.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void dispararLembretesDoDia() {
        logger.info("Iniciando rotina diária de lembretes de agendamento...");
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
                if ("AGENDADO".equals(ag.getStatus()) && ag.getCliente() != null && ag.getCliente().getTelefone() != null) {
                    try {
                        String servicoNome = (ag.getServicos() != null && !ag.getServicos().isEmpty())
                                ? ag.getServicos().get(0).getNome() : "Atendimento";
                        String profissionalNome = ag.getProfissional() != null ? " com " + ag.getProfissional().getNome() : "";
                        String horaFormatada = ag.getDataHora().format(DateTimeFormatter.ofPattern("HH:mm"));

                        String msg = String.format(
                                "⏰ *Lembrete de Atendimento - %s*\n\nOlá, *%s*! ✨\nPassando para lembrar do seu horário hoje para *%s*%s às *%s*.\n\nEstamos preparando tudo para te receber! Se precisar de algo, só nos avisar. 💕",
                                config.getEmpresa().getNome(),
                                ag.getCliente().getNome(),
                                servicoNome,
                                profissionalNome,
                                horaFormatada
                        );

                        whatsAppService.enviarMensagemTexto(config, ag.getCliente().getTelefone(), msg);
                        logger.info("Lembrete enviado para {} ({})", ag.getCliente().getNome(), ag.getCliente().getTelefone());
                    } catch (Exception e) {
                        logger.error("Erro ao enviar lembrete para agendamento #{}: {}", ag.getId(), e.getMessage());
                    }
                }
            }
        }
    }
}
