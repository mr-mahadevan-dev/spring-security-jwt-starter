package dev.example.security.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    ResponseEntity<?> api(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("timestamp", Instant.now().toString(),
            "status", ex.getStatus().value(), "error", ex.getStatus().getReasonPhrase(), "message", ex.getMessage()));
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).distinct().collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now().toString(),"status",400,"error","Bad Request","message",message));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<?> duplicate(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("status",409,"error","Conflict","message","A record with these details already exists"));
    }
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<?> constraint(ConstraintViolationException ex) {
        return ResponseEntity.badRequest().body(Map.of("status",400,"error","Bad Request","message","Request constraint violated"));
    }
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unexpected(Exception ex) {
        return ResponseEntity.status(500).body(Map.of("status",500,"error","Internal Server Error","message","An unexpected error occurred"));
    }
}
