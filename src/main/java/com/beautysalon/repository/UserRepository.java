package com.beautysalon.repository;

import com.beautysalon.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Busca dentro de uma empresa específica
    @EntityGraph(attributePaths = {"empresa"})
    Optional<User> findByUsernameAndEmpresaId(String username, Long empresaId);

    Optional<User> findByEmailAndEmpresaId(String email, Long empresaId);

    @EntityGraph(attributePaths = {"empresa"})
    List<User> findAllByEmpresaId(Long empresaId);

    @EntityGraph(attributePaths = {"empresa"})
    List<User> findAllByEmpresaIdAndAtivoTrue(Long empresaId);

    @EntityGraph(attributePaths = {"empresa"})
    Optional<User> findByIdAndEmpresaId(Long id, Long empresaId);

    // Mantido para uso no DataLoader e autenticação global (com fetch da Empresa para redirecionamento)
    @EntityGraph(attributePaths = {"empresa"})
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsernameAndEmpresaId(String username, Long empresaId);

    boolean existsByEmailAndEmpresaId(String email, Long empresaId);
}