// @ts-check
const { test, expect } = require('@playwright/test');

/**
 * 📱 SIMULAÇÃO DE AGENDAMENTO ONLINE PELO CLIENTE (PORTAL PÚBLICO)
 * 
 * Simula a experiência do cliente final agendando pelo celular/computador:
 * 1. Acessa o link público do salão (/{slug}/agendar)
 * 2. Escolhe o procedimento no catálogo
 * 3. Seleciona o profissional parceiro
 * 4. Escolhe a data e horário disponível
 * 5. Preenche nome e WhatsApp
 * 6. Confirma o agendamento e recebe o Voucher VIP
 * 7. Valida que o agendamento aparece na Agenda interna do Salão
 */

test.describe.configure({ mode: 'serial' });

test.describe('📱 Portal Público — Agendamento Online do Cliente Final', () => {

  const timestamp = Date.now().toString().slice(-6);
  const studioName = `Salão Beleza Pura ${timestamp}`;
  const ownerName = `Patrícia Dono ${timestamp}`;
  const ownerUsername = `patricia_${timestamp}`;
  const ownerEmail = `patricia.${timestamp}@belezapura.com`;
  const defaultPassword = 'senhaSegura123';
  const clienteNome = `Beatriz Cliente VIP ${timestamp}`;
  const clienteTelefone = '(11) 98765-1122';

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

  test('Passo 1: Onboarding do Salão & Cadastro do Serviço no Catálogo', async () => {
    // 1. Cadastra o estúdio
    await page.goto('/register');
    await expect(page.locator('#nomeEmpresa')).toBeVisible();

    await page.fill('#nomeEmpresa', studioName);
    await page.fill('#emailEmpresa', ownerEmail);
    await page.selectOption('#segmento', 'SALAO_BELEZA');
    await page.fill('#telefoneEmpresa', '(11) 98888-7777');

    await page.fill('#nomeUsuario', ownerName);
    await page.fill('#username', ownerUsername);
    await page.fill('#emailUsuario', `dono.${timestamp}@gmail.com`);
    await page.fill('#password', defaultPassword);
    await page.fill('#confirmPassword', defaultPassword);

    await page.click('#btnSubmit');

    // 2. Extrai o slug do salão
    await page.waitForSelector('#success-onboarding-container', { state: 'visible', timeout: 15000 });
    const slugValue = await page.locator('#exclusiveUrlText').inputValue();
    const match = slugValue.match(/\/([^\/]+)\/login/);
    expect(match).not.toBeNull();
    studioSlug = match ? match[1] : '';
    console.log(`✅ [SETUP] Estúdio criado com slug: /${studioSlug}`);

    // 3. Faz o login inicial
    await page.click('#btnGoToStudio');
    await page.waitForURL(`**/${studioSlug}/login**`, { timeout: 15000 });
    await page.fill('#username', ownerUsername);
    await page.fill('#password', defaultPassword);
    await page.click('button[type="submit"]');
    await page.waitForURL(`**/${studioSlug}/home**`, { timeout: 15000 });

    // 4. Cadastra um serviço no catálogo
    await page.goto(`/${studioSlug}/servicos/novo`);
    await page.waitForLoadState('networkidle');
    await page.fill('#nome', 'Corte Bordado & Hidratação');
    await page.fill('#preco', '150.00');
    await page.fill('#duracaoMinutos', '60');
    await page.click('button[type="submit"]:has-text("Salvar"), button[type="submit"]:has-text("Salvar no Catálogo")');
    await page.waitForURL(`**/${studioSlug}/servicos`, { timeout: 10000 });
    console.log(`✅ [SERVIÇO] Serviço cadastrado no catálogo para o agendamento público.`);
  });

  test('Passo 2: Cliente acessa o Portal Público e realiza o agendamento', async () => {
    // 1. Acessa o link público do salão
    await page.goto(`/${studioSlug}/agendar`);
    await page.waitForLoadState('domcontentloaded');

    // Valida que o nome do salão está no topo do portal
    await expect(page.locator('.salon-title')).toBeVisible();
    console.log(`✅ [PORTAL PÚBLICO] Portal de agendamento online carregado.`);

    // 2. Seleciona o primeiro serviço disponível
    const servicoCard = page.locator('.service-item-card').first();
    await servicoCard.waitFor({ state: 'visible', timeout: 5000 });
    await servicoCard.click();

    // 3. Seleciona a data (Amanhã)
    const dataInput = page.locator('#dataAgendamento');
    const amanha = new Date();
    amanha.setDate(amanha.getDate() + 1);
    const dataFormatada = amanha.toISOString().split('T')[0];
    await dataInput.fill(dataFormatada);

    // 4. Seleciona o horário das 14:00
    const slotHorario = page.locator('button.time-slot-chip[data-time="14:00"]');
    if (await slotHorario.isVisible()) {
      await slotHorario.click();
    } else {
      // Se 14:00 não estiver visível, clica no primeiro disponível
      await page.locator('button.time-slot-chip').first().click();
    }

    // 5. Preenche os dados de contato da cliente
    await page.fill('input[name="clienteNome"]', clienteNome);
    await page.fill('#inputTelefone', clienteTelefone);
    await page.fill('input[name="clienteEmail"]', `cliente.${timestamp}@gmail.com`);

    // 6. Confirma o agendamento
    await page.click('#btnConfirmarAgendamento');

    // 7. Valida a tela de sucesso do Voucher VIP
    await page.waitForURL(`**/${studioSlug}/agendar/sucesso**`, { timeout: 15000 });
    await expect(page.locator('.vip-voucher-card, h2:has-text("Confirmado")')).toBeVisible();
    console.log(`🎯 [VOUCHER VIP GERADO] Agendamento confirmado com sucesso para ${clienteNome}!`);
  });

  test('Passo 3: Recepção confere o agendamento recebido na Agenda interna', async () => {
    // 1. Entra na tela de agendamentos interna do salão
    await page.goto(`/${studioSlug}/agendamentos`);
    await page.waitForLoadState('networkidle');

    // 2. Valida que o nome da cliente Beatriz aparece na grade ou na listagem de agendamentos
    await expect(page.locator(`text=${clienteNome}`)).toBeVisible({ timeout: 15000 });
    console.log(`✅ [AGENDA RECONHECIDA] Agendamento público sincronizado instantaneamente na grade da recepção!`);
  });

});
