#!/usr/bin/env bash
# ==============================================================================
# NEXI PRO SaaS - Procedimento de Restauração de Desastres (Linux DR)
# ==============================================================================
set -e

if [ -z "$1" ]; then
    echo "Uso: $0 <caminho_do_arquivo.dump>"
    exit 1
fi

DUMP_FILE="$1"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-beautysalon_db}"
DB_USER="${DB_USER:-postgres}"

echo "======================================================================"
echo " [NEXI PRO] PROCEDIMENTO DE RESTAURAÇÃO DE DESASTRE (DR)"
echo " Arquivo: ${DUMP_FILE}"
echo " Banco Destino: ${DB_NAME} em ${DB_HOST}:${DB_PORT}"
echo "======================================================================"

if [ ! -f "${DUMP_FILE}" ]; then
    echo "[ERRO] Arquivo de dump não encontrado: ${DUMP_FILE}"
    exit 1
fi

# Validação SHA-256
if [ -f "${DUMP_FILE}.sha256" ]; then
    echo ">> Validando checksum SHA-256..."
    sha256sum -c "${DUMP_FILE}.sha256"
    echo "[OK] Checksum válido!"
fi

echo ">> Executando pg_restore..."
pg_restore -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists -v "${DUMP_FILE}"

echo "======================================================================"
echo " [SUCESSO] Restauração concluída com êxito!"
echo "======================================================================"
