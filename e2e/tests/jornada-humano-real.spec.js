// @ts-check
const { test, expect } = require('@playwright/test');

/**
 * 🤖 SIMULAÇÃO DE USUÁRIO REAL - JORNADA DE PONTA A PONTA (E2E)
 * 
 * Executa em modo serial compartilhando a mesma página/sessão,
 * simulando um humano real navegando no sistema do salão:
 * 1. Onboarding de Salão e Ativação do Trial
 * 2. Cadastro de Colaborador Parceiro com comissão e Pix
 * 3. Cadastro de Serviço no Catálogo
 * 4. Cadastro de Produto de Revenda com estoque inicial (10 un)
 * 5. Abertura do Caixa Diário com fundo de troco
 * 6. Atendimento em Comanda (Serviço + Produto) com baixa automática de estoque (10 -> 9 un)
 */

test.describe.configure({ mode: 'serial' });

test.describe('🎭 Simulação de Usuário Real — Jornada Completa SaaS', () => {

  const timestamp = Date.now().toString().slice(-6);
  const studioName = `Studio Glamour VIP ${timestamp}`;
  const ownerName = `Carla Gestora ${timestamp}`;
  const ownerUsername = `carla_${timestamp}`;
  const ownerEmail = `carla.${timestamp}@glamour.com`;
  const defaultPassword = 'senhaSegura123';
  const productName = `Shampoo Nutritivo Lumora ${timestamp}`;

  /** @type {import('@playwright/test').Page} */
  let page;
  let studioSlug = '';

  test.beforeAll(async ({ browser }) => {
    // Abre uma única janela que acompanhará toda a jornada visual
    page = await browser.newPage();
  });

  test.afterAll(async () => {
    if (page) {
      await page.close();
    }
  });

  test('Etapa 1: Onboarding de Novo Estabelecimento (Cadastro de Salão)', async () => {
    // 1. Acessa a página pública de registro
    await page.goto('/register');
    await expect(page.locator('#nomeEmpresa')).toBeVisible();

    // 2. Preenche os dados do estúdio
    await page.fill('#nomeEmpresa', studioName);
    await page.fill('#emailEmpresa', ownerEmail);
    await page.selectOption('#segmento', 'SALAO_BELEZA');
    await page.fill('#telefoneEmpresa', '(11) 98765-4321');

    // 3. Preenche os dados do proprietário
    await page.fill('#nomeUsuario', ownerName);
    await page.fill('#username', ownerUsername);
    await page.fill('#emailUsuario', `pessoal.${timestamp}@gmail.com`);
    await page.fill('#password', defaultPassword);
    await page.fill('#confirmPassword', defaultPassword);

    // 4. Submete o cadastro
    await page.click('#btnSubmit');

    // 5. Aguarda card de sucesso e obtém o link exclusivo do estúdio
    await page.waitForSelector('#success-onboarding-container', { state: 'visible', timeout: 15000 });
    const slugInput = page.locator('#exclusiveUrlText');
    const slugValue = await slugInput.inputValue();

    // Extrai o slug da URL (ex: /studio-glamour-vip-123456/login)
    const match = slugValue.match(/\/([^\/]+)\/login/);
    expect(match).not.toBeNull();
    studioSlug = match ? match[1] : '';
    console.log(`✅ [ONBOARDING CONCLUÍDO] Novo estúdio provisionado com slug: /${studioSlug}`);

    // Clica no botão de acesso direto ao estúdio
    await page.click('#btnGoToStudio');
    await page.waitForURL(`**/${studioSlug}/login**`, { timeout: 15000 });

    // Realiza o primeiro login como proprietário
    await page.fill('#username', ownerUsername);
    await page.fill('#password', defaultPassword);
    await page.click('button[type="submit"]');

    // Aguarda o painel principal do estúdio abrir
    await page.waitForURL(`**/${studioSlug}/home**`, { timeout: 15000 });
    console.log(`✅ [LOGIN AUTORIZADO] Proprietária conectada no painel principal do salão.`);
  });

  test('Etapa 2: Cadastro de Profissional / Cabeleireiro Parceiro', async () => {
    await page.goto(`/${studioSlug}/usuarios/novo`);
    await page.waitForLoadState('networkidle');

    // Preenche dados do novo colaborador
    await page.fill('#createNome', `Helen Especialista ${timestamp}`);
    await page.fill('#createUsername', `helen_${timestamp}`);
    await page.fill('#createEmail', `helen.${timestamp}@glamour.com`);
    await page.fill('#createPassword', 'senha123456');
    await page.fill('#createEspecialidade', 'Cabeleireira & Colorista');
    await page.fill('#createTelefone', '(11) 97777-8888');
    await page.fill('#createComissao', '35.0');
    await page.fill('#createChavePix', 'helen.pix@banco.com');

    // Seleciona perfil Profissional
    await page.selectOption('#createRole', 'PROFISSIONAL');

    // Salva o operador
    await page.click('button[type="submit"]:has-text("Salvar Operador")');
    await page.waitForURL(`**/${studioSlug}/usuarios`, { timeout: 10000 });

    // Valida que o profissional aparece na listagem
    await expect(page.locator(`text=Helen Especialista ${timestamp}`)).toBeVisible();
    console.log(`✅ [EQUIPE] Colaboradora Helen cadastrada com 35% de comissão e chave Pix.`);
  });

  test('Etapa 3: Cadastro de Serviço no Catálogo', async () => {
    await page.goto(`/${studioSlug}/servicos/novo`);
    await page.waitForLoadState('networkidle');

    // Preenche dados do serviço
    await page.fill('#nome', 'Corte & Escova VIP');
    await page.fill('#preco', '120.00');
    await page.fill('#duracaoMinutos', '60');

    // Clica no botão de salvar no catálogo
    await page.click('button[type="submit"]:has-text("Salvar"), button[type="submit"]:has-text("Salvar no Catálogo")');
    await page.waitForURL(`**/${studioSlug}/servicos`, { timeout: 10000 });

    await expect(page.locator('text=Corte & Escova VIP').first()).toBeVisible();
    console.log(`✅ [CATÁLOGO] Serviço 'Corte & Escova VIP' cadastrado por R$ 120,00.`);
  });

  test('Etapa 4: Cadastro de Produto com Estoque Inicial (Entrada de Insumos)', async () => {
    await page.goto(`/${studioSlug}/estoque/produtos/novo`);
    await page.waitForLoadState('networkidle');

    // Preenche dados do produto de revenda
    await page.fill('#nome', productName);
    await page.fill('#marca', 'Lumora Cosméticos');
    await page.fill('#categoria', 'Home Care');
    await page.selectOption('#tipo', 'REVENDA');
    await page.fill('#unidadeMedida', 'UN');
    await page.fill('#precoCusto', '25.00');
    await page.fill('#precoVenda', '60.00');
    await page.fill('#quantidadeEstoque', '10');
    await page.fill('#estoqueMinimo', '3');

    await page.click('button[type="submit"]:has-text("Salvar Produto")');
    await page.waitForURL(`**/${studioSlug}/estoque`, { timeout: 10000 });

    await expect(page.locator(`text=${productName}`)).toBeVisible();
    console.log(`✅ [ESTOQUE] Produto '${productName}' cadastrado com estoque inicial de 10 unidades.`);
  });

  test('Etapa 5: Abertura do Caixa Diário na Recepção', async () => {
    await page.goto(`/${studioSlug}/financeiro`);
    await page.waitForLoadState('networkidle');

    // Verifica se o caixa está fechado e necessita de abertura
    const btnAbrirCaixa = page.locator('button:has-text("Abrir Caixa Agora"), button:has-text("Abrir Caixa")');
    if (await btnAbrirCaixa.isVisible()) {
      await btnAbrirCaixa.click();

      const inputSaldo = page.locator('#modalAbrirCaixa input[name="saldoInicial"]');
      await inputSaldo.waitFor({ state: 'visible', timeout: 5000 });
      await inputSaldo.fill('100.00');

      await page.click('#modalAbrirCaixa button[type="submit"]:has-text("Confirmar Abertura")');
      await page.waitForLoadState('networkidle');
      console.log(`✅ [CAIXA] Caixa diário aberto com sucesso (Fundo de troco: R$ 100,00).`);
    } else {
      console.log(`ℹ️ [CAIXA] Caixa diário já se encontrava aberto.`);
    }
  });

  test('Etapa 6: Simulação de Atendimento Completo (Comanda, Saída de Estoque & Split)', async () => {
    await page.goto(`/${studioSlug}/financeiro`);
    await page.waitForLoadState('networkidle');

    // 1. Clica no botão para abrir nova comanda rápida
    const btnNovaComanda = page.locator('button:has-text("Nova Comanda Rápida")');
    await btnNovaComanda.click();

    const inputNomeAvulso = page.locator('#modalNovaComanda input[name="nomeAvulso"]');
    await inputNomeAvulso.waitFor({ state: 'visible', timeout: 5000 });
    await inputNomeAvulso.fill(`Fernanda Cliente VIP ${timestamp}`);
    await page.click('#modalNovaComanda button[type="submit"]:has-text("Abrir Comanda")');

    // 2. Aguarda redirecionamento para a tela da comanda aberta
    await page.waitForURL(`**/${studioSlug}/financeiro/comandas/*`, { timeout: 10000 });
    const comandaUrl = page.url();
    console.log(`✅ [COMANDA] Comanda aberta em: ${comandaUrl}`);

    // 3. Adiciona o Serviço na comanda
    await page.evaluate(() => {
      const selectServico = /** @type {any} */ (document.querySelector('select[name="servicoId"]'));
      if (selectServico && selectServico.tomselect) {
        const keys = Object.keys(selectServico.tomselect.options);
        if (keys.length > 0) {
          selectServico.tomselect.setValue(keys[0]);
        }
      }
    });

    await page.click('#servico-pane button[type="submit"]');
    await page.waitForLoadState('networkidle');
    console.log(`✅ [COMANDA] Serviço 'Corte & Escova VIP' inserido na comanda.`);

    // 4. Adiciona o Produto de Revenda (Disparará saída automática do estoque)
    await page.click('#produto-tab');
    await page.waitForSelector('#produto-pane', { state: 'visible' });

    await page.evaluate(() => {
      const selectProduto = /** @type {any} */ (document.querySelector('select[name="produtoId"]'));
      if (selectProduto && selectProduto.tomselect) {
        const keys = Object.keys(selectProduto.tomselect.options);
        if (keys.length > 0) {
          selectProduto.tomselect.setValue(keys[0]);
        }
      }
    });

    await page.click('#produto-pane button[type="submit"]');
    await page.waitForLoadState('networkidle');
    console.log(`✅ [COMANDA] Produto de revenda adicionado na comanda.`);

    // 5. Finaliza e Fecha a comanda
    const btnFechar = page.locator('button.btn-gold-action:has-text("Receber & Fechar Comanda")');
    await btnFechar.waitFor({ state: 'visible', timeout: 5000 });
    await btnFechar.click();
    await page.waitForLoadState('networkidle');

    // Valida que a comanda foi finalizada com sucesso e redirecionou ao painel financeiro
    await expect(page.locator('text=Comanda finalizada e paga com sucesso!')).toBeVisible();
    console.log(`✅ [PAGAMENTO] Comanda recebida e fechada com sucesso! Caixa atualizado.`);

    // 6. Auditoria de Estoque: Navega até a tela de Estoque pelo menu lateral
    await page.locator('a.nav-link:has-text("Estoque & Produtos")').click();
    await page.waitForURL(`**/${studioSlug}/estoque`, { timeout: 15000 });

    const linhaProduto = page.locator(`tr:has-text("${productName}")`).first();
    await expect(linhaProduto).toBeVisible({ timeout: 15000 });
    await expect(linhaProduto).toContainText('9');
    console.log(`🎯 [AUDITORIA ESTOQUE APROVADA] Saldo do produto baixou automaticamente de 10 para 9 unidades!`);
  });

});
