package com.beautysalon.model;

/**
 * Papéis de um usuário dentro de uma empresa (tenant).
 * Substitui o modelo Role anterior por um enum simples e seguro.
 */
public enum TenantRole {
    OWNER, // Dono da empresa: acesso total, pode excluir a empresa
    ADMIN, // Administrador: gerencia usuários, clientes, serviços
    FUNCIONARIO // Funcionário: visualiza agenda e registra atendimentos
}
