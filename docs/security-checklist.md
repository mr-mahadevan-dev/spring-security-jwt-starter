# Security checklist

## Before production
- [ ] Replace local credentials and generate `JWT_SECRET_BASE64` from a cryptographically secure random source (at least 32 random bytes).
- [ ] Store secrets in your platform's secret manager; never commit `.env`.
- [ ] Terminate TLS at a trusted ingress/load balancer; never send bearer tokens over plain HTTP in production.
- [ ] Review CORS origins and allow only exact trusted origins.
- [ ] Add rate limiting and monitoring to login, registration, and refresh endpoints.
- [ ] Configure account verification, password reset, and abuse detection if required by your product.
- [ ] Decide account lockout / throttling behavior without enabling trivial denial of service.
- [ ] Review password policy against your threat model; this template requires 12–72 characters.
- [ ] Decide whether refresh-token reuse should revoke the whole token family/session. This baseline rejects replay but does not yet revoke every session.
- [ ] Add cleanup for expired refresh-token rows.
- [ ] Review audit logging; never log passwords, access tokens, refresh tokens, or signing keys.
- [ ] Review database backups, access controls, migrations, and least-privilege DB account.
- [ ] Set up dependency scanning, secret scanning, and image scanning in CI.
- [ ] Restrict actuator endpoints and Swagger UI in production as appropriate.
- [ ] Add integration tests for refresh-token concurrency/replay, disabled users, and privilege changes.
- [ ] Review the use of JWT role claims: role changes do not affect an already-issued token until it expires unless you add revocation/version checks.
- [ ] For browser apps, carefully choose bearer-token storage. Avoid localStorage for sensitive sessions where possible; consider a BFF or secure HttpOnly cookie design with appropriate CSRF protections.
- [ ] Keep dependencies updated and review Spring/JJWT security advisories.
