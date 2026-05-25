#!/usr/bin/env bash
# ==============================================================================
# scripts/deploy.sh - roda NA VPS, dentro de /srv/apps/ms-bluedot
# ==============================================================================
# Adaptado do template oficial: bluedot-handbook/infra/vps/templates/deploy.sh
# Valida ambiente, sobe containers via docker compose, aguarda healthchecks
# e valida o endpoint público HTTPS.
# ==============================================================================
set -euo pipefail

APP_DIR="${APP_DIR:-/srv/apps/ms-bluedot}"
COMPOSE_FILE="$APP_DIR/docker-compose.yml"
ENV_FILE="$APP_DIR/.env"

required_vars=(
  APP_DOMAIN
  APP_PORT
  APP_HEALTH_PATH
  PUBLIC_HEALTH_URL
  POSTGRES_DB
  POSTGRES_USER
  POSTGRES_PASSWORD
  POSTGRES_HOST_PORT
  JWT_SECRET
  CLOUDFLARE_ACCOUNT_ID
  CLOUDFLARE_GET_TOKEN
  CLOUDFLARE_POST_TOKEN
)

fail() { echo "ERRO: $*" >&2; exit 1; }

env_value() {
  local key="$1" value
  value="$(awk -F= -v key="$key" '$1 == key { sub(/^[^=]*=/, ""); print; exit }' "$ENV_FILE")"
  value="${value%$'\r'}"
  if [[ "$value" == \"*\" && "$value" == *\" ]]; then value="${value#\"}"; value="${value%\"}"; fi
  if [[ "$value" == \'*\' && "$value" == *\' ]]; then value="${value#\'}"; value="${value%\'}"; fi
  printf '%s' "$value"
}

validate_env() {
  [[ -f "$ENV_FILE" ]] || fail ".env não encontrado: $ENV_FILE"
  local missing=()
  for var in "${required_vars[@]}"; do
    [[ -n "$(env_value "$var")" ]] || missing+=("$var")
  done
  if (( ${#missing[@]} > 0 )); then
    echo "Variáveis obrigatórias ausentes em $ENV_FILE:" >&2
    printf ' - %s\n' "${missing[@]}" >&2
    exit 1
  fi
  if grep -Eq '=(change-this|__|<)' "$ENV_FILE"; then
    fail "$ENV_FILE contém placeholder. Preencha valores reais antes do deploy."
  fi
}

validate_shared_proxy() {
  docker network inspect proxy >/dev/null 2>&1 || fail "Rede Docker externa 'proxy' não encontrada."
  local traefik_status
  traefik_status="$(docker inspect --format '{{ .State.Status }}' traefik 2>/dev/null || echo missing)"
  [[ "$traefik_status" == "running" ]] || fail "Traefik não está rodando (status: $traefik_status)."
}

compose() { docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" "$@"; }

wait_app_healthy() {
  local timeout="${APP_HEALTH_TIMEOUT:-240}" elapsed=0 status="missing" container_id=""
  echo "Aguardando app ficar healthy (timeout ${timeout}s)..."
  while (( elapsed < timeout )); do
    container_id="$(compose ps -q app 2>/dev/null || true)"
    if [[ -n "$container_id" ]]; then
      status="$(docker inspect --format '{{ if .State.Health }}{{ .State.Health.Status }}{{ else }}none{{ end }}' "$container_id" 2>/dev/null || echo missing)"
      case "$status" in
        healthy) echo "App healthy em ${elapsed}s."; return 0 ;;
        unhealthy) compose logs --tail 120 app >&2 || true; fail "Container app unhealthy." ;;
      esac
    fi
    sleep 5; elapsed=$((elapsed + 5))
  done
  compose logs --tail 120 app >&2 || true
  fail "Timeout aguardando healthcheck do app. Status atual: $status."
}

verify_public_endpoint() {
  local url
  url="$(env_value PUBLIC_HEALTH_URL)"
  echo "Verificando endpoint público: $url"
  curl -fsS --max-time 15 "$url" >/dev/null || fail "Endpoint público não respondeu com sucesso."
  echo "Endpoint público respondendo."
}

command -v docker >/dev/null 2>&1 || fail "Docker não encontrado."
command -v curl   >/dev/null 2>&1 || fail "curl não encontrado."
[[ -f "$COMPOSE_FILE" ]] || fail "Compose não encontrado: $COMPOSE_FILE"

echo "==> Deploy ms-bluedot em $APP_DIR"
cd "$APP_DIR"
validate_env
validate_shared_proxy

mkdir -p "$APP_DIR/backups" "$APP_DIR/logs"
compose config >/dev/null
compose pull postgres
compose up -d --build --remove-orphans app postgres
compose ps
wait_app_healthy
verify_public_endpoint
echo "==> Deploy concluído com sucesso."
