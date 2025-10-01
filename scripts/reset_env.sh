#!/usr/bin/env bash
set -euo pipefail

echo "[reset] Stopping and removing compose stack..."
docker-compose down -v || true

echo "[reset] Removing dangling images and volumes..."
docker volume prune -f || true
docker image prune -f || true

echo "[reset] Removing project-specific volumes if exist..."
docker volume rm -f od46s_web_back_postgres_data od46s_web_back_backend_logs od46s_web_back_backend_uploads || true

echo "[reset] Rebuilding and starting fresh stack..."
docker-compose up -d --build postgres
sleep 5
docker-compose up -d --build backend

echo "[reset] Waiting for backend health..."
until curl -sf http://127.0.0.1:8080/actuator/health >/dev/null; do
  sleep 2
done

echo "[reset] Done. Backend is healthy."

