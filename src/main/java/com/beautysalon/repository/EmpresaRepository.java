package com.beautysalon.repository;

import com.beautysalon.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findBySlug(String slug);

    Optional<Empresa> findByEmail(String email);

    Optional<Empresa> findByAsaasCustomerId(String asaasCustomerId);

    Optional<Empresa> findByAsaasSubscriptionId(String asaasSubscriptionId);

    boolean existsBySlug(String slug);
}
