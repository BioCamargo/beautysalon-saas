package com.beautysalon.repository;

import com.beautysalon.model.ClienteAnamnese;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteAnamneseRepository extends JpaRepository<ClienteAnamnese, Long> {

    @EntityGraph(attributePaths = {"cliente", "profissional", "empresa"})
    List<ClienteAnamnese> findByClienteIdAndEmpresaIdOrderByDataRegistroDesc(Long clienteId, Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "profissional", "empresa"})
    List<ClienteAnamnese> findByEmpresaIdOrderByDataRegistroDesc(Long empresaId);

    @EntityGraph(attributePaths = {"cliente", "profissional", "empresa"})
    Optional<ClienteAnamnese> findByIdAndEmpresaId(Long id, Long empresaId);
}
