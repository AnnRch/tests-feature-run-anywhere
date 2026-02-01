# Скрипт для проверки тестов на Windows

Write-Host "=== Проверка тестов на Windows ===" -ForegroundColor Cyan
Write-Host ""

# Проверка Docker
Write-Host "1. Проверка Docker..." -ForegroundColor Yellow
try {
    $dockerVersion = docker --version
    Write-Host "✅ Docker установлен: $dockerVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker не установлен" -ForegroundColor Red
    exit 1
}

try {
    docker ps | Out-Null
    Write-Host "✅ Docker запущен" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker не запущен. Запустите Docker Desktop" -ForegroundColor Red
    exit 1
}

# Проверка Testcontainers (может не работать на Windows)
Write-Host ""
Write-Host "2. Попытка запуска тестов с Testcontainers..." -ForegroundColor Yellow
Write-Host "   (Может не работать из-за проблем с Docker Desktop на Windows)" -ForegroundColor Gray

Set-Location gym-crm-core-service
$coreTestcontainers = ..\gradlew clean test --no-daemon 2>&1 | Select-String -Pattern "BUILD SUCCESSFUL"
Set-Location ..

Set-Location trainer-workload-service
$workloadTestcontainers = ..\gradlew clean test --no-daemon 2>&1 | Select-String -Pattern "BUILD SUCCESSFUL"
Set-Location ..

if ($coreTestcontainers -and $workloadTestcontainers) {
    Write-Host "✅ Тесты с Testcontainers прошли успешно" -ForegroundColor Green
} else {
    Write-Host "⚠️  Testcontainers не работает (ожидаемо на Windows)" -ForegroundColor Yellow
}

# Проверка с docker-compose (fallback)
Write-Host ""
Write-Host "3. Проверка fallback на docker-compose..." -ForegroundColor Yellow
Write-Host "Запуск docker-compose..." -ForegroundColor Gray
docker-compose up -d postgres mongodb
Start-Sleep -Seconds 5

# Создание тестовой БД
Write-Host "Создание тестовой БД..." -ForegroundColor Gray
docker exec gymcrm-postgres psql -U postgres -c "CREATE DATABASE gymcrm_test;" 2>$null

# Отключение Testcontainers через свойство
Write-Host "Запуск тестов с отключенным Testcontainers..." -ForegroundColor Gray

Set-Location gym-crm-core-service
$coreFallback = ..\gradlew clean test --no-daemon -Dtestcontainers.enabled=false 2>&1 | Select-String -Pattern "BUILD SUCCESSFUL"
Set-Location ..

Set-Location trainer-workload-service
$workloadFallback = ..\gradlew clean test --no-daemon -Dtestcontainers.enabled=false 2>&1 | Select-String -Pattern "BUILD SUCCESSFUL"
Set-Location ..

Write-Host ""
Write-Host "=== Итоги ===" -ForegroundColor Cyan
if ($coreTestcontainers -and $workloadTestcontainers) {
    Write-Host "✅ Testcontainers: РАБОТАЕТ" -ForegroundColor Green
} else {
    Write-Host "⚠️  Testcontainers: НЕ РАБОТАЕТ (нормально для Windows)" -ForegroundColor Yellow
}

if ($coreFallback -and $workloadFallback) {
    Write-Host "✅ Docker Compose fallback: РАБОТАЕТ" -ForegroundColor Green
} else {
    Write-Host "❌ Docker Compose fallback: НЕ РАБОТАЕТ" -ForegroundColor Red
}
