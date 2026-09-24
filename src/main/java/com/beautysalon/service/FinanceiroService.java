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

    public FinanceiroService(CaixaRepository caixaRepository,
                             ComandaRepository comandaRepository,
                             MovimentacaoFinanceiraRepository movimentacaoFinanceiraRepository,
                             ServicoInsumoRepository servicoInsumoRepository,
                             EstoqueService estoqueService,
                             EmpresaRepository empresaRepository,
                             ServicoRepository servicoRepository,
                             UserRepository userRepository,
                             WhatsAppService whatsAppService,
                             AgendamentoRepository agendamentoRepository) {
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
        comanda.setFormaPagamento(formaPagamento);
        if (cupomCodigo != null && !cupomCodigo.isBlank()) {
            comanda.setCupomAplicado(cupomCodigo.trim().toUpperCase());
        }
        if (desconto != null) comanda.setDesconto(desconto);
        if (acrescimo != null) comanda.setAcrescimo(acrescimo);
        comanda.recalcularTotais();
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
}
