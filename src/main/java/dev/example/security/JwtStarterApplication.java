package dev.example.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import dev.example.security.config.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class JwtStarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(JwtStarterApplication.class, args);
    }
}
