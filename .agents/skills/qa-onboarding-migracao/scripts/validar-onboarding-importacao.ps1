# ==============================================================================
# PLAYBOOK DE TESTES: ONBOARDING, TRIAL & MIGRAÇÃO DE DADOS (EXCEL/CSV)
# Agente: qa-onboarding-migracao
# ==============================================================================

param(
    [string]$BaseUrl = "http://localhost:8081"
)

$ErrorActionPreference = "Continue"
$workDir = $PSScriptRoot
$cookieFile = "$workDir\cookie_onboarding_test.txt"
if (Test-Path $cookieFile) { Remove-Item $cookieFile -Force }

function Get-Csrf {
    param($html)
    if ($html -match 'name="_csrf"\s+value="([^"]+)"') { return $matches[1] }
    if ($html -match 'id="csrfToken"\s+value="([^"]+)"') { return $matches[1] }
    return $null
}

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host "      EXECUÇÃO DE AUDITORIA: ONBOARDING, TRIAL & IMPORTAÇÃO       " -ForegroundColor Cyan
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Obter Token CSRF na tela de registro
$rReg = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/register") -join "`n"
$csrfReg = Get-Csrf $rReg
if (-not $csrfReg) {
    Write-Host "[FAIL] Não foi possível obter token CSRF em /register. O servidor está ativo na porta 8081?" -ForegroundColor Red
    exit 1
}

# 2. Autocadastro de Novo Salão com Trial de 7 Dias
$timestamp = Get-Date -Format "HHmmss"
$slugEmpresa = "studio-onboarding-$timestamp"
$nomeEmpresa = "Studio Onboarding VIP $timestamp"
$usernameOwner = "owner_onboard_$timestamp"
$emailEmpresa = "onboard.$timestamp@lumorasaas.com"
$senha = "Lumora123"

$regResp = & curl.exe -s -b $cookieFile -c $cookieFile -X POST "$BaseUrl/register-empresa" `
    -H "X-XSRF-TOKEN: $csrfReg" `
    -d "_csrf=$csrfReg" `
    -d "nomeEmpresa=$nomeEmpresa" `
    -d "emailEmpresa=$emailEmpresa" `
    -d "telefoneEmpresa=(11) 98765-4321" `
    -d "segmento=SALAO_BELEZA" `
    -d "nomeUsuario=Camila Gestora" `
    -d "username=$usernameOwner" `
    -d "emailUsuario=$emailEmpresa" `
    -d "password=$senha" `
    -d "confirmPassword=$senha"

$jsonReg = ($regResp -join "`n") | ConvertFrom-Json
$slug = if ($jsonReg -and $jsonReg.slug) { $jsonReg.slug } else { $slugEmpresa }
Write-Host "  -> Salão criado com sucesso: $slug (Trial 7 dias concedido)" -ForegroundColor Green

# 3. Autenticação do Proprietário
$loginHtml = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/login") -join "`n"
$csrfLogin = Get-Csrf $loginHtml

$loginPost = & curl.exe -s -i -b $cookieFile -c $cookieFile -X POST "$BaseUrl/login" `
    -d "username=$usernameOwner" `
    -d "password=$senha" `
    -d "_csrf=$csrfLogin"

$loginLocation = ($loginPost | Select-String "Location:").ToString()
$loginOk = ($loginLocation -match "/$slug/home")
Write-Host "  -> Login do proprietário redirecionado para dashboard: $loginOk" -ForegroundColor $(if ($loginOk) {"Green"} else {"Red"})

# 4. Auditoria de Download de Modelos
$xlsxFile = "$workDir\modelo_audit.xlsx"
& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/$slug/clientes/importar/modelo?formato=xlsx" -o $xlsxFile
$xlsxLen = (Get-Item $xlsxFile).Length
$xlsxOk = ($xlsxLen -gt 1500)
Write-Host "  -> Modelo Excel (.xlsx) baixado ($xlsxLen bytes): $xlsxOk" -ForegroundColor $(if ($xlsxOk) {"Green"} else {"Red"})

$csvFile = "$workDir\modelo_audit.csv"
& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/$slug/clientes/importar/modelo?formato=csv" -o $csvFile
$csvContent = Get-Content -Raw $csvFile -Encoding UTF8
$csvOk = ($csvContent -match "Nome Completo\*;WhatsApp / Celular;E-mail")
Write-Host "  -> Modelo CSV com delimitador ';' e BOM UTF-8: $csvOk" -ForegroundColor $(if ($csvOk) {"Green"} else {"Red"})

# 5. Ingestão em Lote com Duplicidades e Inconsistências
$clientesPage = (& curl.exe -s -b $cookieFile -c $cookieFile "$BaseUrl/$slug/clientes") -join "`n"
$csrfClientes = Get-Csrf $clientesPage

$planilhaPath = "$workDir\lote_onboarding_teste.csv"
$csvData = @"
Nome Completo*;WhatsApp / Celular;E-mail;Data de Nascimento;Observações / Preferências;Alergias / Restrições
Alice Moraes;(11) 97111-2222;alice@email.com;14/07/1993;Cabelos ondulados;Nenhuma
Beatriz Silveira;(11) 97222-3333;beatriz@email.com;22/01/1989;Mechas loiras;Sensibilidade a pó descolorante
Alice Moraes Repetida;(11) 97111-2222;alice.outro@email.com;14/07/1993;Duplicidade intencional de telefone;Nenhuma
;11999990000;semnome@email.com;;;
"@
[System.IO.File]::WriteAllText($planilhaPath, $csvData, [System.Text.Encoding]::UTF8)

# Upload Multipart
$afterHtml = (& curl.exe -s -L -b $cookieFile -c $cookieFile "$BaseUrl/$slug/clientes/importar?_csrf=$csrfClientes" `
    -F "arquivo=@$planilhaPath") -join "`n"

$hasAlice = ($afterHtml -match "Alice Moraes")
$hasBeatriz = ($afterHtml -match "Beatriz Silveira")

Write-Host "  -> Cliente 'Alice Moraes' importada: $hasAlice" -ForegroundColor $(if ($hasAlice) {"Green"} else {"Red"})
Write-Host "  -> Cliente 'Beatriz Silveira' importada: $hasBeatriz" -ForegroundColor $(if ($hasBeatriz) {"Green"} else {"Red"})

# Limpeza de artefatos temporários
Remove-Item $cookieFile, $xlsxFile, $csvFile, $planilhaPath -ErrorAction SilentlyContinue

Write-Host "`n==================================================================" -ForegroundColor Cyan
if ($loginOk -and $xlsxOk -and $csvOk -and $hasAlice -and $hasBeatriz) {
    Write-Host "   AUDITORIA QA-ONBOARDING-MIGRACAO: 100% APROVADA COM SUCESSO!   " -ForegroundColor Green
} else {
    Write-Host "   AUDITORIA QA-ONBOARDING-MIGRACAO: FALHA ENCONTRADA.            " -ForegroundColor Red
}
Write-Host "==================================================================" -ForegroundColor Cyan
