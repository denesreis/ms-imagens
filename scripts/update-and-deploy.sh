#!/usr/bin/env bash
# ==============================================================================
# scripts/update-and-deploy.sh - roda NA VPS
# ==============================================================================
# Atualiza repositório com git pull --ff-only e executa scripts/deploy.sh.
# Adaptado de bluedot-handbook/infra/vps/templates/update-and-deploy.sh
# ==============================================================================
set -euo pipefail

ROOT_DIR="${APP_ROOT_DIR:-/srv/apps/ms-bluedot}"
REMOTE="${APP_DEPLOY_REMOTE:-origin}"
SKIP_PULL=false

usage() {
  cat <<'USAGE'
Uso: update-and-deploy.sh [--skip-pull]

Atualiza o repositório da VPS com git pull --ff-only e executa scripts/deploy.sh.
USAGE
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --skip-pull) SKIP_PULL=true ;;
    -h|--help) usage; exit 0 ;;
    *) echo "Opção desconhecida: $1" >&2; usage >&2; exit 1 ;;
  esac
  shift
done

resolve_git_user() {
  if [[ -n "${APP_DEPLOY_GIT_USER:-}" ]]; then printf '%s' "$APP_DEPLOY_GIT_USER"; return; fi
  local owner=""
  owner="$(stat -c '%U' "$ROOT_DIR/.git" 2>/dev/null || true)"
  [[ -n "$owner" ]] && { printf '%s' "$owner"; return; }
  printf '%s' "$(id -un)"
}

GIT_USER="$(resolve_git_user)"

run_git() {
  if [[ "$(id -un)" == "$GIT_USER" ]]; then
    git -C "$ROOT_DIR" "$@"
  else
    sudo -u "$GIT_USER" git -C "$ROOT_DIR" "$@"
  fi
}

cd "$ROOT_DIR"

if [[ "$SKIP_PULL" != "true" ]]; then
  echo "==> Atualizando repositório como $GIT_USER..."
  run_git fetch "$REMOTE"
  BRANCH="$(run_git rev-parse --abbrev-ref HEAD)"
  if [[ -n "$(run_git status --porcelain)" ]]; then
    echo "ERRO: worktree remoto sujo. Resolva antes de continuar." >&2
    run_git status --short >&2
    exit 1
  fi
  LOCAL_SHA="$(run_git rev-parse HEAD)"
  REMOTE_SHA="$(run_git rev-parse "$REMOTE/$BRANCH")"
  if [[ "$LOCAL_SHA" == "$REMOTE_SHA" ]]; then
    echo "Repositório já está atualizado em $BRANCH ($LOCAL_SHA)."
  else
    echo "Aplicando $LOCAL_SHA -> $REMOTE_SHA em $BRANCH..."
    run_git pull --ff-only "$REMOTE" "$BRANCH"
  fi
else
  echo "Pulando git pull por --skip-pull."
fi

if [[ "${EUID:-$(id -u)}" -eq 0 ]]; then
  APP_DIR="$ROOT_DIR" "$ROOT_DIR/scripts/deploy.sh"
else
  sudo APP_DIR="$ROOT_DIR" "$ROOT_DIR/scripts/deploy.sh"
fi
