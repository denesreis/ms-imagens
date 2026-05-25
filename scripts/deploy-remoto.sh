#!/usr/bin/env bash
# ==============================================================================
# scripts/deploy-remoto.sh - roda no computador LOCAL
# ==============================================================================
# Acessa a VPS via `ssh -A vps-admin` reaproveitando a chave GitHub do usuário,
# e dispara bootstrap/check/status/logs/deploy via appctl + update-and-deploy.sh.
# Adaptado de bluedot-handbook/infra/vps/templates/deploy-remoto.sh
# ==============================================================================
set -euo pipefail

APP_NAME="${APP_NAME:-ms-bluedot}"
APP_DIR="${APP_DIR:-/srv/apps/$APP_NAME}"
SSH_ALIAS="${SSH_ALIAS:-vps-admin}"
BRANCH="${BRANCH:-main}"
REPO_URL="${REPO_URL:-}"
COMMAND="deploy"
SKIP_PULL=false
REPO_URL_INFERRED=false

if [[ -z "$REPO_URL" ]]; then
  REPO_URL="$(git config --get remote.origin.url 2>/dev/null || true)"
  REPO_URL_INFERRED=true
fi

if [[ "$REPO_URL_INFERRED" == "true" && "$REPO_URL" =~ ^https://github.com/([^/]+)/([^/]+)(\.git)?$ ]]; then
  repo_owner="${BASH_REMATCH[1]}"
  repo_name="${BASH_REMATCH[2]%.git}"
  REPO_URL="git@github.com:${repo_owner}/${repo_name}.git"
fi

usage() {
  cat <<EOF
Uso: $0 [opção]

Opções:
  --check       valida SSH, agent forwarding, diretório remoto e appctl
  --status      mostra containers via appctl
  --logs        mostra logs via appctl
  --bootstrap   clona o repositório em $APP_DIR quando a app ainda não existe
  --skip-pull   executa deploy sem atualizar o Git remoto
EOF
}

fail() { echo "ERRO: $*" >&2; exit 1; }

while (($# > 0)); do
  case "$1" in
    --check)     COMMAND="check" ;;
    --status)    COMMAND="status" ;;
    --logs)      COMMAND="logs" ;;
    --bootstrap) COMMAND="bootstrap" ;;
    --skip-pull) SKIP_PULL=true ;;
    -h|--help)   usage; exit 0 ;;
    *) echo "Opção desconhecida: $1" >&2; usage >&2; exit 1 ;;
  esac
  shift
done

remote_prefix() {
  printf 'APP_NAME=%q APP_DIR=%q BRANCH=%q REPO_URL=%q COMMAND=%q SKIP_PULL=%q bash -se' \
    "$APP_NAME" "$APP_DIR" "$BRANCH" "$REPO_URL" "$COMMAND" "$SKIP_PULL"
}

echo "Deploy remoto $APP_NAME"
echo "SSH: $SSH_ALIAS | APP_DIR: $APP_DIR | comando: $COMMAND"

ssh -A "$SSH_ALIAS" "$(remote_prefix)" <<'REMOTE_SCRIPT'
set -euo pipefail
fail() { echo "ERRO: $*" >&2; exit 1; }
info() { echo "==> $*"; }
require_command() { command -v "$1" >/dev/null 2>&1 || fail "Comando obrigatório não encontrado: $1"; }

check_base() {
  info "Host: $(hostname) | usuário: $(whoami)"
  require_command git
  require_command sudo
  require_command appctl
  sudo -n true >/dev/null 2>&1 || fail "sudo sem senha não disponível."
}

check_agent() {
  [[ -n "${SSH_AUTH_SOCK:-}" ]] || fail "Agent forwarding não chegou na VPS."
  ssh-add -l >/dev/null 2>&1 || fail "Nenhuma chave encaminhada para a VPS."
}

check_app_dir() {
  [[ -d "$APP_DIR" ]] || fail "Diretório da app não encontrado: $APP_DIR. Rode scripts/deploy-remoto.sh --bootstrap."
  [[ -d "$APP_DIR/.git" ]] || fail "$APP_DIR não parece clone Git."
  [[ -f "$APP_DIR/docker-compose.yml" ]] || fail "docker-compose.yml não encontrado."
  [[ -f "$APP_DIR/.env" ]] || fail ".env não encontrado."
  [[ -x "$APP_DIR/scripts/deploy.sh" ]] || fail "scripts/deploy.sh não executável."
  [[ -x "$APP_DIR/scripts/update-and-deploy.sh" ]] || fail "scripts/update-and-deploy.sh não executável."
}

bootstrap() {
  check_base
  check_agent
  [[ -n "$REPO_URL" ]] || fail "REPO_URL vazio."
  if [[ -d "$APP_DIR" && -n "$(find "$APP_DIR" -mindepth 1 -maxdepth 1 -print -quit 2>/dev/null)" ]]; then
    fail "$APP_DIR já existe e não está vazio."
  fi
  remote_user="$(id -un)"
  remote_group="$(id -gn)"
  sudo -n install -d -o "$remote_user" -g "$remote_group" "$APP_DIR"
  GIT_TERMINAL_PROMPT=0 GIT_SSH_COMMAND="ssh -o BatchMode=yes -o StrictHostKeyChecking=accept-new" \
    git clone --branch "$BRANCH" "$REPO_URL" "$APP_DIR"
  [[ -f "$APP_DIR/.env" ]] || echo "AVISO: crie $APP_DIR/.env antes do deploy." >&2
}

case "$COMMAND" in
  check)
    check_base; check_agent; check_app_dir
    sudo -n appctl "$APP_NAME" status >/dev/null
    info "Preflight OK." ;;
  bootstrap)
    bootstrap ;;
  status)
    check_base; sudo -n appctl "$APP_NAME" status ;;
  logs)
    check_base; sudo -n appctl "$APP_NAME" logs ;;
  deploy)
    check_base; check_agent; check_app_dir
    if [[ "$SKIP_PULL" == "true" ]]; then
      "$APP_DIR/scripts/update-and-deploy.sh" --skip-pull
    else
      "$APP_DIR/scripts/update-and-deploy.sh"
    fi ;;
  *) fail "Comando remoto desconhecido: $COMMAND" ;;
esac
REMOTE_SCRIPT
