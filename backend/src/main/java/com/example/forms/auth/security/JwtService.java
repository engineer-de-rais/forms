package com.example.forms.auth.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long ttlSeconds;

    public JwtService(
        @Value("${security.jwt.secret}") String secret,
        @Value("${security.jwt.ttl-seconds:3600}") long ttlSeconds
    ) {
        this.key = buildKey(secret);
        this.ttlSeconds = ttlSeconds;
    }

    public String generateToken(Long userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(ttlSeconds)))
            .signWith(key)
            .compact();
    }

    private SecretKey buildKey(String secret) {
        if (secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() >= 44) {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        }
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(raw);
    }
}
