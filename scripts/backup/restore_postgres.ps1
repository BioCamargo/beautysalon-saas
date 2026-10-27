# ==============================================================================
# NEXI PRO SaaS - Procedimento de Restauração de Desastres (Disaster Recovery)
# Restaura um arquivo de dump compactado (.dump) no PostgreSQL
# ==============================================================================
param (
    [Parameter(Mandatory=$true)]
    [string]$DumpFile,
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }),
    [string]$DbPort = $(if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { "beautysalon_db" }),
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { "postgres" })
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Red
Write-Host " [NEXI PRO] PROCEDIMENTO DE RESTAURAÇÃO DE DESASTRE (DISASTER RECOVERY)" -ForegroundColor Red
Write-Host " Arquivo Alvo: $DumpFile" -ForegroundColor Yellow
Write-Host " Destino: $DbName em $DbHost:$DbPort" -ForegroundColor Yellow
Write-Host "======================================================================" -ForegroundColor Red

if (-not (Test-Path $DumpFile)) {
    Write-Host "[ERRO] Arquivo de dump não encontrado em: $DumpFile" -ForegroundColor Red
    exit 1
}

# 1. Verifica integridade com SHA-256 se existir arquivo .sha256
$hashFile = "${DumpFile}.sha256"
if (Test-Path $hashFile) {
    Write-Host ">> Verificando integridade SHA-256..." -ForegroundColor Yellow
    $expectedHash = (Get-Content $hashFile).Split(" ")[0].Trim()
    $actualHash = (Get-FileHash -Path $DumpFile -Algorithm SHA256).Hash
    if ($expectedHash -ne $actualHash) {
        Write-Host "[ERRO CRÍTICO] Checksum SHA-256 divergente! O arquivo pode estar corrompido." -ForegroundColor Red
        Write-Host "Esperado: $expectedHash" -ForegroundColor Red
        Write-Host "Atual:    $actualHash" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Integridade criptográfica verificada com sucesso!" -ForegroundColor Green
}

# 2. Localiza pg_restore
$pgRestoreCmd = "pg_restore"
if (-not (Get-Command "pg_restore" -ErrorAction SilentlyContinue)) {
    $possiblePaths = @(
        "C:\Program Files\PostgreSQL\16\bin\pg_restore.exe",
        "C:\Program Files\PostgreSQL\15\bin\pg_restore.exe",
        "C:\Program Files\PostgreSQL\14\bin\pg_restore.exe",
        "C:\PostgreSQL\bin\pg_restore.exe"
    )
    foreach ($p in $possiblePaths) {
        if (Test-Path $p) {
            $pgRestoreCmd = $p
            break
        }
    }
}

Write-Host "`n>> Executando restauração com pg_restore..." -ForegroundColor Yellow
try {
    # --clean limpa tabelas existentes antes de recriar; --if-exists evita erros se não existirem
    & $pgRestoreCmd -h $DbHost -p $DbPort -U $DbUser -d $DbName --clean --if-exists -v $DumpFile

    Write-Host "`n[OK] Banco de dados restaurado com sucesso!" -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Green
    Write-Host " [SUCESSO] Operação de Disaster Recovery concluída com êxito!" -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Green
} catch {
    Write-Host "`n[ERRO CRÍTICO] Falha durante o pg_restore: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
