package com.beautysalon.repository;

import com.beautysalon.model.MovimentacaoFinanceira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoFinanceiraRepository extends JpaRepository<MovimentacaoFinanceira, Long> {

    List<MovimentacaoFinanceira> findByCaixaIdOrderByDataHoraDesc(Long caixaId);

    List<MovimentacaoFinanceira> findByEmpresaIdOrderByDataHoraDesc(Long empresaId);
}
