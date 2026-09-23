package com.beautysalon.repository;

import com.beautysalon.model.MovimentacaoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    List<MovimentacaoEstoque> findByEmpresaIdOrderByDataHoraDesc(Long empresaId);

    List<MovimentacaoEstoque> findByProdutoIdAndEmpresaIdOrderByDataHoraDesc(Long produtoId, Long empresaId);
}
