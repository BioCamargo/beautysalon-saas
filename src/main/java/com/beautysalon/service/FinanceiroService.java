package com.beautysalon.service;

import com.beautysalon.model.*;
import com.beautysalon.repository.*;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FinanceiroService {

    private final CaixaRepository caixaRepository;
    private final ComandaRepository comandaRepository;
    private final MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository;
    private final ServicoInsumoRepository servicoInsumoRepository;
    private final EstoqueService estoqueService;
    private final EmpresaRepository empresaRepository;
    private final ServicoRepository servicoRepository;
    private final UserRepository userRepository;
    private final WhatsAppService whatsAppService;
    private final AgendamentoRepository agendamentoRepository;

    private final PagamentoComandaRepository pagamentoComandaRepository;

    public FinanceiroService(CaixaRepository caixaRepository,
                             ComandaRepository comandaRepository,
                             MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository,
                             ServicoInsumoRepository servicoInsumoRepository,
                             EstoqueService estoqueService,
                             EmpresaRepository empresaRepository,
                             ServicoRepository servicoRepository,
                             UserRepository userRepository,
                             WhatsAppService whatsAppService,
                             AgendamentoRepository agendamentoRepository,
                             PagamentoComandaRepository pagamentoComandaRepository) {
        this.caixaRepository = caixaRepository;
        this.comandaRepository = comandaRepository;
        this.movimentacaoFinanceiraRepository = movimentacaoFinanceiraRepository;
        this.servicoInsumoRepository = servicoInsumoRepository;
        this.estoqueService = estoqueService;
        this.empresaRepository = empresaRepository;
        this.servicoRepository = servicoRepository;
        this.userRepository = userRepository;
        this.whatsAppService = whatsAppService;
        this.agendamentoRepository = agendamentoRepository;
        this.pagamentoComandaRepository = pagamentoComandaRepository;
    }

    public Optional<Caixa> buscarCaixaAberto() {
        return caixaRepository.findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(TenantContext.getEmpresaId(), "ABERTO");
    }

    public List<Caixa> listarHistoricoCaixas() {
        return caixaRepository.findByEmpresaIdOrderByDataAberturaDesc(TenantContext.getEmpresaId());
    }

    public Caixa buscarCaixaPorId(Long id) {
        return caixaRepository.findByIdAndEmpresaId(id, TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Caixa não encontrado: " + id));
    }

    public List<MovimentacaoFinanceira> listarMovimentacoesCaixa(Long caixaId) {
        return movimentacaoFinanceiraRepository.findByCaixaIdOrderByDataHoraDesc(caixaId);
    }

    @Transactional
    public Caixa abrirCaixa(BigDecimal saldoInicial, String observacoes, User operador) {
        Optional<Caixa> abertoOpt = buscarCaixaAberto();
        if (abertoOpt.isPresent()) {
            throw new IllegalStateException("Já existe um caixa aberto no momento!");
        }

        Empresa empresa = empresaRepository.findById(TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalStateException("Empresa não encontrada"));

        Caixa caixa = Caixa.builder()
                .dataAbertura(LocalDateTime.now())
                .saldoInicial(saldoInicial != null ? saldoInicial : BigDecimal.ZERO)
                .saldoFinalEsperado(saldoInicial != null ? saldoInicial : BigDecimal.ZERO)
                .status("ABERTO")
                .observacoes(observacoes)
                .operadorAbertura(operador)
                .empresa(empresa)
                .build();

        return caixaRepository.save(caixa);
    }

    @Transactional
    public Caixa fecharCaixa(Long caixaId, BigDecimal saldoFinalContado, String observacoes, User operador) {
        Caixa caixa = buscarCaixaPorId(caixaId);
        if (!"ABERTO".equals(caixa.getStatus())) {
            throw new IllegalStateException("Este caixa já está fechado!");
        }

        BigDecimal esperado = caixa.getSaldoInicial()
                .add(caixa.getTotalEntradas())
                .subtract(caixa.getTotalSaidas());

        caixa.setDataFechamento(LocalDateTime.now());
        caixa.setSaldoFinalEsperado(esperado);
        caixa.setSaldoFinalContado(saldoFinalContado != null ? saldoFinalContado : esperado);
        caixa.setDiferencaFechamento(caixa.getSaldoFinalContado().subtract(esperado));
        caixa.setStatus("FECHADO");
        caixa.setOperadorFechamento(operador);
        if (observacoes != null && !observacoes.isBlank()) {
            caixa.setObservacoes((caixa.getObservacoes() != null ? caixa.getObservacoes() + " | " : "") + observacoes);
        }

        return caixaRepository.save(caixa);
    }

    @Transactional
    public void registrarMovimentacaoCaixa(Long caixaId, String tipo, BigDecimal valor, String categoria, String descricao, User operador) {
        Caixa caixa = buscarCaixaPorId(caixaId);
        if (!"ABERTO".equals(caixa.getStatus())) {
            throw new IllegalStateException("Não é possível movimentar um caixa fechado!");
        }

        if ("SANGRIA".equals(tipo) || "DESPESA_AVULSA".equals(tipo)) {
            caixa.setTotalSaidas(caixa.getTotalSaidas().add(valor));
        } else {
            caixa.setTotalEntradas(caixa.getTotalEntradas().add(valor));
        }
        caixa.setSaldoFinalEsperado(caixa.getSaldoInicial().add(caixa.getTotalEntradas()).subtract(caixa.getTotalSaidas()));
        caixaRepository.save(caixa);

        MovimentacaoFinanceira mov = MovimentacaoFinanceira.builder()
                .caixa(caixa)
                .tipo(tipo)
                .valor(valor)
                .categoria(categoria)
                .descricao(descricao)
                .usuario(operador)
                .empresa(caixa.getEmpresa())
                .build();

        movimentacaoFinanceiraRepository.save(mov);
    }

    // ================= COMANDAS =================

    public List<Comanda> listarComandas() {
        return comandaRepository.findByEmpresaIdOrderByDataAberturaDesc(TenantContext.getEmpresaId());
    }

    public List<Comanda> listarComandasAbertas() {
        return comandaRepository.findByEmpresaIdAndStatusOrderByDataAberturaDesc(TenantContext.getEmpresaId(), "ABERTA");
    }

    public Comanda buscarComandaPorId(Long id) {
        return comandaRepository.findByIdAndEmpresaId(id, TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Comanda não encontrada com id: " + id));
    }

    @Transactional
    public Comanda criarComanda(Cliente cliente, String nomeAvulso, Agendamento agendamento, String observacoes) {
        Empresa empresa = empresaRepository.findById(TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalStateException("Empresa não encontrada"));

        Optional<Caixa> caixaOpt = buscarCaixaAberto();

        String numero = "CMD-" + System.currentTimeMillis() % 1000000;

        Comanda comanda = Comanda.builder()
                .numeroComanda(numero)
                .dataAbertura(LocalDateTime.now())
                .cliente(cliente)
                .clienteNomeAvulso(cliente != null ? cliente.getNome() : (nomeAvulso != null ? nomeAvulso : "Consumidor"))
                .caixa(caixaOpt.orElse(null))
                .agendamento(agendamento)
                .status("ABERTA")
                .observacoes(observacoes)
                .empresa(empresa)
                .build();

        // Se veio de um agendamento, importa os serviços e profissional
        if (agendamento != null && agendamento.getServicos() != null) {
            for (Servico s : agendamento.getServicos()) {
                BigDecimal comissaoPerc = s.getPercentualComissao();
                if (comissaoPerc == null && agendamento.getProfissional() != null) {
                    comissaoPerc = agendamento.getProfissional().getPercentualComissao();
                }

                ComandaItem item = ComandaItem.builder()
                        .comanda(comanda)
                        .tipo("SERVICO")
                        .descricaoItem(s.getNome())
                        .servico(s)
                        .profissional(agendamento.getProfissional())
                        .quantidade(1)
                        .precoUnitario(s.getPreco() != null ? s.getPreco() : BigDecimal.ZERO)
                        .percentualComissao(comissaoPerc != null ? comissaoPerc : BigDecimal.ZERO)
                        .build();
                item.recalcularLinha();
                comanda.getItens().add(item);
            }
            comanda.recalcularTotais();
        }

        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda adicionarItemComanda(Long comandaId, String tipo, Long servicoId, Long produtoId, Long profissionalId, int quantidade, BigDecimal precoUnitario, BigDecimal percentualComissao, User usuarioLogado) {
        Comanda comanda = buscarComandaPorId(comandaId);
        if (!"ABERTA".equals(comanda.getStatus())) {
            throw new IllegalStateException("Não é possível adicionar itens em uma comanda fechada/cancelada.");
        }

        User profissional = null;
        if (profissionalId != null) {
            profissional = userRepository.findByIdAndEmpresaId(profissionalId, TenantContext.getEmpresaId()).orElse(null);
        }

        BigDecimal percComissaoFinal = percentualComissao;
        if (percComissaoFinal == null || percComissaoFinal.compareTo(BigDecimal.ZERO) == 0) {
            if (profissional != null && profissional.getPercentualComissao() != null) {
                percComissaoFinal = profissional.getPercentualComissao();
            } else {
                percComissaoFinal = BigDecimal.ZERO;
            }
        }

        ComandaItem item = ComandaItem.builder()
                .comanda(comanda)
                .tipo(tipo)
                .quantidade(quantidade > 0 ? quantidade : 1)
                .profissional(profissional)
                .percentualComissao(percComissaoFinal)
                .build();

        if ("SERVICO".equals(tipo) && servicoId != null) {
            Servico servico = servicoRepository.findByIdAndEmpresaId(servicoId, TenantContext.getEmpresaId())
                    .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado: " + servicoId));
            item.setServico(servico);
            item.setDescricaoItem(servico.getNome());
            BigDecimal precoServico = servico.getPreco() != null ? servico.getPreco() : BigDecimal.ZERO;
            item.setPrecoUnitario(precoUnitario != null && precoUnitario.compareTo(BigDecimal.ZERO) > 0 ? precoUnitario : precoServico);
            if (servico.getPercentualComissao() != null && servico.getPercentualComissao().compareTo(BigDecimal.ZERO) > 0) {
                item.setPercentualComissao(servico.getPercentualComissao());
            }
        } else if ("PRODUTO".equals(tipo) && produtoId != null) {
            Produto prod = estoqueService.buscarPorId(produtoId);
            item.setProduto(prod);
            item.setDescricaoItem(prod.getNome());
            BigDecimal precoProd = prod.getPrecoVenda() != null ? prod.getPrecoVenda() : BigDecimal.ZERO;
            item.setPrecoUnitario(precoUnitario != null && precoUnitario.compareTo(BigDecimal.ZERO) > 0 ? precoUnitario : precoProd);
        } else {
            if (item.getPrecoUnitario() == null) {
                item.setPrecoUnitario(BigDecimal.ZERO);
            }
            if (item.getDescricaoItem() == null) {
                item.setDescricaoItem("Item");
            }
        }

        if (item.getPercentualComissao() == null) {
            item.setPercentualComissao(BigDecimal.ZERO);
        }

        item.recalcularLinha();
        comanda.getItens().add(item);
        comanda.recalcularTotais();
        return comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda removerItemComanda(Long comandaId, Long itemId) {
        Comanda comanda = buscarComandaPorId(comandaId);
        if (!"ABERTA".equals(comanda.getStatus())) {
            throw new IllegalStateException("Não é possível alterar itens em uma comanda já fechada.");
        }
        comanda.getItens().removeIf(it -> it.getId() != null && it.getId().equals(itemId));
        comanda.recalcularTotais();
        return comandaRepository.save(comanda);
    }

    @Transactional
    public PagamentoComanda adicionarPagamentoComanda(Long comandaId, String formaPagamento, BigDecimal valor, String observacao) {
        Comanda comanda = buscarComandaPorId(comandaId);
        if (!"ABERTA".equals(comanda.getStatus())) {
            throw new IllegalStateException("Não é possível adicionar pagamentos em comanda finalizada.");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        Empresa empresa = empresaRepository.findById(TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalStateException("Empresa não encontrada"));

        PagamentoComanda pagamento = PagamentoComanda.builder()
                .comanda(comanda)
                .formaPagamento(formaPagamento)
                .valor(valor)
                .observacao(observacao)
                .empresa(empresa)
                .dataHora(LocalDateTime.now())
                .build();

        comanda.getPagamentos().add(pagamento);
        pagamentoComandaRepository.save(pagamento);
        comandaRepository.save(comanda);
        return pagamento;
    }

    @Transactional
    public void removerPagamentoComanda(Long comandaId, Long pagamentoId) {
        Comanda comanda = buscarComandaPorId(comandaId);
        if (!"ABERTA".equals(comanda.getStatus())) {
            throw new IllegalStateException("Não é possível remover pagamentos de comanda finalizada.");
        }
        comanda.getPagamentos().removeIf(p -> p.getId() != null && p.getId().equals(pagamentoId));
        pagamentoComandaRepository.deleteById(pagamentoId);
        comandaRepository.save(comanda);
    }

    @Transactional
    public Comanda fecharComanda(Long comandaId, String formaPagamento, BigDecimal desconto, BigDecimal acrescimo, String cupomCodigo, User operador) {
        Comanda comanda = buscarComandaPorId(comandaId);
        if (!"ABERTA".equals(comanda.getStatus())) {
            throw new IllegalStateException("Esta comanda já foi finalizada ou cancelada.");
        }

        Optional<Caixa> caixaOpt = buscarCaixaAberto();
        if (caixaOpt.isEmpty()) {
            throw new IllegalStateException("É necessário abrir um caixa antes de receber comandas!");
        }
        Caixa caixa = caixaOpt.get();

        comanda.setCaixa(caixa);
        if (cupomCodigo != null && !cupomCodigo.isBlank()) {
            comanda.setCupomAplicado(cupomCodigo.trim().toUpperCase());
        }
        if (desconto != null) comanda.setDesconto(desconto);
        if (acrescimo != null) comanda.setAcrescimo(acrescimo);
        comanda.recalcularTotais();

        // Se houver pagamentos fracionados já lançados, valida ou gera o pagamento integral
        if (comanda.getPagamentos().isEmpty()) {
            // Pagamento único direto
            comanda.setFormaPagamento(formaPagamento);
            PagamentoComanda pagamentoUnico = PagamentoComanda.builder()
                    .comanda(comanda)
                    .formaPagamento(formaPagamento)
                    .valor(comanda.getValorTotal())
                    .empresa(comanda.getEmpresa())
                    .dataHora(LocalDateTime.now())
                    .build();
            comanda.getPagamentos().add(pagamentoUnico);
            pagamentoComandaRepository.save(pagamentoUnico);
        } else {
            // Split existente: define forma como MULTIPLO ou a lista concatenada
            comanda.setFormaPagamento("MÚLTIPLO (" + comanda.getPagamentos().size() + " parcelas)");
        }

        comanda.setStatus("PAGA");
        comanda.setDataFechamento(LocalDateTime.now());

        // Atualiza o agendamento original como CONCLUIDO
        if (comanda.getAgendamento() != null) {
            com.beautysalon.model.Agendamento ag = comanda.getAgendamento();
            ag.setStatus("CONCLUIDO");
            agendamentoRepository.save(ag);
        }

        // Baixa automática no estoque para produtos de revenda ou insumos de serviços
        for (ComandaItem item : comanda.getItens()) {
            if ("PRODUTO".equals(item.getTipo()) && item.getProduto() != null) {
                estoqueService.registrarMovimentacao(
                        item.getProduto().getId(),
                        "SAIDA_VENDA",
                        item.getQuantidade(),
                        "Venda na Comanda " + comanda.getNumeroComanda(),
                        operador
                );
            } else if ("SERVICO".equals(item.getTipo()) && item.getServico() != null) {
                // Baixa de insumos cadastrados na ficha técnica do serviço
                List<ServicoInsumo> insumos = servicoInsumoRepository.findByServicoIdAndEmpresaId(item.getServico().getId(), TenantContext.getEmpresaId());
                for (ServicoInsumo ins : insumos) {
                    estoqueService.registrarMovimentacao(
                            ins.getProduto().getId(),
                            "CONSUMO_SERVICO",
                            ins.getQuantidadeGasta() * item.getQuantidade(),
                            "Consumo no Serviço: " + item.getServico().getNome() + " (Comanda " + comanda.getNumeroComanda() + ")",
                            operador
                    );
                }
            }
        }

        // Atualiza o caixa
        caixa.setTotalEntradas(caixa.getTotalEntradas().add(comanda.getValorTotal()));
        caixa.setSaldoFinalEsperado(caixa.getSaldoInicial().add(caixa.getTotalEntradas()).subtract(caixa.getTotalSaidas()));
        caixaRepository.save(caixa);

        Comanda salva = comandaRepository.save(comanda);

        // Dispara comprovante de pagamento via WhatsApp se configurado
        try {
            whatsAppService.enviarReciboComanda(salva);
        } catch (Exception e) {
            // Ignora falha de WhatsApp para não interromper fechamento financeiro
        }

        return salva;
    }

    // ================= DRE & RENTABILIDADE =================

    @Transactional(readOnly = true)
    public com.beautysalon.DTO.DREDTO calcularDREPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);

        BigDecimal receitaServicos = BigDecimal.ZERO;
        BigDecimal receitaProdutos = BigDecimal.ZERO;
        BigDecimal descontos = BigDecimal.ZERO;
        BigDecimal comissoes = BigDecimal.ZERO;
        BigDecimal custoInsumos = BigDecimal.ZERO;
        BigDecimal custoProdutosVendidos = BigDecimal.ZERO;

        for (Comanda c : comandas) {
            if (c.getSubtotalServicos() != null) receitaServicos = receitaServicos.add(c.getSubtotalServicos());
            if (c.getSubtotalProdutos() != null) receitaProdutos = receitaProdutos.add(c.getSubtotalProdutos());
            if (c.getDesconto() != null) descontos = descontos.add(c.getDesconto());
            if (c.getTotalComissoes() != null) comissoes = comissoes.add(c.getTotalComissoes());

            for (ComandaItem item : c.getItens()) {
                if ("SERVICO".equals(item.getTipo()) && item.getServico() != null) {
                    List<ServicoInsumo> insumos = servicoInsumoRepository.findByServicoIdAndEmpresaId(item.getServico().getId(), empresaId);
                    for (ServicoInsumo ins : insumos) {
                        BigDecimal precoCusto = ins.getProduto().getPrecoCusto() != null ? ins.getProduto().getPrecoCusto() : BigDecimal.ZERO;
                        BigDecimal custoItem = precoCusto.multiply(BigDecimal.valueOf((long) ins.getQuantidadeGasta() * item.getQuantidade()));
                        custoInsumos = custoInsumos.add(custoItem);
                    }
                } else if ("PRODUTO".equals(item.getTipo()) && item.getProduto() != null) {
                    BigDecimal precoCusto = item.getProduto().getPrecoCusto() != null ? item.getProduto().getPrecoCusto() : BigDecimal.ZERO;
                    custoProdutosVendidos = custoProdutosVendidos.add(precoCusto.multiply(BigDecimal.valueOf(item.getQuantidade())));
                }
            }
        }

        BigDecimal receitaBruta = receitaServicos.add(receitaProdutos);
        BigDecimal receitaLiquida = receitaBruta.subtract(descontos);
        BigDecimal custosVariaveis = comissoes.add(custoInsumos).add(custoProdutosVendidos);
        BigDecimal margemContribuicao = receitaLiquida.subtract(custosVariaveis);
        BigDecimal margemContribuicaoPerc = receitaLiquida.compareTo(BigDecimal.ZERO) > 0
                ? margemContribuicao.multiply(BigDecimal.valueOf(100)).divide(receitaLiquida, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Despesas operacionais do período (sangrias/despesas avulsas do caixa)
        List<MovimentacaoFinanceira> movs = movimentacaoFinanceiraRepository.findAllByEmpresaIdAndDataHoraBetween(empresaId, inicio, fim);
        BigDecimal despesasOperacionais = movs.stream()
                .filter(m -> "SANGRIA".equals(m.getTipo()) || "DESPESA_AVULSA".equals(m.getTipo()))
                .map(MovimentacaoFinanceira::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal resultadoLiquido = margemContribuicao.subtract(despesasOperacionais);
        BigDecimal lucratividadePerc = receitaLiquida.compareTo(BigDecimal.ZERO) > 0
                ? resultadoLiquido.multiply(BigDecimal.valueOf(100)).divide(receitaLiquida, 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return com.beautysalon.DTO.DREDTO.builder()
                .receitaBrutaServicos(receitaServicos)
                .receitaBrutaProdutos(receitaProdutos)
                .receitaBrutaTotal(receitaBruta)
                .deducoesDescontos(descontos)
                .receitaLiquida(receitaLiquida)
                .totalComissoesProfissionais(comissoes)
                .custoInsumosServicos(custoInsumos)
                .custoProdutosVendidos(custoProdutosVendidos)
                .totalCustosVariaveis(custosVariaveis)
                .margemContribuicao(margemContribuicao)
                .margemContribuicaoPercentual(margemContribuicaoPerc)
                .despesasOperacionais(despesasOperacionais)
                .resultadoLiquido(resultadoLiquido)
                .lucratividadePercentual(lucratividadePerc)
                .build();
    }

    @Transactional(readOnly = true)
    public List<com.beautysalon.DTO.RentabilidadeServicoDTO> calcularRentabilidadeServicos(LocalDateTime inicio, LocalDateTime fim) {
        Long empresaId = TenantContext.getEmpresaId();
        List<Comanda> comandas = comandaRepository.findComandasPagasPorPeriodo(empresaId, inicio, fim);

        java.util.Map<Long, com.beautysalon.DTO.RentabilidadeServicoDTO> map = new java.util.HashMap<>();

        for (Comanda c : comandas) {
            for (ComandaItem item : c.getItens()) {
                if ("SERVICO".equals(item.getTipo()) && item.getServico() != null) {
                    Servico s = item.getServico();
                    com.beautysalon.DTO.RentabilidadeServicoDTO dto = map.computeIfAbsent(s.getId(), k ->
                            com.beautysalon.DTO.RentabilidadeServicoDTO.builder()
                                    .servicoId(s.getId())
                                    .nomeServico(s.getNome())
                                    .quantidadeExecutada(0)
                                    .faturamentoTotal(BigDecimal.ZERO)
                                    .custoInsumosTotal(BigDecimal.ZERO)
                                    .comissoesTotal(BigDecimal.ZERO)
                                    .lucroLiquidoTotal(BigDecimal.ZERO)
                                    .margemLucroPercentual(BigDecimal.ZERO)
                                    .build()
                    );

                    dto.setQuantidadeExecutada(dto.getQuantidadeExecutada() + item.getQuantidade());
                    dto.setFaturamentoTotal(dto.getFaturamentoTotal().add(item.getValorTotal()));
                    if (item.getValorComissao() != null) {
                        dto.setComissoesTotal(dto.getComissoesTotal().add(item.getValorComissao()));
                    }

                    // Calcula insumos deste serviço
                    List<ServicoInsumo> insumos = servicoInsumoRepository.findByServicoIdAndEmpresaId(s.getId(), empresaId);
                    for (ServicoInsumo ins : insumos) {
                        BigDecimal precoCusto = ins.getProduto().getPrecoCusto() != null ? ins.getProduto().getPrecoCusto() : BigDecimal.ZERO;
                        BigDecimal custo = precoCusto.multiply(BigDecimal.valueOf((long) ins.getQuantidadeGasta() * item.getQuantidade()));
                        dto.setCustoInsumosTotal(dto.getCustoInsumosTotal().add(custo));
                    }
                }
            }
        }

        // Calcula lucro e margem de cada serviço
        for (var dto : map.values()) {
            BigDecimal lucro = dto.getFaturamentoTotal()
                    .subtract(dto.getComissoesTotal())
                    .subtract(dto.getCustoInsumosTotal());
            dto.setLucroLiquidoTotal(lucro);

            if (dto.getFaturamentoTotal().compareTo(BigDecimal.ZERO) > 0) {
                dto.setMargemLucroPercentual(
                        lucro.multiply(BigDecimal.valueOf(100)).divide(dto.getFaturamentoTotal(), 2, java.math.RoundingMode.HALF_UP)
                );
            }
        }

        List<com.beautysalon.DTO.RentabilidadeServicoDTO> lista = new java.util.ArrayList<>(map.values());
        lista.sort((a, b) -> b.getLucroLiquidoTotal().compareTo(a.getLucroLiquidoTotal()));
        return lista;
    }
}
