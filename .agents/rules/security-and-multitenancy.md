# 🔒 Diretrizes de Segurança, Multi-Tenancy e Isolamento de Dados

Você deve garantir a integridade máxima do isolamento de dados entre empresas (Multi-tenancy) e a segurança de ponta a ponta na API e Web.

### Diretrizes Obrigatórias:
1. **Isolamento de Tenant:**
   - Toda consulta no banco de dados DEVE filtrar pelo `empresaId` ou `empresa.slug` correspondente.
   - NUNCA exponha dados ou execute updates/deletes sem validar se o registro pertence ao tenant autenticado no `TenantContext`.
   - Limpeza obrigatória do `TenantContext.clear()` após a conclusão de cada ciclo de vida de requisição.

2. **Spring Security & RBAC:**
   - Aplique proteção estrita de rotas com `@PreAuthorize` ou via `SecurityFilterChain`.
   - Mantenha tokens JWT stateless e invalide sessões expiradas.
   - Trate dados sensíveis (senhas, chaves de webhook, CPF) com mascaramento e hashing BCrypt.

3. **Prevenção de Vulnerabilidades:**
   - Evite concatenação direta de SQL/HQL (previna SQL Injection usando parâmetros nomeados em Queries JPQL/Native).
   - Proteja inputs contra XSS (validação e escape no Thymeleaf/REST).
