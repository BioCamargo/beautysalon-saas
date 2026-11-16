package com.beautysalon.repository;

import com.beautysalon.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // Queries com isolamento por empresa
    List<Cliente> findAllByEmpresaId(Long empresaId);

    Optional<Cliente> findByIdAndEmpresaId(Long id, Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND LOWER(c.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    List<Cliente> buscarPorNomeParcialEEmpresa(@Param("nome") String nome, @Param("empresaId") Long empresaId);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND c.dataNascimento IS NOT NULL AND EXTRACT(MONTH FROM c.dataNascimento) = :mes ORDER BY EXTRACT(DAY FROM c.dataNascimento) ASC")
    List<Cliente> findAniversariantesDoMes(@Param("empresaId") Long empresaId, @Param("mes") int mes);

    @Query("SELECT c FROM Cliente c WHERE c.empresa.id = :empresaId AND c.dataNascimento IS NOT NULL AND EXTRACT(MONTH FROM c.dataNascimento) = :mes AND EXTRACT(DAY FROM c.dataNascimento) = :dia")
    List<Cliente> findAniversariantesDoDia(@Param("empresaId") Long empresaId, @Param("mes") int mes, @Param("dia") int dia);

    Optional<Cliente> findFirstByTelefoneAndEmpresaId(String telefone, Long empresaId);

    Optional<Cliente> findFirstByEmailAndEmpresaId(String email, Long empresaId);

    boolean existsByTelefoneAndEmpresaId(String telefone, Long empresaId);

    boolean existsByEmailAndEmpresaId(String email, Long empresaId);

    List<Cliente> findByTelefoneAndEmpresaId(String telefone, Long empresaId);

    long countByEmpresaId(Long empresaId);
}
