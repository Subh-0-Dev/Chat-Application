package com.subh.DemoChat.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "5ff590332d1094f44f3698e02d4c6a43182ffce77c6dfb377e7930acbcde40e548eda48d26855b1b6f3fe455faa462b7290c82450508b87581f350c63c0f4377b0466ec15172744cbd0d4831de3e32b58dfe99cc0dea2eda21c462eebc7bb0a0ef07faa25ad0c77da9182b924f2ef22503490459f90bfd56ea35f89e92ce089c49b376b6aa49b2b7bae4a5161f37d185594949b3091194fc024e8c96e14de1bb8ac62509e21f1cfe08f78b52827352ce736c55c1694ec3317dc06faef1dc623333a6c6110faa521947b124d7c4177efcba4318fde17a76a05042c8d6e41aedf7d471af3a89444566d37c0d371327901b448f191f8aec9f0b778a2ab8935362a6";

    // Secret key must be at least 256 bits (32 bytes) for HS256
    private final SecretKey secretKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private final JwtParser jwtParser = Jwts.parser()
            .verifyWith(secretKey)
            .build();

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 *24 * 7)) // 1 hour
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return jwtParser.parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = jwtParser.parseSignedClaims(token).getPayload().getExpiration();
        return expiration.before(new Date());
    }
}
