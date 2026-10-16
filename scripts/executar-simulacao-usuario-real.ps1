param(
    [switch]$Headed,
    [string]$BaseUrl = "http://localhost:8081"
)

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " 🤖 BEAUTY SALON SAAS - SIMULAÇÃO DE USUÁRIO REAL (E2E)  " -ForegroundColor Yellow
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "Target URL: $BaseUrl" -ForegroundColor Gray

# Verifica se a aplicação Spring Boot está no ar
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/login" -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
    Write-Host "✅ Aplicação Spring Boot detectada e ativa em $BaseUrl!" -ForegroundColor Green
} catch {
    Write-Host "⚠️ ATENÇÃO: A aplicação Spring Boot não parece estar ativa em $BaseUrl." -ForegroundColor Red
    Write-Host "Por favor, inicie a aplicação antes de executar a simulação:" -ForegroundColor Yellow
    Write-Host "  `$env:JAVA_HOME = 'C:\Dev\Projetos\Proj_Studio\.jdk25\jdk-25.0.2'" -ForegroundColor White
    Write-Host "  .\mvnw.cmd spring-boot:run" -ForegroundColor White
    exit 1
}

# Navega para a pasta e2e
Push-Location "$PSScriptRoot\..\e2e"

try {
    if ($Headed) {
        Write-Host "🚀 Iniciando simulação visual no Google Chrome (Headed Mode)..." -ForegroundColor Cyan
        npx.cmd playwright test --headed
    } else {
        Write-Host "🚀 Executando simulação de usuário real (Modo Automatizado)..." -ForegroundColor Cyan
        npx.cmd playwright test
    }
} finally {
    Pop-Location
}
