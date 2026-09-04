# Service Ollama dédié pour le déploiement Railway.
# Ce Dockerfile garantit que le modèle configuré via $OLLAMA_MODEL est
# téléchargé au démarrage (le stockage Railway est éphémère entre redémarrages).
FROM ollama/ollama:latest

# Modèle à précharger (défini par défaut, surchargeable dans Railway).
ENV OLLAMA_MODEL=qwen2.5:7b
ENV OLLAMA_HOST=0.0.0.0
ENV OLLAMA_KEEP_ALIVE=24h

EXPOSE 11434

# Au démarrage : démarre Ollama, tire le modèle, puis reste au premier plan.
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
