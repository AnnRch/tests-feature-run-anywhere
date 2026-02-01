#!/bin/bash
# Скрипт для проверки тестов на всех платформах (Mac/Linux)

set -e

echo "=== Проверка тестов на Unix-платформах (Mac/Linux) ==="
echo ""

# Проверка Docker
echo "1. Проверка Docker..."
if ! command -v docker &> /dev/null; then
    echo "❌ Docker не установлен"
    exit 1
fi

if ! docker ps &> /dev/null; then
    echo "❌ Docker не запущен. Запустите Docker Desktop или Docker daemon"
    exit 1
fi
echo "✅ Docker доступен"

# Проверка Testcontainers (по умолчанию включен)
echo ""
echo "2. Запуск тестов с Testcontainers (по умолчанию)..."
cd gym-crm-core-service
../gradlew clean test --no-daemon
CORE_RESULT=$?
cd ..

cd trainer-workload-service
../gradlew clean test --no-daemon
WORKLOAD_RESULT=$?
cd ..

if [ $CORE_RESULT -eq 0 ] && [ $WORKLOAD_RESULT -eq 0 ]; then
    echo "✅ Тесты с Testcontainers прошли успешно"
else
    echo "❌ Тесты с Testcontainers не прошли"
fi

# Проверка с docker-compose (fallback)
echo ""
echo "3. Проверка fallback на docker-compose..."
echo "Запуск docker-compose..."
docker-compose up -d postgres mongodb
sleep 5

# Создание тестовой БД если нужно
docker exec gymcrm-postgres psql -U postgres -c "CREATE DATABASE gymcrm_test;" 2>/dev/null || true

# Отключение Testcontainers
export TESTCONTAINERS_ENABLED=false

cd gym-crm-core-service
../gradlew clean test --no-daemon -Dtestcontainers.enabled=false
CORE_FALLBACK=$?
cd ..

cd trainer-workload-service
../gradlew clean test --no-daemon -Dtestcontainers.enabled=false
WORKLOAD_FALLBACK=$?
cd ..

if [ $CORE_FALLBACK -eq 0 ] && [ $WORKLOAD_FALLBACK -eq 0 ]; then
    echo "✅ Тесты с docker-compose прошли успешно"
else
    echo "❌ Тесты с docker-compose не прошли"
fi

echo ""
echo "=== Итоги ==="
if [ $CORE_RESULT -eq 0 ] && [ $WORKLOAD_RESULT -eq 0 ]; then
    echo "✅ Testcontainers: РАБОТАЕТ"
else
    echo "❌ Testcontainers: НЕ РАБОТАЕТ"
fi

if [ $CORE_FALLBACK -eq 0 ] && [ $WORKLOAD_FALLBACK -eq 0 ]; then
    echo "✅ Docker Compose fallback: РАБОТАЕТ"
else
    echo "❌ Docker Compose fallback: НЕ РАБОТАЕТ"
fi
