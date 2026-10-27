package dev.example.security.auth;

import dev.example.security.auth.dto.RegisterRequest;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisterRequestTest {
    @Test void rejectsShortPassword() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        assertFalse(validator.validate(new RegisterRequest("a@example.com", "short")).isEmpty());
    }
    @Test void acceptsValidInput() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        assertTrue(validator.validate(new RegisterRequest("a@example.com", "correct-horse-battery")).isEmpty());
    }
}
