package com.beautysalon.repository;

import com.beautysalon.model.Comanda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComandaRepository extends JpaRepository<Comanda, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"empresa", "cliente", "caixa", "agendamento", "itens", "itens.profissional", "itens.servico", "itens.produto"})
    List<Comanda> findByEmpresaIdOrderByDataAberturaDesc(Long empresaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"empresa", "cliente", "caixa", "agendamento", "itens", "itens.profissional", "itens.servico", "itens.produto"})
    List<Comanda> findByEmpresaIdAndStatusOrderByDataAberturaDesc(Long empresaId, String status);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"empresa", "cliente", "caixa", "agendamento", "itens", "itens.profissional", "itens.servico", "itens.produto"})
    List<Comanda> findByClienteIdAndEmpresaIdOrderByDataAberturaDesc(Long clienteId, Long empresaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"empresa", "cliente", "caixa", "agendamento", "itens", "itens.profissional", "itens.servico", "itens.produto"})
    Optional<Comanda> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT c FROM Comanda c WHERE c.empresa.id = :empresaId AND c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim")
    List<Comanda> findComandasPagasPorPeriodo(@Param("empresaId") Long empresaId,
                                             @Param("inicio") LocalDateTime inicio,
                                             @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(c.valorTotal), 0) FROM Comanda c WHERE c.empresa.id = :empresaId AND c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim")
    BigDecimal sumFaturamentoPorPeriodo(@Param("empresaId") Long empresaId,
                                        @Param("inicio") LocalDateTime inicio,
                                        @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(c.totalComissoes), 0) FROM Comanda c WHERE c.empresa.id = :empresaId AND c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim")
    BigDecimal sumComissoesPorPeriodo(@Param("empresaId") Long empresaId,
                                      @Param("inicio") LocalDateTime inicio,
                                      @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(c) FROM Comanda c WHERE c.empresa.id = :empresaId AND c.status = 'PAGA' AND c.dataFechamento BETWEEN :inicio AND :fim")
    long countAtendimentosPorPeriodo(@Param("empresaId") Long empresaId,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim);
}
