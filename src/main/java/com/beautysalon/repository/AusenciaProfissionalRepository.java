package com.beautysalon.repository;

import com.beautysalon.model.AusenciaProfissional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AusenciaProfissionalRepository extends JpaRepository<AusenciaProfissional, Long> {

    @EntityGraph(attributePaths = {"profissional"})
    List<AusenciaProfissional> findByEmpresaIdAndAtivoTrue(Long empresaId);

    @EntityGraph(attributePaths = {"profissional"})
    List<AusenciaProfissional> findByProfissionalIdAndEmpresaIdAndAtivoTrue(Long profissionalId, Long empresaId);
}
