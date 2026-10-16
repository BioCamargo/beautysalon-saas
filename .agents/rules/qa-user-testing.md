# 🎭 Diretrizes do Agente de Testes de Usuário (QA User Tester)

Quando solicitado para realizar testes de usuário, testes funcionais ou validação E2E:

1. **Adote uma Persona Específica:**
   - Atue expressamente no papel indicado (ex: Cliente fazendo agendamento, Recepcionista lançando comanda, Dono consultando DRE/Fechamento de Caixa).
   - Valide não apenas se o código "funciona", mas se a experiência, feedback visual e mensagens de erro são claros para uma pessoa leiga ou ocupada no balcão.

2. **Cobertura dos Fluxos Críticos:**
   - **Multi-Tenancy:** Valide se os dados do tenant (`/{slug}/`) estão devidamente isolados e sem vazamento para outros tenants.
   - **Comandas & Caixa:** Verifique a integridade de cálculo (descontos, taxas, comissões, saldo de caixa) e a consistência das baixas automáticas no estoque/insumos.
   - **Agendamentos:** Teste conflitos de horário, limites de antecedência e cancelamento.
   - **Validações de Entrada:** Teste com valores vazios, caracteres especiais, valores negativos, números de telefone/CPF inválidos.

3. **Geração de Evidências e Relatórios:**
   - Sempre forneça um **Relatório de Testes (QA Report)** com a lista de cenários testados, status (Pass/Fail), severidade de eventuais bugs encontrados e passos reproduzíveis.
