# Кроссплатформенное тестирование

Тесты настроены для работы на всех платформах: **Mac, Linux, Windows**.

## Быстрая проверка

Для проверки тестов на вашей платформе используйте скрипты:

- **Mac/Linux**: `./verify-tests.sh`
- **Windows**: `.\verify-tests.ps1`

Эти скрипты автоматически проверят оба режима работы (Testcontainers и docker-compose fallback).

## Как это работает

### По умолчанию: Testcontainers (рекомендуется)

По умолчанию тесты используют **Testcontainers**, который автоматически создает и управляет контейнерами:
- ✅ **Mac**: Работает из коробки
- ✅ **Linux**: Работает из коробки  
- ✅ **Windows**: Работает когда Docker Desktop настроен правильно

Testcontainers автоматически определяет платформу и использует правильный способ подключения к Docker.

### Fallback: Docker Compose

Если Testcontainers не может подключиться к Docker, можно использовать контейнеры из `docker-compose.yml`:

1. Запустите docker-compose:
   ```bash
   docker-compose up -d
   ```

2. Отключите Testcontainers в `application-test.properties`:
   ```properties
   testcontainers.enabled=false
   ```

3. Убедитесь, что контейнеры запущены:
   - PostgreSQL на порту `5439`
   - MongoDB на порту `27017`

## Настройка для разных платформ

### Mac / Linux

Testcontainers работает автоматически. Просто запустите тесты:
```bash
./gradlew test
```

### Windows

1. **Убедитесь, что Docker Desktop запущен**
2. **Проверьте подключение:**
   ```powershell
   docker ps
   ```

3. **Если Testcontainers не работает:**
   - Перезапустите Docker Desktop
   - Или используйте fallback (см. выше)

## Переменные окружения

Можно переопределить настройки через переменные окружения:

```bash
# Отключить Testcontainers
export TESTCONTAINERS_ENABLED=false

# Использовать другой PostgreSQL
export TEST_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb

# Использовать другой MongoDB
export TEST_MONGODB_URI=mongodb://localhost:27017/mydb
```

## Структура конфигурации

### gym-crm-core-service
- `TestConfig.java` - конфигурация PostgreSQL с Testcontainers
- `application-test.properties` - настройки подключения к БД

### trainer-workload-service  
- `MongoTestConfig.java` - конфигурация MongoDB с Testcontainers
- `WorkloadServiceTest.java` - использует Testcontainers для unit-тестов
- `application-test.properties` - настройки подключения к MongoDB

## Устранение проблем

### Testcontainers не может подключиться к Docker

**Windows:**
1. Перезапустите Docker Desktop
2. Проверьте настройки Docker Desktop → General → "Use the WSL 2 based engine"
3. Или используйте fallback: `testcontainers.enabled=false`

**Mac/Linux:**
1. Убедитесь, что Docker daemon запущен: `docker ps`
2. Проверьте права доступа к Docker socket

### Тесты падают с ошибкой подключения к БД

1. Проверьте, что контейнеры запущены:
   ```bash
   docker ps
   ```

2. Если используете docker-compose, убедитесь что базы данных созданы:
   ```bash
   # Для PostgreSQL
   docker exec gymcrm-postgres psql -U postgres -c "CREATE DATABASE gymcrm_test;"
   ```

3. Проверьте порты в `application-test.properties` соответствуют docker-compose

## Рекомендации

- **Для CI/CD**: Используйте Testcontainers (по умолчанию)
- **Для локальной разработки**: Можно использовать docker-compose для ускорения
- **Для отладки**: Отключите Testcontainers и используйте существующие контейнеры
