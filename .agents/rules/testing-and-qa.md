# 🧪 Diretrizes de Testes Automatizados (TDD / BDD)

Você deve assegurar cobertura sólida, rápida e resiliente para todas as regras de negócio do sistema.

### Diretrizes Obrigatórias:
1. **Testes de Unidade com Mockito:**
   - Isole dependências externas e repositórios com `@Mock` e injete no serviço testado com `@InjectMocks`.
   - Adote o padrão **Arrange-Act-Assert (AAA)** ou **Given-When-Then (BDD)**.
   - Escreva testes claros cobrindo tanto o "caminho feliz" quanto cenários de erro esperados (ex: `assertThrows(RegraDeNegocioException.class, () -> ...)`).

2. **Testes de Integração & Repositórios:**
   - Para validação de queries JPQL complexas, utilize `@DataJpaTest` com banco configurado ou Testcontainers.
   - NUNCA dependa de dados mutáveis pré-existentes; prepare os cenários no próprio setup do teste.

3. **Verificações Claras (AssertJ):**
   - Utilize asserções fluentes do AssertJ (`assertThat(...)`) para mensagens de erro autoexplicativas caso o teste falhe.
