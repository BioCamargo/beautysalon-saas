package com.beautysalon.Controller;

import com.beautysalon.model.ComandaItem;
import com.beautysalon.model.TenantRole;
import com.beautysalon.model.User;
import com.beautysalon.repository.ComandaItemRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantContext;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Controller exclusivo para consulta de extrato de comissões e ganhos dos profissionais parceiros.
 * Implementa DEV-03: Lei do Salão-Parceiro, garantindo isolamento total (profissional só vê seus próprios ganhos).
 */
@Controller
public class MeusGanhosController {

    private final UserRepository userRepository;
    private final ComandaItemRepository comandaItemRepository;

    public MeusGanhosController(UserRepository userRepository,
                                ComandaItemRepository comandaItemRepository) {
        this.userRepository = userRepository;
        this.comandaItemRepository = comandaItemRepository;
    }

    @GetMapping("/{slug}/meus-ganhos")
    public String meusGanhos(@PathVariable String slug,
                             @RequestParam(required = false, defaultValue = "mes") String filtroPeriodo,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                             @RequestParam(required = false) Long profissionalId,
                             Authentication authentication,
                             Model model) {

        Long empresaId = TenantContext.getEmpresaId();
        if (empresaId == null || authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        // 1. Identificar o usuário autenticado no tenant
        Optional<User> usuarioLogadoOpt = userRepository.findByUsernameAndEmpresaId(authentication.getName(), empresaId);
        if (usuarioLogadoOpt.isEmpty()) {
            return "redirect:/login";
        }

        User usuarioLogado = usuarioLogadoOpt.get();
        boolean isAdminOuOwner = usuarioLogado.getTenantRole() == TenantRole.OWNER || usuarioLogado.getTenantRole() == TenantRole.ADMIN;

        // 2. Determinar qual profissional será consultado:
        // Se for FUNCIONARIO, FORÇA estritamente o seu próprio ID (Blindagem contra invasão horizontal)
        User profissionalAlvo = usuarioLogado;
        if (isAdminOuOwner && profissionalId != null) {
            Optional<User> outroProf = userRepository.findByIdAndEmpresaId(profissionalId, empresaId);
            if (outroProf.isPresent()) {
                profissionalAlvo = outroProf.get();
            }
        }

        // 3. Delimitar períodos de tempo
        LocalDate hoje = LocalDate.now();
        LocalDateTime hojeInicio = hoje.atStartOfDay();
        LocalDateTime hojeFim = hoje.atTime(LocalTime.MAX);

        LocalDateTime semanaInicio = hoje.minusDays(6).atStartOfDay();
        LocalDateTime mesInicio = hoje.withDayOfMonth(1).atStartOfDay();
        LocalDateTime mesFim = hoje.withDayOfMonth(hoje.lengthOfMonth()).atTime(LocalTime.MAX);

        LocalDateTime inicioFiltro;
        LocalDateTime fimFiltro;

        if ("hoje".equalsIgnoreCase(filtroPeriodo)) {
            inicioFiltro = hojeInicio;
            fimFiltro = hojeFim;
        } else if ("7dias".equalsIgnoreCase(filtroPeriodo)) {
            inicioFiltro = semanaInicio;
            fimFiltro = hojeFim;
        } else if ("custom".equalsIgnoreCase(filtroPeriodo) && dataInicio != null && dataFim != null) {
            inicioFiltro = dataInicio.atStartOfDay();
            fimFiltro = dataFim.atTime(LocalTime.MAX);
        } else {
            // Padrão: Mês atual
            inicioFiltro = mesInicio;
            fimFiltro = mesFim;
            filtroPeriodo = "mes";
        }

        // 4. Buscar totais de comissão agregados
        BigDecimal ganhosHoje = comandaItemRepository.sumComissaoProfissional(empresaId, profissionalAlvo.getId(), hojeInicio, hojeFim);
        BigDecimal ganhosSemana = comandaItemRepository.sumComissaoProfissional(empresaId, profissionalAlvo.getId(), semanaInicio, hojeFim);
        BigDecimal ganhosMes = comandaItemRepository.sumComissaoProfissional(empresaId, profissionalAlvo.getId(), mesInicio, mesFim);
        BigDecimal ganhosPeriodo = comandaItemRepository.sumComissaoProfissional(empresaId, profissionalAlvo.getId(), inicioFiltro, fimFiltro);

        // 5. Buscar itens detalhados com JOIN FETCH
        List<ComandaItem> itensPeriodo = comandaItemRepository.findItensComComandaEClientePorPeriodo(
                empresaId, profissionalAlvo.getId(), inicioFiltro, fimFiltro);

        // Total faturado gerado pelo profissional no período
        BigDecimal faturamentoTotalGerado = itensPeriodo.stream()
                .map(ComandaItem::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ticket médio de comissão por procedimento
        BigDecimal ticketMedioComissao = BigDecimal.ZERO;
        if (!itensPeriodo.isEmpty()) {
            ticketMedioComissao = ganhosPeriodo.divide(BigDecimal.valueOf(itensPeriodo.size()), 2, RoundingMode.HALF_UP);
        }

        // 6. Se for Admin/Owner, carregar lista de profissionais da equipe para permitir alternar
        if (isAdminOuOwner) {
            model.addAttribute("listaEquipe", userRepository.findAllByEmpresaIdAndAtivoTrue(empresaId));
        }

        model.addAttribute("profissionalAlvo", profissionalAlvo);
        model.addAttribute("isAdminOuOwner", isAdminOuOwner);
        model.addAttribute("filtroPeriodo", filtroPeriodo);
        model.addAttribute("dataInicio", dataInicio != null ? dataInicio : inicioFiltro.toLocalDate());
        model.addAttribute("dataFim", dataFim != null ? dataFim : fimFiltro.toLocalDate());

        model.addAttribute("ganhosHoje", ganhosHoje != null ? ganhosHoje : BigDecimal.ZERO);
        model.addAttribute("ganhosSemana", ganhosSemana != null ? ganhosSemana : BigDecimal.ZERO);
        model.addAttribute("ganhosMes", ganhosMes != null ? ganhosMes : BigDecimal.ZERO);
        model.addAttribute("ganhosPeriodo", ganhosPeriodo != null ? ganhosPeriodo : BigDecimal.ZERO);
        model.addAttribute("faturamentoTotalGerado", faturamentoTotalGerado);
        model.addAttribute("ticketMedioComissao", ticketMedioComissao);
        model.addAttribute("totalAtendimentosPeriodo", itensPeriodo.size());
        model.addAttribute("itensPeriodo", itensPeriodo);

        model.addAttribute("empresaSlug", slug);
        model.addAttribute("pageTitle", "Meus Ganhos & Extrato de Comissões | NEXI PRO");
        return "profissional/meus-ganhos";
    }
}
