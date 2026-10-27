# Architecture

- `AuthController`: HTTP endpoints and request validation.
- `AuthService`: registration/login, access-token issuance, refresh-token rotation, logout.
- `JwtService`: HS256 token signing and issuer/signature/expiry verification through JJWT.
- `JwtAuthenticationFilter`: extracts bearer tokens and populates Spring Security's context.
- `SecurityConfig`: stateless security chain, route authorization, CORS, password encoder.
- `User`: application account and role. Public registration always creates `USER`; do not expose role selection to clients.
- `RefreshToken`: stores SHA-256 hashes of opaque high-entropy refresh tokens, not raw token values.
- Flyway: schema migrations; Hibernate validates schema rather than mutating it.

## Token lifecycle
1. Register/login returns a short-lived JWT access token and an opaque refresh token.
2. The database stores only SHA-256(refresh token).
3. Refresh locks the old token row, checks it is active and unexpired, revokes it, stores a new hash, and returns a replacement token.
4. Logout revokes the supplied refresh token.
5. JWT access tokens remain valid until expiration unless additional revocation/version checks are implemented.

## Important limitation
Refresh rotation is transactional and rejects reuse, but this starter does not implement refresh-token family tracking or automatic revocation of all sessions on replay. Add that behavior for higher-risk deployments.
