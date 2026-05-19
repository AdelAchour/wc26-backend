# Deployment

Production deployment runs on an Oracle Cloud VM
with the Ktor app and Postgres as Docker containers,
nginx as reverse proxy on the host, and Let's Encrypt for TLS.

Public URL: `https://wc26.adelash.dev`

## Architecture

Internet (HTTPS)
│
▼
┌──────────────────────────────────────┐
│  Oracle Cloud VM          │
│                                      │
│  nginx (host, ports 80 & 443)        │
│    │ reverse-proxies                 │
│    ▼                                 │
│  ┌──────────────────────────────┐    │
│  │ Docker stack                 │    │
│  │   wc26-app                   │    │
│  │   wc26-postgres              │    │
│  └──────────────────────────────┘    │
└──────────────────────────────────────┘

## Files

- `docker-compose.prod.yml` — the production stack definition
- `.env.example` — required environment variables (real values in `.env`, gitignored)

## Manual deploy

On the VM:

1. SSH in
2. `cd ~/wc26`
3. Pull the latest image: `docker pull ghcr.io/adelachour/wc26-backend:latest`
4. Restart the stack: `docker compose -f docker-compose.prod.yml up -d`

For automated deploys, see the GitHub Actions workflow.

## Verifying

```bash
# Check both containers are running
docker compose -f docker-compose.prod.yml ps

# Check logs
docker compose -f docker-compose.prod.yml logs -f app

# Local smoke test (from VM)
curl http://localhost:8080
```

## Secrets

Real secrets are stored in `.env` on the VM only.
File permissions: `chmod 600 .env` (owner read/write only).

For first-time setup, generate:

```bash
# Strong DB password (32 alphanumeric chars)
openssl rand -base64 32 | tr -d '/+=' | head -c 32

# Strong JWT secret (64+ bytes base64)
openssl rand -base64 64 | tr -d '\n'
```