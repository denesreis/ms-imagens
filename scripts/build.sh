#!/usr/bin/env bash
# scripts/build.sh
# Gera o WAR para deploy
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🏗️  Gerando WAR..."
cd "$PROJECT_DIR"

./mvnw clean package -DskipTests

WAR_FILE=$(find target -name "*.war" | head -1)
WAR_SIZE=$(du -sh "$WAR_FILE" | cut -f1)

echo ""
echo "✅ Build concluído!"
echo "📦 Arquivo: $WAR_FILE ($WAR_SIZE)"
echo "🚀 Deploy: copie o WAR para /usr/local/tomcat/webapps/"
