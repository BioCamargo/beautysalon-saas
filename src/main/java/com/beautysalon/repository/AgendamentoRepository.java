package com.beautysalon.repository;

import com.beautysalon.model.Agendamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @EntityGraph(attributePaths = { "cliente", "servicos", "profissional" })
    List<Agendamento> findAllByEmpresaId(Long empresaId);

    @EntityGraph(attributePaths = { "cliente", "servicos", "profissional" })
    Optional<Agendamento> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT a FROM Agendamento a JOIN a.servicos s WHERE s.id = :servicoId AND a.empresa.id = :empresaId")
    List<Agendamento> findByServicoIdAndEmpresaId(@Param("servicoId") Long servicoId,
            @Param("empresaId") Long empresaId);

    @EntityGraph(attributePaths = { "cliente", "servicos", "profissional" })
    List<Agendamento> findByEmpresaIdAndProfissionalIdOrderByDataHoraAsc(Long empresaId, Long profissionalId);

    @EntityGraph(attributePaths = { "cliente", "servicos", "profissional" })
    List<Agendamento> findByEmpresaIdAndClienteIdOrderByDataHoraDesc(Long empresaId, Long clienteId);

    @EntityGraph(attributePaths = { "cliente", "servicos", "profissional" })
    @Query("SELECT a FROM Agendamento a WHERE a.empresa.id = :empresaId AND a.dataHora BETWEEN :inicio AND :fim ORDER BY a.dataHora ASC")
    List<Agendamento> findByEmpresaIdAndDataHoraBetween(@Param("empresaId") Long empresaId,
            @Param("inicio") java.time.LocalDateTime inicio,
            @Param("fim") java.time.LocalDateTime fim);

    long countByEmpresaIdAndDataHoraBetween(Long empresaId, java.time.LocalDateTime inicio,
            java.time.LocalDateTime fim);

    @EntityGraph(attributePaths = { "empresa", "cliente", "servicos", "profissional" })
    List<Agendamento> findByStatusAndLembreteEnviadoFalseAndDataHoraBetween(String status, java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    @EntityGraph(attributePaths = { "empresa", "cliente", "servicos", "profissional" })
    @Query("SELECT a FROM Agendamento a WHERE a.empresa.id = :empresaId AND a.dataHora >= :aPartirDe AND a.status IN ('AGENDADO', 'CONFIRMADO') ORDER BY a.dataHora ASC")
    List<Agendamento> findProximosAgendamentosPorEmpresa(@Param("empresaId") Long empresaId, @Param("aPartirDe") java.time.LocalDateTime aPartirDe);

    @EntityGraph(attributePaths = { "empresa", "cliente", "servicos", "profissional" })
    @Query("SELECT a FROM Agendamento a WHERE a.dataHora >= :aPartirDe AND a.status IN ('AGENDADO', 'CONFIRMADO') ORDER BY a.dataHora ASC")
    List<Agendamento> findProximosAgendamentosGlobais(@Param("aPartirDe") java.time.LocalDateTime aPartirDe);
}
