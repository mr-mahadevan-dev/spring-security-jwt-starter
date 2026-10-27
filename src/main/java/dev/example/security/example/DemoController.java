package dev.example.security.example;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class DemoController {
    @GetMapping("/api/v1/demo/user")
    public Map<String, String> user() { return Map.of("message", "You are authenticated as USER or ADMIN."); }

    @GetMapping("/api/v1/demo/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, String> admin() { return Map.of("message", "You are authorized as ADMIN."); }
}
