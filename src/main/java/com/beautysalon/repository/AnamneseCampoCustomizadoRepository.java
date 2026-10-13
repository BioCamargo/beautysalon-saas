package com.beautysalon.repository;

import com.beautysalon.model.AnamneseCampoCustomizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnamneseCampoCustomizadoRepository extends JpaRepository<AnamneseCampoCustomizado, Long> {

    List<AnamneseCampoCustomizado> findAllByEmpresaIdAndAtivoTrueOrderByOrdemAscIdAsc(Long empresaId);

    List<AnamneseCampoCustomizado> findAllByEmpresaIdOrderByOrdemAscIdAsc(Long empresaId);

    Optional<AnamneseCampoCustomizado> findByIdAndEmpresaId(Long id, Long empresaId);
}
