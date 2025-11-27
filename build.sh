#!/usr/bin/env bash
set -euo pipefail

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven not found. Please install Maven (brew install maven) or use your package manager."
  exit 1
fi

mvn clean package

echo "Build complete. JAR in target/" 