# 📖 Manual Completo do Usuário & Documentação de Funcionalidades
## BeautySalon SaaS — Sistema de Gestão para Estúdios & Salões de Beleza

---

## 🌟 Visão Geral da Plataforma

O **BeautySalon** é uma plataforma SaaS multi-tenant completa, projetada para a gestão de salões de beleza, clínicas de estética, barbearias e estúdios. O sistema une a rotina operacional diária (agendamentos, comandas e caixa) com ferramentas avançadas de inteligência financeira e retenção de clientes.

---

## 📑 Índice dos Módulos

1. [Autenticação e Multi-Tenancy](#1-autenticação-e-multi-tenancy)
2. [Painel Principal (Dashboard)](#2-painel-principal-dashboard)
3. [Agenda e Gestão de Horários](#3-agenda-e-gestão-de-horários)
4. [Gestão de Clientes, Anamnese Digital e Fotos](#4-gestão-de-clientes-anamnese-digital-e-fotos)
5. [Serviços e Ciclo de Retorno](#5-serviços-e-ciclo-de-retorno)
6. [Profissionais e Controle de Comissões](#6-profissionais-e-controle-de-comissões)
7. [Frente de Caixa, Comandas e Split de Pagamentos](#7-frente-de-caixa-comandas-e-split-de-pagamentos)
8. [Controle de Estoque e Baixa Automática](#8-controle-de-estoque-e-baixa-automática)
9. [Financeiro Avançado: DRE, Despesas e Rentabilidade](#9-financeiro-avançado-dre-despesas-e-rentabilidade)
10. [Inteligência de Negócio: Anti-Churn & Radar de Ociosidade](#10-inteligência-de-negócio-anti-churn--radar-de-ociosidade)
11. [Programa de Fidelidade e Pontuação](#11-programa-de-fidelidade-e-pontuação)
12. [Configurações da Empresa e Personalização](#12-configurações-da-empresa-e-personalização)

---

## 1. Autenticação e Multi-Tenancy

Cada estúdio possui um identificador único na URL (chamado de **slug**), garantindo o isolamento total dos dados entre empresas.

### Passo a Passo: Acesso ao Sistema
1. Acesse o endereço do seu estúdio: `https://seusite.com/{slug-do-salao}/login` (ex: `/studio-vip/login`).
2. Digite seu **E-mail** ou **Nome de Usuário** e a **Senha**.
3. Clique em **"Entrar no Sistema"**.
4. Caso tenha esquecido sua senha, use a opção *"Esqueci minha senha"* para receber o link de recuperação por e-mail.

> [!NOTE]
> Usuários com perfil `ADMIN` têm acesso irrestrito às finanças, relatórios de DRE e configurações. Usuários `RECEPCAO` ou `PROFISSIONAL` possuem acesso limitado às suas respectivas funções.

---

## 2. Painel Principal (Dashboard)

Ao efetuar login, o sistema apresenta a tela de controle com indicadores em tempo real:

- **Faturamento do Dia / Mês:** Total recebido e projetado.
- **Atendimentos do Dia:** Quantidade de clientes agendados, em atendimento e finalizados.
- **Gráficos de Desempenho:** Evolução de receitas x despesas.
- **Alertas Rápidos:** Alertas de produtos com estoque crítico e clientes aniversariantes do mês.

---

## 3. Agenda e Gestão de Horários

A agenda visual permite organizar os horários por profissional, dia ou semana.

### Passo a Passo: Criando um Novo Agendamento
1. No menu lateral, clique em **"Agenda"**.
2. Clique no botão **"Novo Agendamento"** ou clique diretamente no slot de horário vazio na grade do profissional desejado.
3. Preencha o formulário:
   - **Cliente:** Selecione um cliente existente ou clique em *"Cadastrar Rápido"*.
   - **Profissional:** Escolha o profissional que realizará o serviço.
   - **Serviço(s):** Selecione um ou mais serviços (o sistema calcula automaticamente o tempo de duração e o valor estimado).
   - **Data e Horário:** Defina o início do atendimento.
   - **Observações:** Anotações adicionais sobre o atendimento (ex: *"cliente prefere café sem açúcar"*).
4. Clique em **"Salvar Agendamento"**.

### Passo a Passo: Alterando Status ou Iniciando Atendimento
1. Localize o card do agendamento na grade.
2. Clique no agendamento para abrir os detalhes.
3. Você pode:
   - **Confirmar Presença:** Altera o status para *Confirmado*.
   - **Iniciar Atendimento / Abrir Comanda:** Cria automaticamente a comanda de consumo vinculada ao agendamento.
   - **Disparar Lembrete WhatsApp:** Abre o WhatsApp com mensagem pré-formatada de confirmação de horário.
   - **Cancelar:** Libera o horário na grade.

---

## 4. Gestão de Clientes, Anamnese Digital e Fotos

Cadastro detalhado de clientes com histórico de visitas, preferências e fichas técnicas.

### Passo a Passo: Cadastrar um Cliente
1. Acesse o menu **"Clientes"** e clique em **"Novo Cliente"**.
2. Informe **Nome Completo**, **WhatsApp/Telefone**, **E-mail**, **Data de Nascimento** (para o robô de aniversariantes) e endereço.
3. Clique em **"Salvar"**.

### Passo a Passo: Registrar Ficha Química e Anamnese com Fotos e Assinatura
Este recurso protege juridicamente o salão e garante histórico técnico de tratamentos:
1. Na lista de clientes, clique no ícone de **Prancheta/Anamnese** (<i class="bi bi-clipboard2-pulse"></i>) do cliente.
2. Clique em **"Nova Ficha de Anamnese"**.
3. Preencha os dados técnicos:
   - **Serviço Realizado:** Ex: *Mechas Loiras / Alisamento Orgânico*.
   - **Alergias & Restrições:** Informe qualquer sensibilidade relatada.
   - **Tipo de Cabelo / Pele:** Textura, espessura, curvatura ou tom de pele.
   - **Histórico Químico & Fórmula Utilizada:** Ex: *Pó descolorante 1:2 com OX 20 vol + Tonalizante 9.89*.
   - **Fotos Antes & Depois:** Faça o upload das fotos do resultado.
4. **Assinatura Digital Touchscreen:**
   - O cliente pode assinar diretamente na tela do tablet ou smartphone com o dedo/caneta stylus.
   - O sistema salva a assinatura digitalizada vinculada ao termo de ciência e consentimento.
5. Clique em **"Salvar Ficha de Anamnese"**.

---

## 5. Serviços e Ciclo de Retorno

Cadastro de serviços com controle de duração, valor, comissão e estimativa de retorno.

### Passo a Passo: Cadastrar um Serviço
1. Acesse **"Cadastros" > "Serviços"** e clique em **"Novo Serviço"**.
2. Preencha:
   - **Nome:** Ex: *Corte Feminino + Escova*.
   - **Categoria:** Ex: *Cabelo, Estética, Manicure*.
   - **Preço (R$):** Valor cobrado ao cliente.
   - **Duração (minutos):** Tempo ocupado na agenda (ex: 60 min).
   - **Comissão Padrão (%):** Percentual repassado ao profissional (ex: 40%).
   - **Dias do Ciclo de Retorno:** Intervalo estimado para o cliente refazer o serviço (ex: *30 dias para corte*, *60 dias para coloração*, *15 dias para unhas*). Isso alimenta o **Robô Anti-Churn**.
3. Clique em **"Salvar"**.

---

## 6. Profissionais e Controle de Comissões

Gestão da equipe de parceiros e colaboradores.

### Passo a Passo: Cadastro e Regras de Comissão
1. Acesse **"Cadastros" > "Profissionais"**.
2. Clique em **"Novo Profissional"**.
3. Preencha os dados cadastrais, cargo, especialidades e chave Pix para pagamentos.
4. Defina se a comissão seguirá a regra padrão do serviço ou se terá uma porcentagem personalizada.

### Passo a Passo: Fechamento e Envio de Extrato WhatsApp
1. Acesse **"Relatórios" > "Comissões"**.
2. Selecione o profissional e o período (semanal/quinzenal/mensal).
3. Visualize o total faturado, quantidade de serviços e o valor líquido a pagar.
4. Clique em **"Enviar Extrato WhatsApp"**: o sistema gera o texto formatado detalhando cada serviço e valor a receber diretamente no WhatsApp do profissional.

---

## 7. Frente de Caixa, Comandas e Split de Pagamentos

O coração da operação diária: controle de itens consumidos e recebimentos.

### Passo a Passo: Abrir e Lançar Itens na Comanda
1. Acesse **"Caixa / Comandas"**.
2. Clique em **"Nova Comanda"** e selecione o cliente (ou abra pela própria agenda).
3. Para adicionar itens:
   - Clique em **"Adicionar Serviço"** -> Escolha o serviço e o profissional executante.
   - Clique em **"Adicionar Produto"** -> Escolha produtos vendidos (ex: *Shampoo Home Care*), o que já dá baixa automática no estoque.

### Passo a Passo: Pagamento Dividido (Split) e Pix Dinâmico
1. Com a comanda aberta, clique em **"Adicionar Pagamento"**:
   - Exemplo: Conta total de **R$ 250,00**.
   - **1º Pagamento:** Forma `PIX`, Valor `100.00` -> Clique em *"Adicionar"*.
   - O sistema exibe instantaneamente o **QR Code Pix Dinâmico BACEN (BR Code)** com o valor exato para o cliente escanear.
   - **2º Pagamento:** Forma `CARTAO_CREDITO`, Valor `150.00` -> Clique em *"Adicionar"*.
2. Quando o valor pago cobrir o saldo total da comanda, o botão **"Finalizar Comanda"** fica disponível.
3. Clique em **"Finalizar Comanda"**. O sistema baixa o estoque, registra a receita no caixa e gera as comissões dos profissionais.
4. Clique em **"Imprimir Comprovante Térmico (58/80mm)"** ou **"Enviar Comprovante WhatsApp"** para entregar ao cliente.

---

## 8. Controle de Estoque e Baixa Automática

Gestão de produtos para venda balcão e produtos de uso interno (lavatório/bancada).

### Passo a Passo: Gestão de Estoque
1. Acesse **"Estoque"**.
2. Na listagem de produtos você visualiza a quantidade atual e o nível de alerta (mínimo).
3. **Entrada de Mercadoria:** Clique em *"Dar Entrada"*, informe a quantidade comprada e o custo unitário.
4. **Baixa Manual/Consumo Interno:** Registre produtos consumidos em procedimentos internos para controle do CMV (Custo de Mercadorias Vendidas).

---

## 9. Financeiro Avançado: DRE, Despesas e Rentabilidade

Visão contábil e de saúde financeira real do negócio.

### Passo a Passo: Lançar Despesas Fixas e Variáveis
1. Acesse **"Financeiro" > "Despesas"**.
2. Clique em **"Nova Despesa"**.
3. Preencha a descrição (ex: *Aluguel, Energia Elétrica, Taxas de Sistema*), categoria, valor e data de vencimento/pagamento.

### Visualizando a DRE (Demonstração do Resultado do Exercício)
1. Acesse **"Relatórios" > "DRE Gerencial"**.
2. Selecione o mês de referência.
3. Acompanhe a composição completa:
   - **(+) Receita Bruta** (Serviços + Vendas)
   - **(-) Taxas de Cartão/Meios de Pagamento**
   - **(-) CMV (Custo de Produtos Utilizados/Vendidos)**
   - **(=) Margem de Contribuição Bruta**
   - **(-) Comissões dos Profissionais**
   - **(-) Despesas Operacionais e Fixas**
   - **(=) LUCRO LÍQUIDO REAL & MARGEM (%)**

### Analisando a Rentabilidade por Serviço
Na aba **"Rentabilidade de Serviços"**, descubra quais procedimentos deixam a maior margem líquida para o salão após dedução de produtos e comissão, orientando seu foco de vendas.

---

## 10. Inteligência de Negócio: Anti-Churn & Radar de Ociosidade

Ferramentas automáticas que geram receita imediata sem gastar com anúncios.

### Passo a Passo: Usando o Robô Anti-Churn (Clientes Sumidos)
1. Acesse o menu **"Fidelização & Inteligência"**.
2. Na aba **"Robô Anti-Churn (Ciclo de Retorno)"**, o sistema lista clientes que ultrapassaram o ciclo médio do serviço que costumam fazer (ex: *"Maria fez Mechas há 75 dias e o ciclo é de 60 dias"*).
3. Ao lado de cada cliente, clique no botão **"Resgatar via WhatsApp"**.
4. Uma mensagem personalizada convidativa será aberta automaticamente no WhatsApp com o nome do cliente e o serviço que ele precisa retocar.

### Passo a Passo: Radar de Horários Ociosos & Promoção Relâmpago
1. Na mesma tela de inteligência, consulte o painel **"Radar de Ociosidade da Semana"**.
2. O sistema indica dias e horários com baixo volume de agendamentos (ex: *Terça-feira à tarde com 65% de ociosidade*).
3. Clique em **"Lançar Campanha Relâmpago"** para disparar ofertas com desconto exclusivo para horários de menor movimento.

---

## 11. Programa de Fidelidade e Pontuação

Incentive seus clientes a retornarem com frequência acumulando pontos.

### Funcionamento:
- A cada comanda finalizada, o valor pago gera pontos automaticamente na proporção configurada (ex: R$ 1,00 = 1 ponto).
- No histórico do cliente, você pode visualizar o saldo de pontos acumulados.
- Ao atingir a pontuação necessária, o operador pode clicar em **"Resgatar Recompensa"** e conceder o serviço ou brinde correspondente.

---

## 12. Configurações da Empresa e Personalização

Personalize o sistema com a identidade do seu espaço.

### Passo a Passo: Configurações Gerais
1. Acesse **"Configurações"**.
2. Configure:
   - **Nome do Salão, Telefone e Endereço**.
   - **Chave Pix da Empresa** (usada para gerar os QR Codes nas comandas).
   - **Logotipo e Cores do Tema**.
   - **Taxa Estimada de Cartão (%)** para o cálculo automático da DRE.
3. Clique em **"Salvar Alterações"**.

---

## 🔒 Dicas de Segurança e Boas Práticas

1. **Senhas Seguras:** Cada profissional/recepcionista deve ter seu próprio login para auditoria correta de lançamentos.
2. **Fechamento Diário de Caixa:** Confira os totais de dinheiro físico e comprovantes de cartão antes de encerrar o expediente.
3. **Backup e Nuvem:** Seus dados são salvos em banco de dados isolado com auditoria temporal (`createdAt` / `updatedAt`) de todas as alterações.
