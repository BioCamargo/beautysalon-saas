package com.beautysalon.repository;

import com.beautysalon.model.Caixa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CaixaRepository extends JpaRepository<Caixa, Long> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"operadorAbertura", "operadorFechamento", "empresa"})
    Optional<Caixa> findFirstByEmpresaIdAndStatusOrderByDataAberturaDesc(Long empresaId, String status);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"operadorAbertura", "operadorFechamento", "empresa"})
    List<Caixa> findByEmpresaIdOrderByDataAberturaDesc(Long empresaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"operadorAbertura", "operadorFechamento", "empresa"})
    Optional<Caixa> findByIdAndEmpresaId(Long id, Long empresaId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"operadorAbertura", "operadorFechamento", "empresa"})
    @Query("SELECT c FROM Caixa c WHERE c.empresa.id = :empresaId AND c.dataAbertura BETWEEN :inicio AND :fim ORDER BY c.dataAbertura DESC")
    List<Caixa> findByEmpresaIdAndPeriodo(@Param("empresaId") Long empresaId,
                                         @Param("inicio") LocalDateTime inicio,
                                         @Param("fim") LocalDateTime fim);
}
