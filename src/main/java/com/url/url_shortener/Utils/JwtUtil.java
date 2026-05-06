package com.url.url_shortener.Utils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.crypto.SecretKey;

import com.url.url_shortener.DTO.Auth.Response.TokenPair;
import com.url.url_shortener.Entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtUtil {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;
    @Value("${jwt.expiration.time}")
    private long accessTokenExpiration;
    @Value("${jwt.refresh.expiration.time}")
    private long refreshTokenExpiration; // 7 days by default

    public String extractUsername(String token) {
        return extractClaim(token , claims -> claims.get("username", String.class));
    }

    public <T> T extractClaim(String token,  Function<Claims, T> claimsResolver) {
        token = getTokenFromHeader(token);
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public TokenPair generateTokens(UserDetails userDetails) {
        Map<String, Object> refreshTokenClaims = new HashMap<>();
        Map<String, Object> accessTokenClaims = new HashMap<>();
        User user = (User) userDetails;
        refreshTokenClaims.put("username", user.getUsername());
        refreshTokenClaims.put("email", user.getEmail());
        refreshTokenClaims.put("typ", "refresh");
        refreshTokenClaims.put("jti", UUID.randomUUID().toString());

        accessTokenClaims.put("username", user.getUsername());
        accessTokenClaims.put("email", user.getEmail());
        accessTokenClaims.put("typ", "access");
        accessTokenClaims.put("jti", UUID.randomUUID().toString());

        String refreshToken = generateRefreshToken(refreshTokenClaims, user.getId());
        String accessToken = generateAccessToken(accessTokenClaims, user.getId());

        return TokenPair
                .builder()
                .refreshToken(refreshToken)
                .accessToken(accessToken)
                .build();
    }

    public String generateAccessToken(Map<String, Object> extraClaims, String subject) {
        return Jwts
                .builder()
                .subject(subject)
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(Keys.hmacShaKeyFor(getSignInKey().getEncoded()))
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, String subject) {
        return Jwts
                .builder()
                .subject(subject)
                .claims(extraClaims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(Keys.hmacShaKeyFor(getSignInKey().getEncoded()))
                .compact();
    }


    
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return isTokenSignedByApp(token) && (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    public boolean isTokenSignedByApp(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("typ", String.class));
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(extractTokenType(token));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(extractTokenType(token));
    }

    public boolean isValidRefreshToken(String token) {
        if (!isTokenSignedByApp(token)) {
            return false;
        }

        return isRefreshToken(token) && !isTokenExpired(token);
    }

    public boolean isValidAccessToken(String token) {
        if (!isTokenSignedByApp(token)) {
            return false;
        }

        return isAccessToken(token) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getAccessTokenExpiration() { return accessTokenExpiration; }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public long getTokenExpirationInSeconds(String token) {
        Date expirationDate = extractExpiration(token);
        return (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
    }

    public String getTokenJti(String token) {
        return extractClaim(token, claims -> claims.get("jti", String.class));
    }

    public String extractId(String token) {
        token = getTokenFromHeader(token);
        return extractClaim(token, Claims::getSubject);
    }

    private Claims extractAllClaims(String token) {
        token = getTokenFromHeader(token);
        SecretKey secretKey = Keys.hmacShaKeyFor(getSignInKey().getEncoded());
        return Jwts
                .parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String getTokenFromHeader(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return token;
    }

    private Key getSignInKey() {
        byte[] keyBytes = SECRET_KEY.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
