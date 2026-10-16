package com.beautysalon.repository;

import com.beautysalon.model.MovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuario", "caixa", "empresa"})
    List<MovimentacaoFinanceira> findByCaixaIdOrderByDataHoraDesc(Long caixaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuario", "caixa", "empresa"})
    List<MovimentacaoFinanceira> findByEmpresaIdOrderByDataHoraDesc(Long empresaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"usuario", "caixa", "empresa"})
    List<MovimentacaoFinanceira> findAllByEmpresaIdAndDataHoraBetween(Long empresaId, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
