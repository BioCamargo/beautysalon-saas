package com.beautysalon.repository;

import com.beautysalon.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findAllByEmpresaId(Long empresaId);

    Optional<Servico> findByIdAndEmpresaId(Long id, Long empresaId);
}
