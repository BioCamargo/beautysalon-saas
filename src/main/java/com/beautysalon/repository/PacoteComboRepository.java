package com.beautysalon.repository;

import com.beautysalon.model.PacoteCombo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacoteComboRepository extends JpaRepository<PacoteCombo, Long> {

    List<PacoteCombo> findByEmpresaIdAndAtivoTrue(Long empresaId);

    Optional<PacoteCombo> findByIdAndEmpresaId(Long id, Long empresaId);
}
