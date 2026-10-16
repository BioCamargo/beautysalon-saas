// @ts-check
const { test, expect } = require('@playwright/test');

/**
 * 💰 AUDITORIA FINANCEIRA & FLUXO DE CAIXA (E2E)
 * 
 * Simula a rotina financeira diária da recepção:
 * 1. Abertura de Caixa Diário com R$ 100,00 de fundo de troco
 * 2. Registro de Reforço de Caixa (+ R$ 50,00 de moedas/troco)
 * 3. Registro de Sangria de Caixa (- R$ 35,00 despesa operacional)
 * 4. Auditoria Matemática dos Indicadores:
 *    - Saldo Esperado = 100 + 50 - 35 = R$ 115,00
 * 5. Fechamento de Caixa com conferência cega e registro no histórico
 */

test.describe.configure({ mode: 'serial' });

test.describe('💰 Auditoria Financeira — Caixa, Sangria & Reforço', () => {

  const timestamp = Date.now().toString().slice(-6);
  const studioName = `Studio Finanças VIP ${timestamp}`;
  const ownerName = `Carla Tesoureira ${timestamp}`;
  const ownerUsername = `carla_fin_${timestamp}`;
  const ownerEmail = `carla.${timestamp}@financas.com`;
  const defaultPassword = 'senhaSegura123';

  /** @type {import('@playwright/test').Page} */
  let page;
  let studioSlug = '';

  test.beforeAll(async ({ browser }) => {
    page = await browser.newPage();
  });

  test.afterAll(async () => {
    if (page) {
      await page.close();
    }
  });

  test('Passo 1: Onboarding Rápido & Abertura de Caixa', async () => {
    // 1. Cadastra estúdio
    await page.goto('/register');
    await page.fill('#nomeEmpresa', studioName);
    await page.fill('#emailEmpresa', ownerEmail);
    await page.selectOption('#segmento', 'SALAO_BELEZA');
    await page.fill('#telefoneEmpresa', '(11) 98765-4321');
    await page.fill('#nomeUsuario', ownerName);
    await page.fill('#username', ownerUsername);
    await page.fill('#emailUsuario', `carla.${timestamp}@gmail.com`);
    await page.fill('#password', defaultPassword);
    await page.fill('#confirmPassword', defaultPassword);
    await page.click('#btnSubmit');

    // 2. Obtém slug e faz login
    await page.waitForSelector('#success-onboarding-container', { state: 'visible', timeout: 15000 });
    const slugValue = await page.locator('#exclusiveUrlText').inputValue();
    const match = slugValue.match(/\/([^\/]+)\/login/);
    expect(match).not.toBeNull();
    studioSlug = match ? match[1] : '';

    await page.click('#btnGoToStudio');
    await page.waitForURL(`**/${studioSlug}/login**`, { timeout: 15000 });
    await page.fill('#username', ownerUsername);
    await page.fill('#password', defaultPassword);
    await page.click('button[type="submit"]');
    await page.waitForURL(`**/${studioSlug}/home**`, { timeout: 15000 });

    // 3. Acessa Financeiro & Caixa
    await page.goto(`/${studioSlug}/financeiro`);
    await page.waitForLoadState('networkidle');

    // Abre o caixa com R$ 100,00 se estiver fechado
    const btnAbrir = page.locator('button:has-text("Abrir Caixa Agora"), button:has-text("Abrir Caixa")');
    if (await btnAbrir.isVisible()) {
      await btnAbrir.click();
      const inputSaldo = page.locator('#modalAbrirCaixa input[name="saldoInicial"]');
      await inputSaldo.waitFor({ state: 'visible', timeout: 5000 });
      await inputSaldo.fill('100.00');
      await page.click('#modalAbrirCaixa button[type="submit"]:has-text("Confirmar Abertura")');
      await page.waitForLoadState('networkidle');
      console.log(`✅ [CAIXA INICIAL] Caixa aberto com R$ 100,00.`);
    }
  });

  test('Passo 2: Registro de Reforço de Caixa (+ R$ 50,00)', async () => {
    // 1. Clica no botão para abrir modal de movimentação
    const btnMov = page.locator('button[data-bs-target="#modalMovCaixa"]');
    await expect(btnMov).toBeVisible({ timeout: 10000 });
    await btnMov.click();

    const modalMov = page.locator('#modalMovCaixa');
    try {
      await modalMov.waitFor({ state: 'visible', timeout: 3000 });
    } catch {
      await page.evaluate(() => {
        const el = document.getElementById('modalMovCaixa');
        const bs = /** @type {any} */ (window)['bootstrap'];
        if (el && bs) bs.Modal.getOrCreateInstance(el).show();
      });
      await modalMov.waitFor({ state: 'visible', timeout: 5000 });
    }

    // 2. Seleciona REFORCO
    await page.selectOption('#modalMovCaixa select[name="tipo"]', 'REFORCO');
    await page.fill('#modalMovCaixa input[name="valor"]', '50.00');
    await page.fill('#modalMovCaixa input[name="descricao"]', 'Aporte de moedas para troco');
    await page.click('#modalMovCaixa button[type="submit"]:has-text("Confirmar")');
    
    // Aguarda a linha de REFORÇO aparecer na tabela de movimentações (garantia de reload concluído)
    await expect(page.locator('table tr:has-text("REFORCO")')).toBeVisible({ timeout: 15000 });
    console.log(`✅ [REFORÇO] Reforço de R$ 50,00 lançado com sucesso.`);
  });

  test('Passo 3: Registro de Sangria de Caixa (- R$ 35,00)', async () => {
    // Garante tela atualizada e sem resquícios de modais anteriores
    await page.goto(`/${studioSlug}/financeiro`);
    await page.waitForLoadState('networkidle');

    // 1. Clica no botão para abrir modal de movimentação
    const btnMov = page.locator('button[data-bs-target="#modalMovCaixa"]');
    await expect(btnMov).toBeVisible({ timeout: 10000 });
    await btnMov.click();

    const modalMov = page.locator('#modalMovCaixa');
    try {
      await modalMov.waitFor({ state: 'visible', timeout: 3000 });
    } catch {
      await page.evaluate(() => {
        const el = document.getElementById('modalMovCaixa');
        const bs = /** @type {any} */ (window)['bootstrap'];
        if (el && bs) bs.Modal.getOrCreateInstance(el).show();
      });
      await modalMov.waitFor({ state: 'visible', timeout: 5000 });
    }

    // 2. Seleciona SANGRIA
    await page.selectOption('#modalMovCaixa select[name="tipo"]', 'SANGRIA');
    await page.fill('#modalMovCaixa input[name="valor"]', '35.00');
    await page.fill('#modalMovCaixa input[name="descricao"]', 'Pagamento entregador insumos');
    await page.click('#modalMovCaixa button[type="submit"]:has-text("Confirmar")');

    // Aguarda a linha de SANGRIA aparecer na tabela de movimentações (garantia de reload concluído)
    await expect(page.locator('table tr:has-text("SANGRIA")')).toBeVisible({ timeout: 15000 });
    console.log(`✅ [SANGRIA] Sangria de R$ 35,00 retirada com sucesso.`);
  });

  test('Passo 4: Auditoria Matemática dos Saldos & Fechamento de Caixa', async () => {
    // Garante tela atualizada
    await page.goto(`/${studioSlug}/financeiro`);
    await page.waitForLoadState('networkidle');

    // Valida que o saldo esperado no painel reflete o cálculo: 100 + 50 - 35 = R$ 115,00
    const painelSaldoEsperado = page.locator('h4.text-primary:has-text("R$ 115,00")');
    await expect(painelSaldoEsperado).toBeVisible({ timeout: 10000 });
    console.log(`🎯 [AUDITORIA MATEMÁTICA APROVADA] Saldo esperado bateu exatamente em R$ 115,00!`);

    // Realiza o Fechamento do Caixa
    const btnFechar = page.locator('button[data-bs-target="#modalFecharCaixa"]');
    await expect(btnFechar).toBeVisible({ timeout: 10000 });
    await btnFechar.click();

    const modalFechar = page.locator('#modalFecharCaixa');
    try {
      await modalFechar.waitFor({ state: 'visible', timeout: 3000 });
    } catch {
      await page.evaluate(() => {
        const el = document.getElementById('modalFecharCaixa');
        const bs = /** @type {any} */ (window)['bootstrap'];
        if (el && bs) bs.Modal.getOrCreateInstance(el).show();
      });
      await modalFechar.waitFor({ state: 'visible', timeout: 5000 });
    }

    // Informa o saldo contado de R$ 115,00
    await page.fill('#modalFecharCaixa input[name="saldoFinalContado"]', '115.00');
    await page.fill('#modalFecharCaixa input[name="observacoes"]', 'Fechamento perfeito sem divergência de centavos.');
    await page.click('#modalFecharCaixa button[type="submit"]:has-text("Fechar Caixa")');

    // Valida que o caixa agora consta como fechado
    await expect(page.locator('text=O Caixa do dia está fechado')).toBeVisible({ timeout: 10000 });
    console.log(`✅ [FECHAMENTO CONCLUÍDO] Caixa diário conferido e encerrado com zero divergência.`);
  });

});
