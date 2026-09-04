#!/bin/sh
set -e

# Démarre le serveur Ollama en arrière-plan.
ollama serve &
SERVER_PID=$!

# Attend que l'API soit prête.
echo "Waiting for Ollama to start..."
until curl -s http://localhost:11434 >/dev/null 2>&1; do
  sleep 1
done
echo "Ollama is up."

# Précharge le modèle cible (s'adapte au volume éphémère de Railway).
echo "Pulling model: ${OLLAMA_MODEL}..."
ollama pull "${OLLAMA_MODEL}"

# Surveille Ollama tant que le conteneur est vivant.
wait $SERVER_PID
