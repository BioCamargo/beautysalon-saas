package com.beautysalon.repository;

import com.beautysalon.model.CupomDesconto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CupomDescontoRepository extends JpaRepository<CupomDesconto, Long> {

    List<CupomDesconto> findByEmpresaIdAndAtivoTrue(Long empresaId);

    Optional<CupomDesconto> findByCodigoIgnoreCaseAndEmpresaId(String codigo, Long empresaId);

    Optional<CupomDesconto> findByIdAndEmpresaId(Long id, Long empresaId);
}
