package com.beautysalon.service;

import com.beautysalon.DTO.ClienteResgateDTO;
import com.beautysalon.DTO.OciosidadeAgendaDTO;
import com.beautysalon.model.Agendamento;
import com.beautysalon.model.Cliente;
import com.beautysalon.model.Servico;
import com.beautysalon.repository.AgendamentoRepository;
import com.beautysalon.repository.ClienteRepository;
import com.beautysalon.repository.ServicoRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Inteligência de Retenção de Clientes (Anti-Churn) e Preenchimento Inteligente de Agenda Ociosa.
 */
@Service
public class InteligenciaNegocioService {

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ServicoRepository servicoRepository;

    public InteligenciaNegocioService(ClienteRepository clienteRepository,
                                      AgendamentoRepository agendamentoRepository,
                                      ServicoRepository servicoRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.servicoRepository = servicoRepository;
    }

    /**
     * Identifica clientes que ultrapassaram o ciclo ideal de retorno do último serviço e não possuem agendamento futuro.
     */
    public List<ClienteResgateDTO> identificarClientesParaResgate() {
        Long empresaId = TenantContext.getEmpresaId();
        List<Cliente> clientes = clienteRepository.findAllByEmpresaId(empresaId);
        List<ClienteResgateDTO> resgates = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        for (Cliente c : clientes) {
            List<Agendamento> historico = agendamentoRepository.findByEmpresaIdAndClienteIdOrderByDataHoraDesc(empresaId, c.getId());
            if (historico.isEmpty()) {
                continue;
            }

            // Verifica se possui algum agendamento futuro já marcado
            boolean temAgendamentoFuturo = historico.stream()
                    .anyMatch(a -> a.getDataHora().toLocalDate().isAfter(hoje) ||
                            (a.getDataHora().toLocalDate().isEqual(hoje) && !"CANCELADO".equals(a.getStatus()) && !"CONCLUIDO".equals(a.getStatus())));

            if (temAgendamentoFuturo) {
                continue; // Cliente já está com horário marcado
            }

            // Pega o último atendimento concluído
            Optional<Agendamento> ultimoConcluidoOpt = historico.stream()
                    .filter(a -> a.getDataHora().toLocalDate().isBefore(hoje) || "CONCLUIDO".equals(a.getStatus()))
                    .findFirst();

            if (ultimoConcluidoOpt.isEmpty()) {
                continue;
            }

            Agendamento ultimo = ultimoConcluidoOpt.get();
            LocalDate dataUltimo = ultimo.getDataHora().toLocalDate();
            long diasPassados = ChronoUnit.DAYS.between(dataUltimo, hoje);

            // Determina o ciclo ideal do serviço realizado
            int cicloIdeal = 30; // Padrão 30 dias
            String nomeServico = "Serviço";
            Long servicoId = null;

            if (ultimo.getServicos() != null && !ultimo.getServicos().isEmpty()) {
                Servico serv = ultimo.getServicos().get(0);
                nomeServico = serv.getNome();
                servicoId = serv.getId();
                if (serv.getDiasCicloRetorno() != null && serv.getDiasCicloRetorno() > 0) {
                    cicloIdeal = serv.getDiasCicloRetorno();
                }
            }

            // Se o tempo decorrido ultrapassou o ciclo ideal estimado
            if (diasPassados >= cicloIdeal) {
                long atraso = diasPassados - cicloIdeal;
                String urgencia = atraso <= 15 ? "MODERADO" : (atraso <= 45 ? "ALTO" : "CRÍTICO");

                String profNome = ultimo.getProfissional() != null ? ultimo.getProfissional().getNome() : "nosso profissional";
                String msg = String.format(
                        "Olá %s! Já faz %d dias desde seu último procedimento de %s com %s. Que tal renovar o seu visual essa semana? Temos horários disponíveis com condições especiais!",
                        c.getNome(), diasPassados, nomeServico, profNome
                );

                resgates.add(ClienteResgateDTO.builder()
                        .clienteId(c.getId())
                        .nomeCliente(c.getNome())
                        .telefone(c.getTelefone())
                        .ultimoServicoNome(nomeServico)
                        .ultimoServicoId(servicoId)
                        .ultimoProfissionalNome(profNome)
                        .dataUltimoAtendimento(dataUltimo)
                        .diasDesdeUltimoAtendimento(diasPassados)
                        .cicloIdealDias(cicloIdeal)
                        .diasAtraso(atraso)
                        .nivelUrgencia(urgencia)
                        .mensagemSugeridaWhatsapp(msg)
                        .build());
            }
        }

        // Ordena pelos clientes com maior tempo de atraso
        resgates.sort((a, b) -> Long.compare(b.getDiasAtraso(), a.getDiasAtraso()));
        return resgates;
    }

    /**
     * Analisa os próximos 7 dias em busca de dias e horários com baixa taxa de ocupação (buracos na grade).
     */
    public List<OciosidadeAgendaDTO> analisarOciosidadeProximosDias() {
        Long empresaId = TenantContext.getEmpresaId();
        List<OciosidadeAgendaDTO> relatorio = new ArrayList<>();
        LocalDate hoje = LocalDate.now();

        // Considera capacidade padrão de 10 atendimentos por dia
        final int CAPACIDADE_ESTIMADA_DIA = 10;

        for (int i = 0; i < 7; i++) {
            LocalDate data = hoje.plusDays(i);
            LocalDateTime inicio = data.atStartOfDay();
            LocalDateTime fim = data.atTime(LocalTime.MAX);

            List<Agendamento> agendados = agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);
            long totalAtendimentos = agendados.stream()
                    .filter(a -> !"CANCELADO".equals(a.getStatus()))
                    .count();

            double ocupacao = Math.min(100.0, (totalAtendimentos / (double) CAPACIDADE_ESTIMADA_DIA) * 100.0);
            boolean oport = ocupacao < 40.0; // Menos de 40% ocupado é considerado ocioso / oportunidade

            String diaSemana = data.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("pt", "BR")).toUpperCase();

            List<String> vagos = new ArrayList<>();
            if (oport) {
                vagos.add("09:00 - 11:00 (Manhã)");
                vagos.add("14:00 - 16:30 (Tarde)");
            }

            relatorio.add(OciosidadeAgendaDTO.builder()
                    .data(data)
                    .diaSemana(diaSemana)
                    .totalHorariosDisponiveis(CAPACIDADE_ESTIMADA_DIA)
                    .totalAgendamentosMarcados((int) totalAtendimentos)
                    .taxaOcupacaoPercentual(Math.round(ocupacao * 10.0) / 10.0)
                    .oportunidadePromocao(oport)
                    .horariosVagos(vagos)
                    .build());
        }

        return relatorio;
    }
}
