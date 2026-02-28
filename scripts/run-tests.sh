#!/usr/bin/env bash
# scripts/run-tests.sh
# Executa todos os testes com relatório de cobertura JaCoCo
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

echo "🧪 Executando testes..."
cd "$PROJECT_DIR"

# Executar testes com cobertura
./mvnw clean test jacoco:report

echo ""
echo "✅ Testes concluídos!"
echo "📊 Relatório de cobertura: target/site/jacoco/index.html"
echo ""

# Mostrar resumo rápido
echo "📋 Resumo dos testes:"
grep -E "Tests run:|BUILD" target/surefire-reports/*.txt 2>/dev/null | tail -5 || true
