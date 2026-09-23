package com.beautysalon.repository;

import com.beautysalon.model.Agendamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @EntityGraph(attributePaths = {"cliente", "servicos", "profissional"})
    List<Agendamento> findAllByEmpresaId(Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "servicos", "profissional"})
    Optional<Agendamento> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT a FROM Agendamento a JOIN a.servicos s WHERE s.id = :servicoId AND a.empresa.id = :empresaId")
    List<Agendamento> findByServicoIdAndEmpresaId(@Param("servicoId") Long servicoId, @Param("empresaId") Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "servicos", "profissional"})
    List<Agendamento> findByEmpresaIdAndProfissionalIdOrderByDataHoraAsc(Long empresaId, Long profissionalId);

    @EntityGraph(attributePaths = {"cliente", "servicos", "profissional"})
    List<Agendamento> findByEmpresaIdAndClienteIdOrderByDataHoraDesc(Long empresaId, Long clienteId);

    @Query("SELECT a FROM Agendamento a WHERE a.empresa.id = :empresaId AND a.dataHora BETWEEN :inicio AND :fim ORDER BY a.dataHora ASC")
    List<Agendamento> findByEmpresaIdAndDataHoraBetween(@Param("empresaId") Long empresaId,
                                                       @Param("inicio") java.time.LocalDateTime inicio,
                                                       @Param("fim") java.time.LocalDateTime fim);

    long countByEmpresaIdAndDataHoraBetween(Long empresaId, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    // Mantido para compatibilidade com método legado em AgendamentoServiceImpl
    @Query("SELECT a FROM Agendamento a JOIN a.servicos s WHERE s.id = :servicoId")
    List<Agendamento> findByServicoId(@Param("servicoId") Long servicoId);
}
