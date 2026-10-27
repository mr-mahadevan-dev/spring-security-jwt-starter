package dev.example.security.security;

import dev.example.security.config.JwtProperties;
import dev.example.security.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Base64;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        if (properties.secretBase64() == null || properties.secretBase64().isBlank()) {
            throw new IllegalStateException("JWT_SECRET_BASE64 is required. Generate one with: openssl rand -base64 32");
        }
        byte[] bytes;
        try { bytes = Base64.getDecoder().decode(properties.secretBase64()); }
        catch (IllegalArgumentException ex) { throw new IllegalStateException("JWT_SECRET_BASE64 must be valid Base64", ex); }
        if (bytes.length < 32) throw new IllegalStateException("HS256 requires at least 32 random secret bytes");
        this.key = Keys.hmacShaKeyFor(bytes);
        if (properties.accessTtlSeconds() < 60 || properties.refreshTtlSeconds() < 300)
            throw new IllegalStateException("JWT/refresh token TTL is too low");
    }

    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().issuer(properties.issuer()).subject(user.getEmail())
            .claim("uid", user.getId()).claim("role", user.getRole().name())
            .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(properties.accessTtlSeconds())))
            .signWith(key, Jwts.SIG.HS256).compact();
    }

    public Claims parseAndValidate(String token) {
        return Jwts.parser().verifyWith(key).requireIssuer(properties.issuer())
            .build().parseSignedClaims(token).getPayload();
    }

    public long accessTtlSeconds() { return properties.accessTtlSeconds(); }
    public long refreshTtlSeconds() { return properties.refreshTtlSeconds(); }
}
