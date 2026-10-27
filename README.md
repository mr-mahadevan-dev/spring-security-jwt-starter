# Spring Security JWT Starter

A reusable Java 21 / Spring Boot 3 starter demonstrating Spring Security, JJWT (HS256), MySQL, Flyway, BCrypt, role-based authorization, and opaque rotating refresh tokens.

## Features
- Registration and login
- BCrypt password hashing
- Signed JWT access tokens (15-minute default)
- Database-backed opaque refresh tokens; only SHA-256 hashes are stored
- Refresh-token rotation and logout revocation
- `USER` / `ADMIN` route and method authorization
- Consistent REST authentication/authorization errors
- MySQL + Flyway migrations
- Docker Compose, Postman collection, OpenAPI UI, Actuator health endpoint
- GitHub Actions CI and security checklist

## Requirements
- Java 21
- Maven 3.9+ (or use the included Dockerfile)
- Docker Compose v2

## Run with Docker Compose
1. Copy `.env` to `.env`.
2. Generate a random HS256 secret and place its Base64 output in `.env`:
   ```bash
   openssl rand -base64 32
   ```
   `JWT_SECRET_BASE64` must decode to at least 32 random bytes. Do not use the example placeholder.
3. Change the local MySQL passwords in `.env`.
4. Start:
   ```bash
   docker compose up --build
   ```
5. Check `http://localhost:8080/actuator/health`.
6. Open Swagger UI at `http://localhost:8080/swagger-ui.html`.

Compose uses MySQL on port 3306 and the application on port 8080. For an existing local MySQL instance, ensure database and credentials match `.env`.

## Run locally
Start MySQL and create the configured database/user, then export the environment variables from `.env` (shells do not automatically load it for a local Maven process). For example, on bash:
```bash
set -a
source .env
set +a
mvn spring-boot:run
```

## API
| Method | Endpoint | Purpose | Access |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Create a USER account and issue tokens | Public |
| POST | `/api/v1/auth/login` | Authenticate and issue tokens | Public |
| POST | `/api/v1/auth/refresh` | Rotate refresh token and issue new access token | Refresh token |
| POST | `/api/v1/auth/logout` | Revoke supplied refresh token | Public endpoint; token must be supplied |
| GET | `/api/v1/auth/me` | Current account | Authenticated |
| GET | `/api/v1/demo/user` | Example user resource | USER or ADMIN |
| GET | `/api/v1/demo/admin` | Example admin resource | ADMIN |

### Register
```bash
curl -i http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"correct-horse-battery"}'
```
Response includes `accessToken`, `expiresIn`, and `refreshToken`. Treat both tokens as secrets.

### Login
```bash
curl -i http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"correct-horse-battery"}'
```

### Call a protected endpoint
```bash
curl -i http://localhost:8080/api/v1/demo/user \
  -H 'Authorization: Bearer YOUR_ACCESS_TOKEN'
```

### Refresh
```bash
curl -i http://localhost:8080/api/v1/auth/refresh \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"YOUR_CURRENT_REFRESH_TOKEN"}'
```
The old refresh token is revoked. Persist the returned refresh token and use it for the next refresh.

### Logout
```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H 'Content-Type: application/json' \
  -d '{"refreshToken":"YOUR_CURRENT_REFRESH_TOKEN"}'
```
Logout revokes the refresh token; an already-issued access token remains valid until it expires.

## Authorization
Public registration cannot set a role: it always creates `USER`. To test the ADMIN route locally, promote a test account directly in the database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'demo@example.com';
```
Do this only in a local development database. Never expose self-service role changes.

## Postman
Import `postman/Spring-Security-JWT-Starter.postman_collection.json`. Register or login first; scripts save the access and refresh tokens as collection variables.

## Tests
```bash
mvn verify
```
The integration test uses Testcontainers and skips itself when Docker is unavailable. Unit tests cover JWT claims and request validation.

## GitHub template setup
1. Push this folder to a new GitHub repository.
2. In repository Settings, enable **Template repository**.
3. Add topics such as `spring-boot`, `spring-security`, `jwt`, `jjwt`, `java`, `authentication`.
4. Keep `.env` and all secrets out of version control.

## Configuration
See `.env`. Key settings:
- `JWT_SECRET_BASE64`: Base64 encoding of a random secret of at least 32 bytes
- `JWT_ISSUER`: expected issuer claim
- `JWT_ACCESS_TTL_SECONDS`: access token lifetime (default 900)
- `JWT_REFRESH_TTL_SECONDS`: refresh token lifetime (default 2592000)
- `CORS_ALLOWED_ORIGINS`: comma-separated exact origins

## Production hardening
Read [`docs/security-checklist.md`](docs/security-checklist.md) and [`docs/architecture.md`](docs/architecture.md) before production use. This is a strong starting template, not a substitute for threat modeling, penetration testing, operational controls, and dependency review. In particular, add rate limiting, email verification/password reset where needed, refresh-token family/replay response, expired-token cleanup, and deployment-specific CSRF/cookie decisions.
