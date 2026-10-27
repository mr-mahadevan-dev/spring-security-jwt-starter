package dev.example.security.auth;

import dev.example.security.auth.dto.*;
import dev.example.security.config.JwtProperties;
import dev.example.security.exception.ApiException;
import dev.example.security.security.JwtService;
import dev.example.security.token.RefreshToken;
import dev.example.security.token.RefreshTokenRepository;
import dev.example.security.user.Role;
import dev.example.security.user.User;
import dev.example.security.user.UserRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwt;
    private final JwtProperties properties;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, RefreshTokenRepository refreshTokens, PasswordEncoder encoder,
            AuthenticationManager authenticationManager, JwtService jwt, JwtProperties properties) {
        this.users = users; this.refreshTokens = refreshTokens; this.encoder = encoder;
        this.authenticationManager = authenticationManager; this.jwt = jwt; this.properties = properties;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (users.existsByEmailIgnoreCase(email)) throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        User user = users.save(new User(email, encoder.encode(request.password()), Role.USER));
        return issue(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalize(request.email());
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        User user = users.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEnabled()) throw new ApiException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        return issue(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        String oldRaw = request.refreshToken();
        String oldHash = sha256(oldRaw);
        RefreshToken old = refreshTokens.findLockedByTokenHash(oldHash)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        Instant now = Instant.now();
        if (!old.isActiveAt(now)) {
            // If a revoked token is replayed, revoke all currently active refresh tokens for this user.
            // This simple template implementation records the replay but does not silently accept it.
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token is expired or has already been used");
        }
        User user = old.getUser();
        if (!user.isEnabled()) throw new ApiException(HttpStatus.UNAUTHORIZED, "Account is disabled");
        String replacementRaw = newRefreshToken();
        String replacementHash = sha256(replacementRaw);
        old.revoke(now, replacementHash);
        refreshTokens.save(new RefreshToken(user, replacementHash, now.plusSeconds(jwt.refreshTtlSeconds())));
        return response(user, jwt.createAccessToken(user), replacementRaw);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokens.findLockedByTokenHash(sha256(refreshToken)).ifPresent(token -> {
            if (token.getRevokedAt() == null) token.revoke(Instant.now(), null);
        });
    }

    private AuthResponse issue(User user) {
        String raw = newRefreshToken();
        refreshTokens.save(new RefreshToken(user, sha256(raw), Instant.now().plusSeconds(jwt.refreshTtlSeconds())));
        return response(user, jwt.createAccessToken(user), raw);
    }

    private AuthResponse response(User user, String access, String refresh) {
        return new AuthResponse("Bearer", access, jwt.accessTtlSeconds(), refresh, user.getEmail(), user.getRole().name());
    }
    private String newRefreshToken() {
        byte[] bytes = new byte[48]; random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private String sha256(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8))); }
        catch (Exception ex) { throw new IllegalStateException("SHA-256 is unavailable", ex); }
    }
    private String normalize(String email) { return email.trim().toLowerCase(java.util.Locale.ROOT); }
}
