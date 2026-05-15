# wc26-backend

Ktor backend for a FIFA World Cup 2026 fan social app. Part of a portfolio
project showcasing modern backend + Android development.

**Status:** under active development.

## Stack

- Kotlin + Ktor
- PostgreSQL 16
- Exposed (SQL framework)
- Flyway (migrations)
- Docker + Docker Compose
- Oracle Cloud (planned deployment)

## Local development

Prerequisites: JDK 21, Docker.

```bash
# Start the database
docker compose up -d

# Run the Ktor server
./gradlew run
```

The API will be available at `http://localhost:8080`.
pgAdmin (web-based DB GUI) at `http://localhost:5050`.

## Architecture

_Coming soon — schema diagram, API surface, deployment topology._

## Related repos

- `wc26-android` (coming soon)
- `wc26-kmp` (coming soon)