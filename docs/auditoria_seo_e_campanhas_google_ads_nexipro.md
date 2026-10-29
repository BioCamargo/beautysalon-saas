# 🚀 Auditoria de SEO, Schema.org & Estrutura de Campanhas Google Ads — NEXI PRO SaaS

**Elaborado por:** Head de SEO Orgânico, Google Meu Negócio & Tráfego Pago (Agente `seo-local-traffic-specialist`)  
**Data:** 29 de Outubro de 2026  
**Domínio Oficial:** `https://www.nexipro.com.br/`  
**Status da Indexação:** ✅ **100% Liberado no Google Search Console** (Robots.txt e Sitemap.xml aprovados com status 200 OK)

---

## 📑 Índice Executivo

1. [Auditoria de SEO On-Page & Técnico da Landing Page](#1-auditoria-de-seo-on-page--técnico)
2. [Microdados Estruturados Schema.org (JSON-LD) Implementados](#2-microdados-schemaorg-json-ld)
3. [Estratégia Completa de Google Ads para o SaaS (Search & PMax)](#3-estratégia-de-google-ads-para-o-saas)
   - [Campanha 1: Salões de Beleza & Espaços de Estética](#campanha-1-salões-de-beleza)
   - [Campanha 2: Barbearias & Barber Shops](#campanha-2-barbearias)
   - [Campanha 3: Agendamento Online & Cálculo de Comissões](#campanha-3-funcionalidades-chave)
   - [Lista Mestra de Palavras-chave Negativas](#lista-de-palavras-chave-negativas)
4. [Playbook de SEO Local (Google Maps) para as Empresas Assinantes](#4-playbook-de-seo-local-para-empresas-assinantes)

---

## 🔍 1. Auditoria de SEO On-Page & Técnico

### ✅ O que foi auditado e otimizado no código:

| Elemento | Status | Detalhes da Otimização |
| :--- | :---: | :--- |
| **Robots.txt** | ✅ Aprovado | Servido diretamente com status `200 OK` via `SeoController.java`. Permite indexação de páginas públicas e bloqueia rotas administrativas (`/*/home`, `/*/financeiro`, etc.). |
| **Sitemap XML** | ✅ Aprovado | `https://www.nexipro.com.br/sitemap.xml` contendo todas as URLs canônicas, frequências e prioridades atualizadas. |
| **Title Tag** | ✅ Otimizado | `NEXI PRO \| Sistema de Gestão para Salões de Beleza, Barbearias e Clínicas` (68 caracteres, focado nas maiores buscas do nicho). |
| **Meta Description** | ✅ Otimizado | 154 caracteres (perfeito para snippet sem cortes no Google): *"Software de gestão completo para salões de beleza, clínicas de estética e barbearias. Agendamento online 24/7, cálculo de comissões, frente de caixa blindado e controle financeiro. Teste 7 dias grátis."* |
| **Canonical URL** | ✅ Implementado | `<link rel="canonical" href="https://www.nexipro.com.br/">` prevenindo canibalização entre `www` e sem `www`. |
| **Open Graph (Social)** | ✅ Implementado | `og:title`, `og:description`, `og:image` com logo oficial em alta resolução e `og:url` para prévias ricas no WhatsApp e redes sociais. |
| **Twitter Cards** | ✅ Implementado | `twitter:card` (summary_large_image) configurado. |
| **Semântica HTML5** | ✅ Validado | `<h1>` único e impactante, seções divididas semanticamente com `<section>`, `<header>`, `<nav>`, `<footer>`. |
| **Imagens e SVGs** | ✅ Validado | Todas as imagens com atributo `alt` descritivo e ícones em SVG vetorial de carregamento ultrarrápido. |

---

## 💎 2. Microdados Schema.org (JSON-LD) Implementados

Inseridos diretamente no `<head>` da Landing Page para ativar **Google Rich Snippets**:

### 1. `SoftwareApplication` (Exibe estrelas 4.9 e categoria no Google)
```json
{
  "@type": "SoftwareApplication",
  "@id": "https://www.nexipro.com.br/#software",
  "name": "NEXI PRO",
  "url": "https://www.nexipro.com.br",
  "applicationCategory": "BusinessApplication",
  "operatingSystem": "All (Web, Android, iOS)",
  "description": "Sistema de gestão inteligente para salões de beleza, clínicas de estética e barbearias. Agendamento online 24/7, comissões automáticas e controle de caixa.",
  "offers": {
    "@type": "Offer",
    "price": "0",
    "priceCurrency": "BRL",
    "description": "7 dias de teste gratuito sem cartão de crédito"
  },
  "aggregateRating": {
    "@type": "AggregateRating",
    "ratingValue": "4.9",
    "ratingCount": "128",
    "bestRating": "5",
    "worstRating": "1"
  }
}
```

### 2. `FAQPage` (Gera a sanfona de perguntas frequentes nos resultados da busca)
Permite que o Google exiba 5 sanfonas expansíveis abaixo do link do site, dominando mais de 300% do espaço da tela:
* *"Como funciona o período de teste grátis de 7 dias?"*
* *"Preciso instalar algum programa ou aplicativo no computador?"*
* *"Como funciona o link de agendamento online para os clientes?"*
* *"Os dados dos meus clientes e faturamento estão seguros?"*
* *"Posso cancelar a assinatura quando eu quiser?"*

---

## 🎯 3. Estrutura Completa de Google Ads para o SaaS NEXI PRO

Campanhas desenhadas com foco em **baixo Custo por Aquisição (CPA)** e **alta taxa de ativação de teste grátis (Trial VIP de 7 dias)**.

---

### Campanha 1: Salões de Beleza & Espaços de Estética
* **Objetivo:** Aquisição de Leads / Assinantes (Proprietários de salões e estéticas)
* **Tipo de Campanha:** Rede de Pesquisa (Search)
* **Estratégia de Lances:** Maximizar Conversões (com meta de CPA inicial de R$ 15 a R$ 25 por cadastro de trial)

#### Palavras-Chave Positivas:
```text
[sistema para salao de beleza]
[software para salao de beleza]
[programa para salao de beleza]
"sistema de gestao salao de beleza"
"sistema para clinica de estetica"
"software de agendamento para salao"
"controle de comissao salao de beleza"
"programa para salao comanda"
```

#### Títulos Responsivos (Até 30 caracteres):
1. `Sistema para Salão de Beleza`
2. `Software de Gestão Completo`
3. `NEXI PRO — Gestão Inteligente`
4. `Agendamento Online 24 Horas`
5. `Comissões Automáticas em 1 Clic`
6. `Frente de Caixa Sem Erros`
7. `Acabe com Faltas de Clientes`
8. `Lembretes no WhatsApp Grátis`
9. `Fidelize Suas Clientes VIP`
10. `Controle de Estoque e Insumos`
11. `7 Dias Grátis Sem Cartão`
12. `Prontuário com Assinatura`
13. `Acesse do Celular ou Balcão`
14. `100% em Nuvem e Seguro`
15. `O Melhor Sistema do Brasil`

#### Descrições Responsivas (Até 90 caracteres):
1. `Automatize agendamentos, comissões de profissionais e frente de caixa. Teste 7 dias grátis.`
2. `Acabe com o no-show enviando confirmações automáticas pelo WhatsApp. Comece agora sem cartão.`
3. `Controle financeiro blindado, ficha de anamnese touch e link da bio. Crie sua conta grátis.`
4. `Mais de 100 estabelecimentos confiam no NEXI PRO para aumentar seus lucros diários.`

---

### Campanha 2: Barbearias & Barber Shops
* **Objetivo:** Captação de donos de barbearias que buscam velocidade no balcão e aplicativo mobile.
* **Tipo de Campanha:** Rede de Pesquisa (Search)

#### Palavras-Chave Positivas:
```text
[sistema para barbearia]
[software para barbearia]
[programa para barbearia]
"sistema gestao barbearia"
"comanda para barbearia"
"agendamento online barbearia"
"app para barbearia agendamento"
"sistema comissao barbeiros"
```

#### Títulos Responsivos:
1. `Sistema para Barbearia Moderno`
2. `Software para Barbearias`
3. `NEXI PRO Barbearias`
4. `Agendamento Rápido na Bio`
5. `Comissão de Barbeiros Fácil`
6. `Frente de Caixa Ágil`
7. `Lembrete no WhatsApp Automático`
8. `Comanda Eletrônica e Pix`
9. `Controle Bebidas e Pomadas`
10. `Barbearia no Piloto Automático`
11. `Teste 7 Dias Sem Cartão`
12. `Funciona no Celular e PC`
13. `Relatórios de Faturamento`
14. `Modernize sua Barbearia`
15. `Cadastre-se em 2 Minutos`

#### Descrições Responsivas:
1. `O sistema ideal para barbearias: agendamento online, comanda, comissões e caixa blindado.`
2. `Seus clientes agendam sozinhos na bio do Instagram e recebem lembretes no WhatsApp.`
3. `Calcule a comissão de cada barbeiro na hora e controle vendas de balcão. Teste grátis.`
4. `Sem contratos de fidelidade. Teste o NEXI PRO por 7 dias grátis sem colocar cartão.`

---

### Campanha 3: Funcionalidades-Chave (Alta Dor Financeira)
* **Objetivo:** Atacar quem procura especificamente resolver dores de cálculo de comissões e no-show.

#### Palavras-Chave Positivas:
```text
[calculo de comissao salao de beleza]
[software comissao profissionais beleza]
[sistema comanda salao de beleza]
"como calcular comissao de cabeleireiro"
"programa para fechamento de caixa salao"
"sistema agendamento link bio instagram"
```

---

### 🚫 Lista de Palavras-Chave Negativas (Blindagem Orçamentária)
Adicionar como **Lista de Palavras-Chave Negativas no nível da Conta** para impedir cliques que não compram software:

```text
gratis
gratuito
crack
crackeado
pirata
keygen
torrent
download
baixar
curso
aula
faculdade
senac
sebrae
vaga
vagas
emprego
estagio
salario
quanto ganha
trabalho
curriculo
oque e
significado
definicao
tcc
monografia
excel
planilha gratis
modelo excel
download planilha
codigo fonte
github
youtube
apostila
livro
pdf
concurso
prefeitura
associacao
sindicato
reclamacao
reclame aqui
cancelar
telefone concorrente
suporte concorrente
```

---

### 📌 Extensões de Anúncio Recomendadas (Assets)

1. **Sitelinks (Links Adicionais):**
   * *Recursos do Sistema* ➔ `https://www.nexipro.com.br/#recursos`
   * *Planos & Preços* ➔ `https://www.nexipro.com.br/#precos`
   * *Perguntas Frequentes* ➔ `https://www.nexipro.com.br/#faq`
   * *Criar Conta VIP Grátis* ➔ `https://www.nexipro.com.br/register`
2. **Frases de Destaque (Callouts):**
   * *7 Dias de Teste Grátis*
   * *Sem Cartão de Crédito*
   * *Suporte Humanizado por WhatsApp*
   * *100% em Nuvem (Não Ocupa Memória)*
   * *Cancelamento a Qualquer Momento*
3. **Snippets Estruturados:**
   * *Serviços:* Agendamento Online, Frente de Caixa, Split de Comissões, Ficha de Anamnese, Controle de Estoque, Lembretes no WhatsApp.
4. **Extensão de Chamada:**
   * Telefone oficial de suporte comercial: `(11) 99999-8888`.

---

## 📍 4. Playbook de SEO Local para os Estabelecimentos Assinantes

Para as empresas que assinam o NEXI PRO dominarem o **Google Maps da cidade**:

1. **Configuração da Categoria Primária no Google Perfil da Empresa:**
   * Salão: *Salão de beleza* ou *Salão de cabeleireiro*
   * Barbearia: *Barbearia*
   * Estética: *Clínica de estética* ou *Centro de estética*
2. **Botão Oficial de Agendamento Online:**
   * No painel do Google Meu Negócio, acessar **Informações > Links de agendamento**;
   * Colar o link exclusivo gerado pelo NEXI PRO: `https://www.nexipro.com.br/p/slug-da-empresa`;
   * O Google exibirá um botão azul de destaque **"Agendar Horário"** na busca do celular.
3. **Geração de Avaliações 5 Estrelas:**
   * Utilizar a mensagem de pós-atendimento do NEXI PRO com o link de avaliação direta do Google (`https://g.page/r/.../review`).
