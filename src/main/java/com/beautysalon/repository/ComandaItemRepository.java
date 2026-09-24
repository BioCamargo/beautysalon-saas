package com.beautysalon.repository;

import com.beautysalon.model.ComandaItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComandaItemRepository extends JpaRepository<ComandaItem, Long> {

    List<ComandaItem> findByComandaId(Long comandaId);

    @Query("SELECT i FROM ComandaItem i WHERE i.comanda.empresa.id = :empresaId AND i.profissional.id = :profissionalId AND i.comanda.status = 'PAGA' AND i.comanda.dataFechamento BETWEEN :inicio AND :fim")
    List<ComandaItem> findItensPorProfissionalEPeriodo(@Param("empresaId") Long empresaId,
                                                      @Param("profissionalId") Long profissionalId,
                                                      @Param("inicio") LocalDateTime inicio,
                                                      @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(i.valorComissao), 0) FROM ComandaItem i WHERE i.comanda.empresa.id = :empresaId AND i.profissional.id = :profissionalId AND i.comanda.status = 'PAGA' AND i.comanda.dataFechamento BETWEEN :inicio AND :fim")
    BigDecimal sumComissaoProfissional(@Param("empresaId") Long empresaId,
                                       @Param("profissionalId") Long profissionalId,
                                       @Param("inicio") LocalDateTime inicio,
                                       @Param("fim") LocalDateTime fim);

    @Query("SELECT i.descricaoItem as nome, SUM(i.quantidade) as qtd, SUM(i.valorTotal) as total FROM ComandaItem i WHERE i.comanda.empresa.id = :empresaId AND i.comanda.status = 'PAGA' GROUP BY i.descricaoItem ORDER BY SUM(i.quantidade) DESC")
    List<Object[]> findTopItensMaisVendidos(@Param("empresaId") Long empresaId);
}
