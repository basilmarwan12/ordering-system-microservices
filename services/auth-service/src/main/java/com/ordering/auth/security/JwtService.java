package com.ordering.auth.security;

import com.ordering.auth.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Issues HS256 JWTs signed with a secret shared (via env var) with every
 * Resource Server in the system. This replaces the monolith's OAuth2
 * Authorization Server: same end result (a verifiable, stateless token every
 * service can check independently) with far less moving infrastructure at
 * this service count. Revisit if you outgrow a single shared secret --
 * key rotation and per-client scopes are where a real OAuth2 AS earns its
 * complexity back.
 */
@Component
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }

    public String issueToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getUuid().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key,Jwts.SIG.HS256)
                .compact();
    }

    public long expirationSeconds() {
        return expirationMs / 1000;
    }
}
