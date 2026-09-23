package com.beautysalon.repository;

import com.beautysalon.model.Produto;
import com.beautysalon.model.TipoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByEmpresaIdAndAtivoTrue(Long empresaId);

    List<Produto> findByEmpresaIdAndTipoAndAtivoTrue(Long empresaId, TipoProduto tipo);

    Optional<Produto> findByIdAndEmpresaId(Long id, Long empresaId);

    /**
     * Busca produtos onde a quantidade em estoque é menor ou igual ao estoque mínimo (Estoque Baixo).
     */
    @Query("SELECT p FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo ORDER BY p.quantidadeEstoque ASC")
    List<Produto> findProdutosEstoqueBaixo(@Param("empresaId") Long empresaId);

    /**
     * Soma o valor financeiro total parado em estoque (quantidade * precoCusto).
     */
    @Query("SELECT COALESCE(SUM(p.quantidadeEstoque * p.precoCusto), 0) FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true")
    BigDecimal calcularValorTotalParadoEstoque(@Param("empresaId") Long empresaId);

    long countByEmpresaIdAndAtivoTrue(Long empresaId);

    @Query("SELECT COUNT(p) FROM Produto p WHERE p.empresa.id = :empresaId AND p.ativo = true AND p.quantidadeEstoque <= p.estoqueMinimo")
    long countProdutosEstoqueBaixo(@Param("empresaId") Long empresaId);
}
