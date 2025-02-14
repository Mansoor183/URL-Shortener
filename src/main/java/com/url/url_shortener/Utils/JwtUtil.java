package com.url.url_shortener.Utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.url.url_shortener.Entity.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    @Value("${jwt.expiration.time}")
    private long expirationTime;

    public String extractUsername(String token) {
        return extractClaim(token , Claims::getSubject);
    }

    public <T> T extractClaim(String token,  Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        User user = (User) userDetails;
        claims.put("id", user.getId());
        claims.put("email", user.getEmail());
        // claims.put("role", user.getRole());

        return generateToken(claims, user.getUsername());
    }

    public String generateToken(Map<String, Object> extraClaims, String username ) {
        return Jwts
                .builder()
                .claims(extraClaims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(Keys.hmacShaKeyFor(getSignInKey().getEncoded()))
                .compact();
    }
    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    

    public String extractId(String token) {
        token = token.substring(7);
        Claims claims = extractAllClaims(token);
        return claims.get("id", String.class);
    }

    private Claims extractAllClaims(String token) {
        SecretKey secretKey = Keys.hmacShaKeyFor(getSignInKey().getEncoded());
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getSignInKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
