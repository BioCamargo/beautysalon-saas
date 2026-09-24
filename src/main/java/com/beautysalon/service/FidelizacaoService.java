package com.beautysalon.service;

import com.beautysalon.model.*;
import com.beautysalon.repository.*;
import com.beautysalon.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FidelizacaoService {

    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final ComandaRepository comandaRepository;
    private final CupomDescontoRepository cupomDescontoRepository;
    private final VoucherPresenteRepository voucherPresenteRepository;
    private final PacoteComboRepository pacoteComboRepository;
    private final EmpresaRepository empresaRepository;

    public FidelizacaoService(ClienteRepository clienteRepository,
                              AgendamentoRepository agendamentoRepository,
                              ComandaRepository comandaRepository,
                              CupomDescontoRepository cupomDescontoRepository,
                              VoucherPresenteRepository voucherPresenteRepository,
                              PacoteComboRepository pacoteComboRepository,
                              EmpresaRepository empresaRepository) {
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.comandaRepository = comandaRepository;
        this.cupomDescontoRepository = cupomDescontoRepository;
        this.voucherPresenteRepository = voucherPresenteRepository;
        this.pacoteComboRepository = pacoteComboRepository;
        this.empresaRepository = empresaRepository;
    }

    public List<Cliente> listarAniversariantesDoMes(int mes) {
        return clienteRepository.findAniversariantesDoMes(TenantContext.getEmpresaId(), mes);
    }

    /**
     * Identifica clientes que não comparecem há mais de X dias (ex: 30 dias)
     * e precisam de um lembrete/convite para retorno.
     */
    public List<Map<String, Object>> buscarClientesEmRiscoRetorno(int diasAusente) {
        List<Cliente> clientes = clienteRepository.findAllByEmpresaId(TenantContext.getEmpresaId());
        List<Map<String, Object>> retornoPendente = new ArrayList<>();
        LocalDateTime limite = LocalDateTime.now().minusDays(diasAusente);

        for (Cliente cliente : clientes) {
            List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndClienteIdOrderByDataHoraDesc(
                    TenantContext.getEmpresaId(), cliente.getId());

            if (agendamentos.isEmpty()) {
                continue; // Cliente cadastrado sem histórico ainda
            }

            Agendamento ultimo = agendamentos.get(0);
            if (ultimo.getDataHora().isBefore(limite)) {
                long dias = java.time.Duration.between(ultimo.getDataHora(), LocalDateTime.now()).toDays();
                Map<String, Object> item = new HashMap<>();
                item.put("cliente", cliente);
                item.put("ultimoAtendimento", ultimo.getDataHora());
                item.put("diasAusente", dias);
                item.put("ultimoProfissional", ultimo.getProfissional() != null ? ultimo.getProfissional().getNome() : "Não informado");
                item.put("ultimoServico", !ultimo.getServicos().isEmpty() ? ultimo.getServicos().get(0).getNome() : "Geral");
                retornoPendente.add(item);
            }
        }

        retornoPendente.sort((a, b) -> Long.compare((Long) b.get("diasAusente"), (Long) a.get("diasAusente")));
        return retornoPendente;
    }

    /**
     * Sugere serviços mais frequentes e profissional preferido para um cliente com base no histórico.
     */
    public Map<String, Object> obterSugestoesEHistoricoCliente(Long clienteId) {
        Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, TenantContext.getEmpresaId())
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado"));

        List<Agendamento> agendamentos = agendamentoRepository.findByEmpresaIdAndClienteIdOrderByDataHoraDesc(
                TenantContext.getEmpresaId(), clienteId);
        List<Comanda> comandas = comandaRepository.findByClienteIdAndEmpresaIdOrderByDataAberturaDesc(
                clienteId, TenantContext.getEmpresaId());

        Map<String, Integer> freqServicos = new HashMap<>();
        Map<String, Integer> freqProfissionais = new HashMap<>();

        for (Agendamento ag : agendamentos) {
            if (ag.getProfissional() != null) {
                freqProfissionais.put(ag.getProfissional().getNome(),
                        freqProfissionais.getOrDefault(ag.getProfissional().getNome(), 0) + 1);
            }
            for (Servico s : ag.getServicos()) {
                freqServicos.put(s.getNome(), freqServicos.getOrDefault(s.getNome(), 0) + 1);
            }
        }

        String profPreferido = freqProfissionais.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("Nenhum definido");

        String servicoFrequente = freqServicos.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse("Nenhum");

        Map<String, Object> res = new HashMap<>();
        res.put("cliente", cliente);
        res.put("agendamentos", agendamentos);
        res.put("comandas", comandas);
        res.put("profissionalPreferido", profPreferido);
        res.put("servicoFrequente", servicoFrequente);
        res.put("totalVisitas", agendamentos.size());

        return res;
    }

    // ================= CUPONS, VOUCHERS E COMBOS =================

    public List<CupomDesconto> listarCupons() {
        return cupomDescontoRepository.findByEmpresaIdAndAtivoTrue(TenantContext.getEmpresaId());
    }

    @Transactional
    public CupomDesconto salvarCupom(CupomDesconto cupom) {
        if (cupom.getEmpresa() == null) {
            cupom.setEmpresa(empresaRepository.findById(TenantContext.getEmpresaId()).orElseThrow());
        }
        return cupomDescontoRepository.save(cupom);
    }

    public List<VoucherPresente> listarVouchers() {
        return voucherPresenteRepository.findByEmpresaIdOrderByValidadeDesc(TenantContext.getEmpresaId());
    }

    @Transactional
    public VoucherPresente criarVoucher(VoucherPresente voucher) {
        if (voucher.getEmpresa() == null) {
            voucher.setEmpresa(empresaRepository.findById(TenantContext.getEmpresaId()).orElseThrow());
        }
        if (voucher.getCodigo() == null || voucher.getCodigo().isBlank()) {
            voucher.setCodigo("GIFT-" + (int)(Math.random() * 900000 + 100000));
        }
        voucher.setSaldoRestante(voucher.getValorOriginal());
        return voucherPresenteRepository.save(voucher);
    }

    public List<PacoteCombo> listarCombos() {
        return pacoteComboRepository.findByEmpresaIdAndAtivoTrue(TenantContext.getEmpresaId());
    }

    @Transactional
    public PacoteCombo salvarCombo(PacoteCombo combo) {
        if (combo.getEmpresa() == null) {
            combo.setEmpresa(empresaRepository.findById(TenantContext.getEmpresaId()).orElseThrow());
        }
        return pacoteComboRepository.save(combo);
    }
}
