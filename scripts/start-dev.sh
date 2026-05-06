#!/usr/bin/env bash
# scripts/start-dev.sh
# Inicia o ambiente de desenvolvimento (banco via Docker + app via Maven)
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🚀 Iniciando ambiente de desenvolvimento..."

# 1. Subir banco de dados via Docker
echo "📦 Subindo PostgreSQL..."
docker-compose -f "$PROJECT_DIR/docker-compose.dev.yml" up -d postgres

# 2. Aguardar banco ficar pronto
echo "⏳ Aguardando PostgreSQL..."
until docker exec ms-bluedot-postgres-dev pg_isready -U postgres > /dev/null 2>&1; do
  sleep 1
done
echo "✅ PostgreSQL pronto!"

# 3. Carregar variáveis de ambiente (se existir .env)
if [ -f "$PROJECT_DIR/.env" ]; then
  set -a
  source "$PROJECT_DIR/.env"
  set +a
  echo "✅ Variáveis de ambiente carregadas de .env"
fi

# 4. Iniciar aplicação com Spring Boot
echo "🌱 Iniciando aplicação (perfil: dev)..."
cd "$PROJECT_DIR"
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
