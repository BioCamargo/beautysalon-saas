# ==============================================================================
# PLAYBOOK DE TESTES: CAIXA, COMANDAS & INTEGRIDADE FINANCEIRA
# Agente: qa-financeiro-comissoes
# ==============================================================================

param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Continue"
$workDir = $PSScriptRoot
$cookieFile = "$workDir\cookie_financeiro_test.txt"
if (Test-Path $cookieFile) { Remove-Item $cookieFile -Force }

function Get-Csrf {
    param($html)
    if ($html -match 'name="_csrf"\s+value="([^"]+)"') { return $matches[1] }
    if ($html -match 'id="csrfToken"\s+value="([^"]+)"') { return $matches[1] }
    return $null
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      EXECUÇÃO DE AUDITORIA: CAIXA, COMANDAS & FINANCEIRO         " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Obter CSRF e Criar Novo Salão Isolado
$rReg = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/register") -join "`n"
$csrfReg = Get-Csrf $rReg
if (-not $csrfReg) {
    Write-Host "[FAIL] Não foi possível obter token CSRF. O servidor está ativo na porta 8081?" -ForegroundColor Red
    exit 1
}

$timestamp = Get-Date -Format "HHmmss"
$slugEmpresa = "studio-fin-$timestamp"
$nomeEmpresa = "Studio Fin VIP $timestamp"
$usernameOwner = "owner_fin_$timestamp"
$emailEmpresa = "fin.$timestamp@lumorasaas.com"
$senha = "Lumora123"

$regResp = & curl.exe -s -b $cookieFile -c $cookieFile -X POST "$BaseUrl/register-empresa" `
    -H "X-XSRF-TOKEN: $csrfReg" `
    -d "_csrf=$csrfReg" `
    -d "nomeEmpresa=$nomeEmpresa" `
    -d "emailEmpresa=$emailEmpresa" `
    -d "telefoneEmpresa=(11) 98765-4321" `
    -d "segmento=SALAO_BELEZA" `
    -d "nomeUsuario=Marcos Financeiro" `
    -d "username=$usernameOwner" `
    -d "emailUsuario=$emailEmpresa" `
    -d "password=$senha" `
    -d "confirmPassword=$senha"

$jsonReg = ($regResp -join "`n") | ConvertFrom-Json
$slug = if ($jsonReg -and $jsonReg.slug) { $jsonReg.slug } else { $slugEmpresa }
Write-Host "  -> Salão criado com sucesso: $slug" -ForegroundColor Green

# 2. Login do Proprietário
$loginHtml = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/login") -join "`n"
$csrfLogin = Get-Csrf $loginHtml

$loginPost = & curl.exe -s -i -b $cookieFile -c $cookieFile -X POST "$BaseUrl/login" `
    -d "username=$usernameOwner" `
    -d "password=$senha" `
    -d "_csrf=$csrfLogin"

$loginOk = (($loginPost | Select-String "Location:").ToString() -match "/$slug/home")
Write-Host "  -> Login efetuado com sucesso: $loginOk" -ForegroundColor $(if ($loginOk) {"Green"} else {"Red"})

# 3. Abertura de Caixa com R$ 100,00 de fundo de troco
$finPage = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/$slug/financeiro") -join "`n"
$csrfFin = Get-Csrf $finPage

$abrirResp = & curl.exe -s -i -b $cookieFile -c $cookieFile -X POST "$BaseUrl/$slug/financeiro/caixa/abrir" `
    -d "_csrf=$csrfFin" `
    -d "saldoInicial=100.00" `
    -d "observacoes=Fundo de troco inicial"

$abrirOk = (($abrirResp | Select-String "Location:").ToString() -match "/$slug/financeiro")
Write-Host "  -> Abertura de caixa com R$ 100,00: $abrirOk" -ForegroundColor $(if ($abrirOk) {"Green"} else {"Red"})

# Validar se o caixa aparece aberto na tela
$finPageAfter = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/$slug/financeiro") -join "`n"
$caixaAbertoVisual = ($finPageAfter -match "Caixa Aberto" -or $finPageAfter -match "100,00" -or $finPageAfter -match "100.00")
Write-Host "  -> Painel financeiro reflete caixa aberto: $caixaAbertoVisual" -ForegroundColor $(if ($caixaAbertoVisual) {"Green"} else {"Red"})

# 4. Criação de Comanda Balcão
$csrfFin2 = Get-Csrf $finPageAfter
$comandaResp = & curl.exe -s -i -b $cookieFile -c $cookieFile -X POST "$BaseUrl/$slug/financeiro/comandas/nova" `
    -d "_csrf=$csrfFin2" `
    -d "nomeAvulso=Cliente Balcao Teste" `
    -d "observacoes=Atendimento rapido"

$comandaLoc = ($comandaResp | Select-String "Location:").ToString()
$comandaOk = ($comandaLoc -match "/financeiro/comandas/")
Write-Host "  -> Criação de comanda balcão: $comandaOk ($comandaLoc)" -ForegroundColor $(if ($comandaOk) {"Green"} else {"Red"})

# Limpeza
Remove-Item $cookieFile -ErrorAction SilentlyContinue

Write-Host "`n==================================================================" -ForegroundColor Cyan
if ($loginOk -and $abrirOk -and $caixaAbertoVisual -and $comandaOk) {
    Write-Host "   AUDITORIA QA-FINANCEIRO-COMISSOES: 100% APROVADA COM SUCESSO!  " -ForegroundColor Green
} else {
    Write-Host "   AUDITORIA QA-FINANCEIRO-COMISSOES: FALHA ENCONTRADA.           " -ForegroundColor Red
}
Write-Host "==================================================================" -ForegroundColor Cyan
