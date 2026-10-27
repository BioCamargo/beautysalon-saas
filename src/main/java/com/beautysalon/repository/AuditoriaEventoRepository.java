package com.beautysalon.repository;

import com.beautysalon.model.AuditoriaEvento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {

    Page<AuditoriaEvento> findByEmpresaIdOrderByCriadoEmDesc(Long empresaId, Pageable pageable);

    Page<AuditoriaEvento> findByEmpresaIdAndTipoAcaoOrderByCriadoEmDesc(Long empresaId, String tipoAcao, Pageable pageable);

    List<AuditoriaEvento> findByTabelaNomeAndRegistroIdOrderByCriadoEmDesc(String tabelaNome, String registroId);

    Page<AuditoriaEvento> findAllByOrderByCriadoEmDesc(Pageable pageable);
}
