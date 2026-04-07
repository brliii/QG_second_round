package com.example.backend.utils;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Long expiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    //生成token
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date endDate = new Date(now.getTime() + expiration);
        return Jwts.builder().setSubject(userId.toString()).claim("username", username).setIssuedAt(now).setExpiration(endDate).signWith(getSigningKey(), SignatureAlgorithm.HS256).compact();
    }

    private Claims analyseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = analyseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    public String getUsernameFromToken(String token) {
        Claims claims = analyseToken(token);
        return claims.get("username", String.class);
    }

    public boolean validateToken(String token) {
        try {
            analyseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
