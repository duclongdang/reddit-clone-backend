package com.yourname.redditclone.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Component
public class JwtTokenProvider {
    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${JWT_SECRET}") String secret,
            @Value("${JWT_EXPIRATION_MS:3600000}") long accessTokenExpirationMs,
            @Value("${JWT_REFRESH_EXPIRATION_MS}") long refreshTokenExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(String subject, String email, String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .claim("username", username)
                .claim("tokenType", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(accessTokenExpirationMs)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(refreshTokenExpirationMs)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<Claims> parseClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    public boolean isValid(String token) {
        return parseClaims(token).isPresent();
    }

    public boolean isAccessToken(String token) {
        return hasTokenType(token, "access");
    }

    public boolean isRefreshToken(String token) {
        return hasTokenType(token, "refresh");
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private boolean hasTokenType(String token, String expectedType) {
        return parseClaims(token)
                .map(claims -> expectedType.equals(claims.get("tokenType", String.class)))
                .orElse(false);
    }

    public String getSubject(String token) {
        return parseClaims(token).map(Claims::getSubject).orElse(null);
    }
}
