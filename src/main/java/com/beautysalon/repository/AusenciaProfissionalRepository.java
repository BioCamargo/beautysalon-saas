package com.beautysalon.repository;

import com.beautysalon.model.AusenciaProfissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AusenciaProfissionalRepository extends JpaRepository<AusenciaProfissional, Long> {

    List<AusenciaProfissional> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<AusenciaProfissional> findByProfissionalIdAndEmpresaIdAndAtivoTrue(Long profissionalId, Long empresaId);
}
