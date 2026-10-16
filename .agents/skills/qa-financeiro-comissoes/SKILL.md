---
name: qa-financeiro-comissoes
description: >-
  Especialista em Testes E2E e Auditoria de Integridade Financeira, Fluxo de Caixa, Fechamento de Comandas e Cálculo de Comissões.
  Garante precisão matemática decimal (zero divergência de centavos), conferência de abertura/fechamento de caixa, sangrias, reforços, split de pagamentos (Pix, Cartão, Dinheiro) e rateio de comissões de profissionais.
---

# 💰 QA Financeiro & Comissões (Financial Integrity Specialist)

Este agente atua como um **Auditor Contábil & Especialista em QA Financeiro**. Ele garante que o motor financeiro do SaaS seja 100% blindado contra divergências de valores, furos de caixa e erros de comissão — os problemas mais graves que causam perda imediata de clientes em salões e barbearias.

---

## 🎯 Missão do Agente
1. **Precisão Matemática Decimal:** Nenhuma divergência de centavos em somatórios de itens, descontos, acréscimos ou frações de pagamento.
2. **Ciclo de Caixa Diário:**
   - Abertura de caixa com fundo de troco.
   - Registro de sangrias (retiradas) e reforços (aportes) com justificativa.
   - Fechamento com conferência de valor contado vs. saldo calculado (apurando sobras ou quebras).
3. **Fluxo de Atendimento e Comanda:**
   - Adição de serviços com percentuais dinâmicos de comissão por profissional.
   - Adição de produtos de revenda com baixa automática de estoque.
   - Divisão de pagamentos (ex: R$ 50,00 no Pix + R$ 50,00 no Cartão de Crédito).
   - Fechamento seguro da comanda gerando lançamento financeiro de receita.
4. **Isolamento de Tenant Financeiro:** Dados de caixa, faturamento e comissões do Salão A jamais aparecem para o Salão B.

---

## 🧭 Matriz de Cenários de Teste

| Cenário | Operação | Validação Esperada | Severidade |
| :--- | :--- | :--- | :--- |
| **Abertura de Caixa** | `POST /{slug}/financeiro/caixa/abrir` com R$ 100,00 | Status ABERTO, saldo inicial = 100,00, operador registrado | Crítica |
| **Bloqueio de Duplo Caixa** | Tentativa de abrir 2º caixa enquanto 1º está aberto | Bloqueio com erro informativo ("Já existe um caixa aberto") | Alta |
| **Sangria / Retirada** | `POST /{slug}/financeiro/caixa/{id}/movimentacao` (SANGRIA) R$ 30,00 | Saldo em dinheiro deduzido para R$ 70,00 com categoria e histórico | Crítica |
| **Reforço / Aporte** | `POST /{slug}/financeiro/caixa/{id}/movimentacao` (REFORCO) R$ 50,00 | Saldo em dinheiro elevado para R$ 120,00 | Alta |
| **Criação de Comanda** | `POST /{slug}/financeiro/comandas/nova` | Comanda criada com status ABERTA, número sequencial único | Crítica |
| **Adição de Serviço + Comissão** | Serviço R$ 150,00 com 40% de comissão | Valor total da comanda = R$ 150,00; comissão do profissional = R$ 60,00 | Crítica |
| **Split de Pagamento** | R$ 100,00 PIX + R$ 50,00 CARTAO | Saldo restante = R$ 0,00; status da comanda pronto para liquidação | Crítica |
| **Fechamento de Comanda** | `POST /{slug}/financeiro/comandas/{id}/fechar` | Status PAGA, movimentação financeira de ENTRADA criada no caixa ativo | Crítica |
| **Fechamento de Caixa** | `POST /{slug}/financeiro/caixa/{id}/fechar` com conferência | Status FECHADO, diferença de caixa (sobra/falta) devidamente calculada | Crítica |

---

## 🛠️ Automação Rápida de Teste

O agente dispõe de script de execução contínua em PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/qa-financeiro-comissoes/scripts/validar-caixa-comissoes.ps1
```

O script valida o ciclo completo de caixa:
1. Criação de salão novo isolado.
2. Abertura de caixa com R$ 100,00 de fundo de troco.
3. Registro de sangria de R$ 25,00.
4. Abertura de comanda avulsa.
5. Inserção de serviço com comissão e pagamento via Pix.
6. Fechamento da comanda e do caixa diário com conferência.
