#!/usr/bin/env bash
# ==============================================================================
# scripts/backup-db.sh - roda NA VPS
# ==============================================================================
# pg_dump do container postgres para /srv/apps/ms-bluedot/backups
# Adaptado de bluedot-handbook/infra/vps/templates/backup-db.sh
# ==============================================================================
set -euo pipefail

APP_DIR="${APP_DIR:-/srv/apps/ms-bluedot}"
ENV_FILE="$APP_DIR/.env"
COMPOSE_FILE="$APP_DIR/docker-compose.yml"

fail() { echo "ERRO: $*" >&2; exit 1; }
[[ -f "$ENV_FILE" ]] || fail ".env não encontrado: $ENV_FILE"

# shellcheck disable=SC1090
set -a
source "$ENV_FILE"
set +a

: "${POSTGRES_DB:?POSTGRES_DB ausente}"
: "${POSTGRES_USER:?POSTGRES_USER ausente}"

mkdir -p "$APP_DIR/backups"
backup_file="$APP_DIR/backups/ms-bluedot-postgres-$(date +%Y%m%d-%H%M%S).sql.gz"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" \
  | gzip > "$backup_file"

chmod 600 "$backup_file"
echo "Backup gerado: $backup_file"
