#!/usr/bin/env bash
# scripts/docker-build.sh
# Build e push da imagem Docker
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

IMAGE_NAME="${IMAGE_NAME:-ms-bluedot}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
FULL_IMAGE="$IMAGE_NAME:$IMAGE_TAG"

echo "🐳 Fazendo build da imagem Docker..."
echo "   Imagem: $FULL_IMAGE"
echo ""

cd "$PROJECT_DIR"

# Build
docker build -t "$FULL_IMAGE" .

echo ""
echo "✅ Imagem criada: $FULL_IMAGE"
echo ""
echo "Para iniciar com docker-compose:"
echo "  docker-compose up -d"
echo ""
echo "Para push (configurar registry primeiro):"
echo "  docker push $FULL_IMAGE"
