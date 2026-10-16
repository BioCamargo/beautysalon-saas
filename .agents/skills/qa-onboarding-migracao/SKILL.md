---
name: qa-onboarding-migracao
description: >-
  Especialista em Testes E2E de Onboarding de Novos Estabelecimentos, Funil de Conversão SaaS e Migração/Importação de Dados em Lote (Excel/CSV).
  Valida cadastro de salão, ativação de Trial (7 dias), limites de planos, download de templates modelo e ingestão de cadastros legados com tolerância a falhas.
---

# 🚀 QA Onboarding & Migração de Dados (SaaS Growth Specialist)

Este agente atua como um **Especialista em QA de Conversão, Onboarding e Migração de Dados**. Ele garante que a primeira impressão do cliente no SaaS seja impecável: do cadastro inicial na landing page até a migração completa da base de clientes de concorrentes ou planilhas do Excel.

---

## 🎯 Missão do Agente
1. **Zero Fricção no Cadastro:** Garantir que um novo dono de salão consiga criar sua conta, receber seu slug exclusivo e acessar o painel em menos de 2 minutos.
2. **Ciclo de Vida do Trial:** Validar a correta concessão dos 7 dias gratuitos, exibição de badges informativos e proteção contra expiração indevida.
3. **Robustez de Ingestão de Dados (Excel `.xlsx` / `.csv`):**
   - Garantir compatibilidade com formatos variados gerados por outros sistemas (Trinks, Avec, AppBarber, planilhas manuais).
   - Validar sanitização de telefones com ou sem DDD, máscaras `(11) 98765-4321` e e-mails com espaços acidentais.
   - Garantir que duplicidades dentro do mesmo lote ou já existentes no banco sejam ignoradas sem interromper o salvamento dos registros válidos.
   - Fornecer relatórios de importação claros e amigáveis ao usuário.

---

## 🧭 Matriz de Cenários de Teste

### 1. Funil de Autocadastro & Trial
| Cenário | Entrada | Validação Esperada | Severidade |
| :--- | :--- | :--- | :--- |
| **Cadastro Válido** | Dados completos + plano padrão | Status HTTP 200/302, redirecionamento para `/{slug}/home`, slug único gerado | Crítica |
| **Slug Duplicado** | Estabelecimento com nome idêntico | Sufixo incremental automático ou slug único para evitar colisões | Alta |
| **Senhas Divergentes** | Senha != Confirmação de senha | Bloqueio imediato no frontend e backend com mensagem de erro clara | Média |
| **Contador de Trial** | Salão recém-criado | Badge "Teste: 7 dias" visível na topbar com modal de upgrade funcional | Alta |

### 2. Download de Modelos Oficiais
| Cenário | Formato | Validação Esperada | Severidade |
| :--- | :--- | :--- | :--- |
| **Template Excel** | `GET /{slug}/clientes/importar/modelo?formato=xlsx` | `Content-Type: application/vnd.openxmlformats...`, cabeçalhos estilizados, linhas didáticas | Alta |
| **Template CSV** | `GET /{slug}/clientes/importar/modelo?formato=csv` | `Content-Type: text/csv; charset=UTF-8`, cabeçalho delimitado por ponto-e-vírgula e **BOM UTF-8** para abertura nativa no Excel | Alta |

### 3. Ingestão e Processamento em Lote
| Cenário | Payload / Arquivo | Validação Esperada | Severidade |
| :--- | :--- | :--- | :--- |
| **Lote 100% Válido** | 50 clientes com telefones e e-mails novos | Todos salvos vinculados ao `empresa_id`, contador exato no feedback | Crítica |
| **Duplicidades de Telefone** | Planilha com 2 clientes com mesmo WhatsApp | Apenas o 1º é cadastrado; o 2º é contabilizado como duplicado ignorado | Crítica |
| **Duplicidade contra o Banco** | Cliente da planilha já existente no salão | Ignorado silenciosamente com alerta no relatório final | Crítica |
| **Linhas Sem Nome** | Linhas com telefone mas nome em branco | Descartadas com contabilização de erros e aviso didático | Alta |
| **Datas Heterogêneas** | `15/04/1992`, `1992-04-15`, `5/4/1992` | Conversão segura para `LocalDate` sem lançar `DateTimeParseException` | Alta |
| **Upload Sem Arquivo** | Formulário submetido vazio | Mensagem amigável de erro sem crash (500) | Média |

---

## 🛠️ Automação Rápida de Teste

O agente dispõe de script de execução contínua em PowerShell:
```powershell
powershell -ExecutionPolicy Bypass -File .agents/skills/qa-onboarding-migracao/scripts/validar-onboarding-importacao.ps1
```

O script executa em sequência:
1. Geração de novo tenant e proprietário isolado.
2. Login com captura de sessão e CSRF.
3. Download e integridade dos arquivos `.xlsx` e `.csv`.
4. Upload de lote misto com clientes válidos, duplicados e faltantes.
5. Verificação da persistência e renderização no banco e na interface.
