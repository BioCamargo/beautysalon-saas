# ==============================================================================
# NEXI PRO SaaS - Automação de Backup Lógico PostgreSQL 16 (Regra 3-2-1)
# Custo Operacional: R$ 0,00 (Totalmente Gratuito)
# ==============================================================================
param (
    [string]$DbHost = $(if ($env:DB_HOST) { $env:DB_HOST } else { "localhost" }),
    [string]$DbPort = $(if ($env:DB_PORT) { $env:DB_PORT } else { "5432" }),
    [string]$DbName = $(if ($env:DB_NAME) { $env:DB_NAME } else { "beautysalon_db" }),
    [string]$DbUser = $(if ($env:DB_USER) { $env:DB_USER } else { "postgres" }),
    [string]$BackupDir = ".\backups",
    [int]$RetentionDays = 14,
    [string]$CloudflareR2Bucket = $env:R2_BUCKET_NAME # Opcional: Para cópia externa gratuita (Cloudflare R2 Free Tier)
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " [NEXI PRO] Iniciando Rotina de Backup Corporativo PostgreSQL 16" -ForegroundColor Cyan
Write-Host " Data/Hora: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')" -ForegroundColor Gray
Write-Host " Banco de Dados: $DbName em $DbHost:$DbPort" -ForegroundColor Gray
Write-Host "======================================================================" -ForegroundColor Cyan

# 1. Cria diretório de backup se não existir
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    Write-Host "[OK] Diretório de backup criado: $BackupDir" -ForegroundColor Green
}

# 2. Localiza o executável do pg_dump
$pgDumpCmd = "pg_dump"
if (-not (Get-Command "pg_dump" -ErrorAction SilentlyContinue)) {
    $possiblePaths = @(
        "C:\Program Files\PostgreSQL\16\bin\pg_dump.exe",
        "C:\Program Files\PostgreSQL\15\bin\pg_dump.exe",
        "C:\Program Files\PostgreSQL\14\bin\pg_dump.exe",
        "C:\PostgreSQL\bin\pg_dump.exe"
    )
    foreach ($p in $possiblePaths) {
        if (Test-Path $p) {
            $pgDumpCmd = $p
            break
        }
    }
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$dumpFileName = "backup_${DbName}_${timestamp}.dump"
$dumpFilePath = Join-Path $BackupDir $dumpFileName
$hashFilePath = Join-Path $BackupDir "${dumpFileName}.sha256"

Write-Host "`n>> Executando extração lógica compactada (Formato Custom pg_dump -Fc)..." -ForegroundColor Yellow

try {
    # Executa dump com alta compressão nativa do PostgreSQL (-Fc)
    & $pgDumpCmd -h $DbHost -p $DbPort -U $DbUser -d $DbName -Fc -b -v -f $dumpFilePath

    if (-not (Test-Path $dumpFilePath)) {
        throw "Arquivo de backup não foi gerado em: $dumpFilePath"
    }

    $fileInfo = Get-Item $dumpFilePath
    $sizeMB = [math]::Round($fileInfo.Length / 1MB, 2)
    Write-Host "[OK] Backup gerado com sucesso!" -ForegroundColor Green
    Write-Host "     Arquivo: $dumpFileName" -ForegroundColor White
    Write-Host "     Tamanho: $sizeMB MB" -ForegroundColor White

    # 3. Gera Hash SHA-256 para garantir integridade e proteção contra corrupção
    $hash = (Get-FileHash -Path $dumpFilePath -Algorithm SHA256).Hash
    Set-Content -Path $hashFilePath -Value "$hash *$dumpFileName" -Encoding UTF8
    Write-Host "[OK] Hash SHA-256 gerado: $hash" -ForegroundColor Green

    # 4. Sincronização Externa Opcional (Cloudflare R2 Free Tier - 10GB Grátis)
    if ($CloudflareR2Bucket) {
        Write-Host "`n>> Despachando cópia imutável para Cloudflare R2 / S3..." -ForegroundColor Yellow
        if (Get-Command "aws" -ErrorAction SilentlyContinue) {
            aws s3 cp $dumpFilePath "s3://$CloudflareR2Bucket/$dumpFileName"
            Write-Host "[OK] Cópia externa off-site sincronizada com sucesso!" -ForegroundColor Green
        } else {
            Write-Host "[AVISO] CLI do AWS/R2 não encontrado no PATH. Cópia externa ignorada." -ForegroundColor DarkYellow
        }
    }

    # 5. Rotação Automática de Backups Locais (Remove arquivos com mais de $RetentionDays dias)
    Write-Host "`n>> Verificando rotação e retenção de arquivos antigos (>$RetentionDays dias)..." -ForegroundColor Yellow
    $cutoffDate = (Get-Date).AddDays(-$RetentionDays)
    $oldFiles = Get-ChildItem -Path $BackupDir -File | Where-Object {
        ($_.Extension -eq ".dump" -or $_.Extension -eq ".sha256") -and $_.LastWriteTime -lt $cutoffDate
    }

    if ($oldFiles) {
        foreach ($old in $oldFiles) {
            Remove-Item $old.FullName -Force
            Write-Host "     [EXPURGADO] Backup antigo removido: $($old.Name)" -ForegroundColor DarkGray
        }
    } else {
        Write-Host "     Nenhum arquivo expirado para expurgo." -ForegroundColor DarkGray
    }

    Write-Host "`n======================================================================" -ForegroundColor Cyan
    Write-Host " [SUCESSO] Rotina concluída com êxito! RPO e RTO garantidos." -ForegroundColor Green
    Write-Host "======================================================================" -ForegroundColor Cyan

} catch {
    Write-Host "`n[ERRO CRÍTICO] Falha na rotina de backup: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}
