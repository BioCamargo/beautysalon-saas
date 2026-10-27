#!/usr/bin/env bash
# ==============================================================================
# NEXI PRO SaaS - Automação de Backup Lógico PostgreSQL 16 (Linux / Cron / Cloud)
# Custo Operacional: R$ 0,00 (Totalmente Gratuito)
# ==============================================================================
set -e

DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-beautysalon_db}"
DB_USER="${DB_USER:-postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
R2_BUCKET="${R2_BUCKET_NAME:-}"

echo "======================================================================"
echo " [NEXI PRO] Iniciando Rotina de Backup PostgreSQL 16 (Linux)"
echo " Data/Hora: $(date '+%Y-%m-%d %H:%M:%S')"
echo " Banco: ${DB_NAME} em ${DB_HOST}:${DB_PORT}"
echo "======================================================================"

mkdir -p "${BACKUP_DIR}"

TIMESTAMP=$(date '+%Y%m%d_%H%M%S')
DUMP_FILE="${BACKUP_DIR}/backup_${DB_NAME}_${TIMESTAMP}.dump"
HASH_FILE="${DUMP_FILE}.sha256"

echo ">> Executando extração lógica compactada (pg_dump -Fc)..."
pg_dump -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -Fc -b -v -f "${DUMP_FILE}"

if [ ! -f "${DUMP_FILE}" ]; then
    echo "[ERRO] Arquivo de backup não gerado!"
    exit 1
fi

FILE_SIZE=$(du -h "${DUMP_FILE}" | cut -f1)
echo "[OK] Backup gerado com sucesso: ${DUMP_FILE} (${FILE_SIZE})"

# Gera Hash SHA-256
sha256sum "${DUMP_FILE}" > "${HASH_FILE}"
echo "[OK] Checksum SHA-256 gerado: $(cat ${HASH_FILE})"

# Sincronização externa opcional (Cloudflare R2 Free Tier)
if [ -n "${R2_BUCKET}" ]; then
    echo ">> Enviando cópia externa para Cloudflare R2 / S3..."
    if command -v aws &> /dev/null; then
        aws s3 cp "${DUMP_FILE}" "s3://${R2_BUCKET}/"
        echo "[OK] Cópia off-site enviada com sucesso!"
    fi
fi

# Rotação de arquivos com mais de X dias
echo ">> Limpando backups locais com mais de ${RETENTION_DAYS} dias..."
find "${BACKUP_DIR}" -type f \( -name "*.dump" -o -name "*.sha256" \) -mtime +${RETENTION_DAYS} -exec rm -f {} \;
echo "[OK] Rotação concluída. Espaço em disco protegido."

echo "======================================================================"
echo " [SUCESSO] Backup concluído com êxito!"
echo "======================================================================"
