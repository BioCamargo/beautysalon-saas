package com.beautysalon.repository;

import com.beautysalon.model.ServicoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoInsumoRepository extends JpaRepository<ServicoInsumo, Long> {

    List<ServicoInsumo> findByServicoIdAndEmpresaId(Long servicoId, Long empresaId);

    void deleteByServicoIdAndEmpresaId(Long servicoId, Long empresaId);
}
