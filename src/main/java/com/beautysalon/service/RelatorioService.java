package com.beautysalon.service;

import com.beautysalon.DTO.*;
import com.beautysalon.model.*;
import com.beautysalon.repository.*;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RelatorioService {

    private final ComandaRepository comandaRepository;
    private final PagamentoComandaRepository pagamentoComandaRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ProdutoRepository produtoRepository;
    private final UserRepository userRepository;
    private final InteligenciaNegocioService inteligenciaNegocioService;

    public RelatorioService(ComandaRepository comandaRepository,
                            PagamentoComandaRepository pagamentoComandaRepository,
                            AgendamentoRepository agendamentoRepository,
                            ProdutoRepository produtoRepository,
                            UserRepository userRepository,
                            InteligenciaNegocioService inteligenciaNegocioService) {
        this.comandaRepository = comandaRepository;
        this.pagamentoComandaRepository = pagamentoComandaRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.produtoRepository = produtoRepository;
        this.userRepository = userRepository;
        this.inteligenciaNegocioService = inteligenciaNegocioService;
    }

    /**
     * Métricas de Formas de Pagamento no período (Pix, Cartão, Dinheiro, etc.)
     */
    @Transactional(readOnly = true)
    public List<MetricaFormaPagamentoDTO> obterMetricasFormasPagamento(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<PagamentoComanda> pagamentos = pagamentoComandaRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);

        // Agrupamento por forma de pagamento
        Map<String, List<PagamentoComanda>> porForma = pagamentos.stream()
                .collect(Collectors.groupingBy(p -> p.getFormaPagamento() != null ? p.getFormaPagamento() : "OUTRO"));

        BigDecimal valorTotalGeral = pagamentos.stream()
                .map(PagamentoComanda::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Se não houver pagamentos detalhados via PagamentoComanda, tenta fallback das comandas pagas
        if (pagamentos.isEmpty()) {
            List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);
            Map<String, List<Comanda>> porFormaComanda = comandas.stream()
                    .collect(Collectors.groupingBy(c -> c.getFormaPagamento() != null ? c.getFormaPagamento() : "OUTRO"));

            BigDecimal totalComandas = comandas.stream()
                    .map(Comanda::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            List<MetricaFormaPagamentoDTO> result = new ArrayList<>();
            for (var entry : porFormaComanda.entrySet()) {
                String forma = entry.getKey();
                BigDecimal total = entry.getValue().stream()
                        .map(Comanda::getValorTotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                long qtd = entry.getValue().size();

                BigDecimal perc = (totalComandas.compareTo(BigDecimal.ZERO) > 0)
                        ? total.multiply(BigDecimal.valueOf(100)).divide(totalComandas, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                result.add(MetricaFormaPagamentoDTO.builder()
                        .formaPagamento(forma)
                        .nomeFormatado(formatarNomeFormaPagamento(forma))
                        .total(total)
                        .quantidade(qtd)
                        .percentual(perc)
                        .corBadge(obterCorBadgeFormaPagamento(forma))
                        .icone(obterIconeFormaPagamento(forma))
                        .build());
            }
            result.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
            return result;
        }

        List<MetricaFormaPagamentoDTO> resultado = new ArrayList<>();
        for (var entry : porForma.entrySet()) {
            String forma = entry.getKey();
            BigDecimal total = entry.getValue().stream()
                    .map(PagamentoComanda::getValor)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            long qtd = entry.getValue().size();

            BigDecimal perc = (valorTotalGeral.compareTo(BigDecimal.ZERO) > 0)
                    ? total.multiply(BigDecimal.valueOf(100)).divide(valorTotalGeral, 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            resultado.add(MetricaFormaPagamentoDTO.builder()
                    .formaPagamento(forma)
                    .nomeFormatado(formatarNomeFormaPagamento(forma))
                    .total(total)
                    .quantidade(qtd)
                    .percentual(perc)
                    .corBadge(obterCorBadgeFormaPagamento(forma))
                    .icone(obterIconeFormaPagamento(forma))
                    .build());
        }

        resultado.sort((a, b) -> b.getTotal().compareTo(a.getTotal()));
        return resultado;
    }

    /**
     * Ranking e Desempenho Completo por Profissional no período
     */
    @Transactional(readOnly = true)
    public List<MetricaProfissionalPerformanceDTO> obterDesempenhoProfissionais(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<User> profissionais = userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId);
        List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);

        List<MetricaProfissionalPerformanceDTO> lista = new ArrayList<>();

        for (User prof : profissionais) {
            BigDecimal fatServicos = BigDecimal.ZERO;
            BigDecimal fatProdutos = BigDecimal.ZERO;
            BigDecimal totalComissao = BigDecimal.ZERO;
            long totalAtendimentos = 0;

            for (Comanda c : comandas) {
                boolean participouDaComanda = false;
                for (ComandaItem item : c.getItens()) {
                    if (item.getProfissional() != null && item.getProfissional().getId().equals(prof.getId())) {
                        participouDaComanda = true;
                        if ("SERVICO".equals(item.getTipo())) {
                            fatServicos = fatServicos.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
                        } else if ("PRODUTO".equals(item.getTipo())) {
                            fatProdutos = fatProdutos.add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO);
                        }
                        if (item.getValorComissao() != null) {
                            totalComissao = totalComissao.add(item.getValorComissao());
                        }
                    }
                }
                if (participouDaComanda) {
                    totalAtendimentos++;
                }
            }

            BigDecimal fatTotal = fatServicos.add(fatProdutos);
            BigDecimal ticketMedio = totalAtendimentos > 0
                    ? fatTotal.divide(BigDecimal.valueOf(totalAtendimentos), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            lista.add(MetricaProfissionalPerformanceDTO.builder()
                    .profissionalId(prof.getId())
                    .nome(prof.getNome())
                    .especialidade(prof.getEspecialidade() != null ? prof.getEspecialidade() : "Geral")
                    .percentualComissao(prof.getPercentualComissao() != null ? prof.getPercentualComissao() : BigDecimal.ZERO)
                    .totalAtendimentos(totalAtendimentos)
                    .faturamentoServicos(fatServicos)
                    .faturamentoProdutos(fatProdutos)
                    .faturamentoTotal(fatTotal)
                    .totalComissoes(totalComissao)
                    .ticketMedio(ticketMedio)
                    .build());
        }

        lista.sort((a, b) -> b.getFaturamentoTotal().compareTo(a.getFaturamentoTotal()));
        return lista;
    }

    /**
     * Top Clientes (Curva ABC / LTV) e Clientes para Resgate
     */
    @Transactional(readOnly = true)
    public List<MetricaClienteTopDTO> obterTopClientes(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);

        Map<Long, MetricaClienteTopDTO> map = new HashMap<>();

        for (Comanda c : comandas) {
            if (c.getCliente() != null) {
                Cliente cli = c.getCliente();
                MetricaClienteTopDTO dto = map.computeIfAbsent(cli.getId(), k ->
                        MetricaClienteTopDTO.builder()
                                .clienteId(cli.getId())
                                .nomeCliente(cli.getNome())
                                .telefone(cli.getTelefone())
                                .totalVisitas(0)
                                .totalGasto(BigDecimal.ZERO)
                                .ticketMedio(BigDecimal.ZERO)
                                .ultimaVisita(c.getDataFechamento())
                                .build()
                );

                dto.setTotalVisitas(dto.getTotalVisitas() + 1);
                dto.setTotalGasto(dto.getTotalGasto().add(c.getValorTotal() != null ? c.getValorTotal() : BigDecimal.ZERO));
                if (dto.getUltimaVisita() == null || (c.getDataFechamento() != null && c.getDataFechamento().isAfter(dto.getUltimaVisita()))) {
                    dto.setUltimaVisita(c.getDataFechamento());
                }
            }
        }

        for (var dto : map.values()) {
            if (dto.getTotalVisitas() > 0) {
                dto.setTicketMedio(dto.getTotalGasto().divide(BigDecimal.valueOf(dto.getTotalVisitas()), 2, RoundingMode.HALF_UP));
            }
        }

        List<MetricaClienteTopDTO> topList = new ArrayList<>(map.values());
        topList.sort((a, b) -> b.getTotalGasto().compareTo(a.getTotalGasto()));
        return topList.stream().limit(15).collect(Collectors.toList());
    }

    /**
     * Lista de Clientes para Resgate (Anti-churn)
     */
    @Transactional(readOnly = true)
    public List<ClienteResgateDTO> obterClientesResgate() {
        return inteligenciaNegocioService.identificarClientesParaResgate();
    }

    /**
     * Métricas de Estoque e Curva ABC de Produtos
     */
    @Transactional(readOnly = true)
    public List<MetricaProdutoEstoqueDTO> obterMetricasProdutos(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<Produto> todosProdutos = produtoRepository.findByEmpresaIdAndAtivoTrue(empresaId);
        List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);

        // Mapeia vendas no período
        Map<Long, Long> qtdVendidaMap = new HashMap<>();
        Map<Long, BigDecimal> fatVendidoMap = new HashMap<>();

        for (Comanda c : comandas) {
            for (ComandaItem item : c.getItens()) {
                if ("PRODUTO".equals(item.getTipo()) && item.getProduto() != null) {
                    Long prodId = item.getProduto().getId();
                    qtdVendidaMap.put(prodId, qtdVendidaMap.getOrDefault(prodId, 0L) + item.getQuantidade());
                    fatVendidoMap.put(prodId, fatVendidoMap.getOrDefault(prodId, BigDecimal.ZERO).add(item.getValorTotal() != null ? item.getValorTotal() : BigDecimal.ZERO));
                }
            }
        }

        List<MetricaProdutoEstoqueDTO> lista = new ArrayList<>();
        for (Produto p : todosProdutos) {
            long qtdVendida = qtdVendidaMap.getOrDefault(p.getId(), 0L);
            BigDecimal faturamento = fatVendidoMap.getOrDefault(p.getId(), BigDecimal.ZERO);
            BigDecimal precoCusto = p.getPrecoCusto() != null ? p.getPrecoCusto() : BigDecimal.ZERO;
            BigDecimal custoTotalVendido = precoCusto.multiply(BigDecimal.valueOf(qtdVendida));
            BigDecimal lucro = faturamento.subtract(custoTotalVendido);

            int estoqueAtual = p.getQuantidadeEstoque() != null ? p.getQuantidadeEstoque() : 0;
            int estoqueMin = p.getEstoqueMinimo() != null ? p.getEstoqueMinimo() : 0;
            BigDecimal valorEstoque = precoCusto.multiply(BigDecimal.valueOf(estoqueAtual));

            lista.add(MetricaProdutoEstoqueDTO.builder()
                    .produtoId(p.getId())
                    .nome(p.getNome())
                    .marca(p.getMarca() != null ? p.getMarca() : "-")
                    .tipo(p.getTipo() != null ? p.getTipo().name() : "REVENDA")
                    .quantidadeEstoque(estoqueAtual)
                    .estoqueMinimo(estoqueMin)
                    .precoCusto(precoCusto)
                    .precoVenda(p.getPrecoVenda() != null ? p.getPrecoVenda() : BigDecimal.ZERO)
                    .valorTotalEstoque(valorEstoque)
                    .quantidadeVendidaPeriodo(qtdVendida)
                    .faturamentoPeriodo(faturamento)
                    .lucroPeriodo(lucro)
                    .alertaEstoqueBaixo(estoqueAtual <= estoqueMin)
                    .build());
        }

        // Ordena por faturamento no período e depois por alerta
        lista.sort((a, b) -> {
            int comp = b.getFaturamentoPeriodo().compareTo(a.getFaturamentoPeriodo());
            if (comp != 0) return comp;
            return Integer.compare(b.getQuantidadeEstoque(), a.getQuantidadeEstoque());
        });

        return lista;
    }

    /**
     * Métricas de Conversão, Cancelamento e No-show de Agendamentos
     */
    @Transactional(readOnly = true)
    public MetricaAgendamentoDTO obterMetricasAgendamento(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);

        long total = agendamentos.size();
        long concluidos = agendamentos.stream().filter(a -> "CONCLUIDO".equalsIgnoreCase(a.getStatus())).count();
        long cancelados = agendamentos.stream().filter(a -> "CANCELADO".equalsIgnoreCase(a.getStatus())).count();
        long pendentes = total - concluidos - cancelados;

        BigDecimal taxaConversao = total > 0
                ? BigDecimal.valueOf(concluidos).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal taxaCancelamento = total > 0
                ? BigDecimal.valueOf(cancelados).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Estima perda financeira dos cancelados com base no valor dos serviços agendados
        BigDecimal perdaEstimada = agendamentos.stream()
                .filter(a -> "CANCELADO".equalsIgnoreCase(a.getStatus()))
                .map(a -> {
                    if (a.getServicos() == null || a.getServicos().isEmpty()) return BigDecimal.ZERO;
                    return a.getServicos().stream()
                            .map(s -> s.getPreco() != null ? s.getPreco() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return MetricaAgendamentoDTO.builder()
                .totalAgendados(total)
                .concluidos(concluidos)
                .cancelados(cancelados)
                .pendentes(pendentes)
                .taxaConversao(taxaConversao)
                .taxaCancelamento(taxaCancelamento)
                .perdaFinanceiraEstimada(perdaEstimada)
                .build();
    }

    private String formatarNomeFormaPagamento(String forma) {
        if (forma == null) return "Outro";
        switch (forma.toUpperCase()) {
            case "PIX": return "Pix Instantâneo";
            case "CARTAO_CREDITO": return "Cartão de Crédito";
            case "CARTAO_DEBITO": return "Cartão de Débito";
            case "DINHEIRO": return "Dinheiro em Espécie";
            case "VOUCHER": return "Voucher / Cartão Presente";
            case "MULTIPLO": return "Múltiplas Formas (Split)";
            default: return forma;
        }
    }

    private String obterCorBadgeFormaPagamento(String forma) {
        if (forma == null) return "bg-secondary";
        switch (forma.toUpperCase()) {
            case "PIX": return "bg-teal text-white";
            case "CARTAO_CREDITO": return "bg-primary text-white";
            case "CARTAO_DEBITO": return "bg-info text-dark";
            case "DINHEIRO": return "bg-success text-white";
            case "VOUCHER": return "bg-warning text-dark";
            default: return "bg-secondary text-white";
        }
    }

    private String obterIconeFormaPagamento(String forma) {
        if (forma == null) return "bi-wallet2";
        switch (forma.toUpperCase()) {
            case "PIX": return "bi-qr-code";
            case "CARTAO_CREDITO": return "bi-credit-card-2-front";
            case "CARTAO_DEBITO": return "bi-credit-card";
            case "DINHEIRO": return "bi-cash";
            case "VOUCHER": return "bi-gift";
            default: return "bi-wallet2";
        }
    }
}
