# ⚡ Diretrizes de Performance JPA, Banco de Dados & Concorrência

Você deve priorizar alta performance, escalabilidade e integridade transacional no Spring Data JPA e PostgreSQL.

### Diretrizes Obrigatórias:
1. **Eliminação de N+1 Queries:**
   - Em relacionamentos `@OneToMany` ou `@ManyToOne` frequentemente acessados, use `@EntityGraph(attributePaths = {...})` ou `JOIN FETCH` explícito.
   - NUNCA execute loops no Java que façam consultas unitárias ao repositório dentro do loop.

2. **Agregações Financeiras e Atômicas:**
   - Utilize funções de agregação no banco de dados (`COALESCE(SUM(...), 0)`) para evitar `NullPointerException` e carregar milhares de registros desnecessários na memória da JVM.
   - Execute operações compostas de negócio (ex: baixa de estoque + fechamento de comanda + lançamento no caixa) sob `@Transactional(rollbackFor = Exception.class)`.

3. **Paginação e Filtros REST:**
   - Em APIs públicas ou internas de listagem, sempre prefira `Pageable` e retorne `Page<DTO>` ou `Slice<DTO>` para proteger a memória da aplicação contra OutOfMemoryError.
   - Use `JpaSpecificationExecutor` para filtros dinâmicos de múltiplos parâmetros opcionais.
