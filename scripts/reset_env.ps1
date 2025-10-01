Param()
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Write-Host "[reset] Stopping and removing compose stack..."
docker-compose down -v | Out-Null

Write-Host "[reset] Removing dangling images and volumes..."
docker volume prune -f | Out-Null
docker image prune -f | Out-Null

Write-Host "[reset] Removing project-specific volumes if exist..."
docker volume rm -f od46s_web_back_postgres_data od46s_web_back_backend_logs od46s_web_back_backend_uploads | Out-Null

Write-Host "[reset] Rebuilding and starting fresh stack..."
docker-compose up -d --build postgres | Out-Null
Start-Sleep -Seconds 5
docker-compose up -d --build backend | Out-Null

Write-Host "[reset] Waiting for backend health..."
while ($true) {
  try {
    $resp = Invoke-WebRequest -UseBasicParsing -Uri http://127.0.0.1:8080/actuator/health -TimeoutSec 3
    if ($resp.StatusCode -eq 200) { break }
  } catch {}
  Start-Sleep -Seconds 2
}

Write-Host "[reset] Done. Backend is healthy."

