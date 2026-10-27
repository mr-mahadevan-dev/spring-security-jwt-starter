package dev.example.security.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Map;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper mapper;
    public RestAccessDeniedHandler(ObjectMapper mapper) { this.mapper = mapper; }
    @Override public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex) throws IOException {
        response.setStatus(403); response.setContentType("application/json");
        mapper.writeValue(response.getOutputStream(), Map.of("status",403,"error","Forbidden","message","Insufficient permissions"));
    }
}
