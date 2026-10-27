package dev.example.security.security;

import dev.example.security.config.JwtProperties;
import dev.example.security.user.Role;
import dev.example.security.user.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private final String secret = Base64.getEncoder().encodeToString(new byte[32]);

    @Test void createsAndParsesSignedToken() {
        JwtService service = new JwtService(new JwtProperties(secret, "test-issuer", 900, 3600));
        User user = new User("person@example.com", "hashed", Role.USER);
        // Entity ID is assigned by the database in production; token claims permit null only in this unit test.
        String token = service.createAccessToken(user);
        Claims claims = service.parseAndValidate(token);
        assertEquals("person@example.com", claims.getSubject());
        assertEquals("test-issuer", claims.getIssuer());
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test void rejectsMissingSecret() {
        assertThrows(IllegalStateException.class, () -> new JwtService(new JwtProperties("", "issuer", 900, 3600)));
    }
}
