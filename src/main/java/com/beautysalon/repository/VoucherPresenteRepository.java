package com.beautysalon.repository;

import com.beautysalon.model.VoucherPresente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherPresenteRepository extends JpaRepository<VoucherPresente, Long> {

    List<VoucherPresente> findByEmpresaIdOrderByValidadeDesc(Long empresaId);

    Optional<VoucherPresente> findByCodigoIgnoreCaseAndEmpresaId(String codigo, Long empresaId);

    Optional<VoucherPresente> findByIdAndEmpresaId(Long id, Long empresaId);
}
