package com.beautysalon.repository;

import com.beautysalon.model.PagamentoComanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PagamentoComandaRepository extends JpaRepository<PagamentoComanda, Long> {

    List<PagamentoComanda> findByComandaIdAndEmpresaId(Long comandaId, Long empresaId);

    List<PagamentoComanda> findByEmpresaIdAndDataHoraBetween(Long empresaId, LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT p.formaPagamento, SUM(p.valor) FROM PagamentoComanda p WHERE p.empresa.id = :empresaId AND p.dataHora BETWEEN :inicio AND :fim GROUP BY p.formaPagamento")
    List<Object[]> totalPorFormaPagamentoPeriodo(@Param("empresaId") Long empresaId,
                                                @Param("inicio") LocalDateTime inicio,
                                                @Param("fim") LocalDateTime fim);

    @Query("SELECT p.formaPagamento, SUM(p.valor) FROM PagamentoComanda p WHERE p.comanda.caixa.id = :caixaId GROUP BY p.formaPagamento")
    List<Object[]> totalPorFormaPagamentoCaixa(@Param("caixaId") Long caixaId);
}
