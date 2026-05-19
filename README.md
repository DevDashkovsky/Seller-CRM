# CRM

Тестовое для ШИФТ Лаборатория. Спека — `SHIFT_Lab_Java_2b1db1e019.pdf` в корне.
Контракт API — `openapi.yml`.

Учёт продавцов и их транзакций, плюс пара аналитических ручек: топ продавец
за период, продавцы с суммой ниже порога, и поиск самого продуктивного окна
по конкретному продавцу.

## Стек

Spring Boot 4 на Java 17. Spring Web + Spring Data JPA + Hibernate.
PostgreSQL в prod, H2 в dev и тестах. Liquibase для миграций.
Тесты — JUnit 5, Mockito, MockMvc. Покрытие через JaCoCo.
Swagger UI через springdoc.

## Запуск

```bash
./gradlew bootRun
```

По умолчанию dev-профиль с H2 в памяти. Слушает 8080.

Profile-ы:

- `dev` — H2, h2-console включена
- `test` — H2 (используется gradle test)
- `prod` — Postgres, креды через `DB_URL`, `DB_USER`, `DB_PASSWORD`

```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

Схема накатывается через Liquibase (`src/main/resources/db/changelog/`).

## Куда тыкать

- Swagger: <http://localhost:8080/swagger-ui.html>
- H2 console (только dev): <http://localhost:8080/h2-console>
  - URL: `jdbc:h2:mem:crmdev`, user `sa`, пароль пустой

## Примеры

Полный список ручек — в swagger. Тут несколько на разогрев.

Создать продавца:
```bash
curl -X POST localhost:8080/api/v1/sellers \
  -H 'Content-Type: application/json' \
  -d '{"name":"Egor","contactInfo":"egor@example.com"}'
```

Создать транзакцию:
```bash
curl -X POST localhost:8080/api/v1/transactions \
  -H 'Content-Type: application/json' \
  -d '{"sellerId":1,"amount":1500.50,"paymentType":"CARD","transactionDate":"2026-05-15T12:00:00"}'
```

`paymentType` — `CASH`, `CARD` или `TRANSFER`.

Транзакции конкретного продавца:
```bash
curl localhost:8080/api/v1/sellers/1/transactions
```

Аналитика:
```bash
# топ продавец за май
curl 'localhost:8080/api/v1/analytics/top-seller?period=MONTH&date=2026-05-15'

# продавцы с суммой < 1000 за май
curl 'localhost:8080/api/v1/analytics/sellers/below?from=2026-05-01T00:00:00&to=2026-06-01T00:00:00&threshold=1000'

# самое продуктивное окно 7 дней
curl 'localhost:8080/api/v1/analytics/sellers/1/best-period?windowDays=7'
```

Если за период нет данных — аналитика отдаёт `204 No Content`.

## Структура

```
com.shiftlab.crm
├── controller     REST
├── service        бизнес-логика
├── repository     JPA + JPQL для аналитики
├── entity         Seller, Transaction, и enum-ы
├── dto            request / response / analytics
├── mapper         entity ↔ dto
└── exception      GlobalExceptionHandler
```

Soft-delete через `@SQLDelete` + `@SQLRestriction` — записи помечаются `deleted=true`
и не возвращаются в выборках. Аудит-поля (`createdAt`, `updatedAt`) через
`@EnableJpaAuditing`.

`PeriodType.toRange()` превращает (период + опорная дата) в полуоткрытый диапазон
`[from, to)`. Все JPQL-запросы аналитики используют `>= AND <`, чтобы граница
не попадала дважды.

Best-period работает sliding window'ом по отсортированным транзакциям продавца —
`O(n)`. При равном количестве транзакций выбирается окно с большей суммой.

## Тесты

```bash
./gradlew test
```

Отчёт JaCoCo после прогона — `build/reports/jacoco/test/html/index.html`.
Покрытие выше 50% (требование пдф).

Сервисы покрыты юнит-тестами (Mockito), контроллеры — MockMvc-тестами на
основные сценарии (happy + 4xx).

## Что не делал

- У транзакций нет update/delete — в списке ручек из пдф их нет, оставил
  иммутабельными.
- `sellers/below` показывает только продавцов с хотя бы одной транзакцией
  в периоде. Те у кого совсем нет транзакций (0 < threshold) формально подходят,
  но в `GROUP BY` не попадают — оставил как есть.
