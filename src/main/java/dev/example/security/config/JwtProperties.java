package dev.example.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secretBase64, String issuer, long accessTtlSeconds, long refreshTtlSeconds) {}
