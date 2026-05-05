package com.url.url_shortener.Service;

import com.url.url_shortener.DTO.Auth.Request.RefreshTokenRequest;
import com.url.url_shortener.DTO.Auth.Response.TokenPair;
import com.url.url_shortener.Exceptions.ResourceNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.url.url_shortener.DTO.Auth.Request.AuthenticationRequest;
import com.url.url_shortener.DTO.Auth.Request.RegisterRequest;
import com.url.url_shortener.DTO.Auth.Response.AuthenticationResponse;
import com.url.url_shortener.Entity.User;
import com.url.url_shortener.Exceptions.InvalidRequest;
import com.url.url_shortener.Exceptions.UnauthorizedException;
import com.url.url_shortener.Repository.UserRepository;
import com.url.url_shortener.Utils.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtUtil jwtUtil;
        @Autowired
        private AuthenticationManager authenticationManager;
        @Autowired
        private RedisTemplate<String, String> redisTemplate;

        public AuthenticationResponse register(RegisterRequest request) {
                if(request.getEmail() == null || request.getPassword() == null || request.getUsername() == null) {
                        throw new InvalidRequest("Email, password and username are required fields");
                }
                else if(userRepository.existsByEmailOrUsername(request.getEmail(), request.getUsername())) {
                        throw new InvalidRequest("Email or Username exists");
                } 
                User user = User.builder()
                        .username(request.getUsername())
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .build();

                userRepository.save(user);
                return generateAuthenticationResponse(user);
        }
        
        public AuthenticationResponse authenticate(AuthenticationRequest request) {
                try {
                        authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                        request.getUsername(),
                                        request.getPassword()
                                )
                        );
                        
                        var user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new ResourceNotFound("Username not found"));
                        return generateAuthenticationResponse(user);
                }
                catch(BadCredentialsException e) {
                        throw new UnauthorizedException("Invalid username or password");
                }
        }

        public AuthenticationResponse refreshToken(RefreshTokenRequest request) {

                String refreshToken = request.getRefreshToken();

                if (!jwtUtil.isTokenSignedByApp(refreshToken)) {
                        throw new UnauthorizedException("Invalid refresh token signature");
                }

                if (jwtUtil.isTokenExpired(refreshToken)) {
                        throw new UnauthorizedException("Refresh token has expired");
                }

                if (!jwtUtil.isRefreshToken(refreshToken)) {
                        throw new UnauthorizedException("Invalid token type. Expected refresh token");
                }

                String jti = jwtUtil.getTokenJti(refreshToken);
                Boolean isBlacklisted = redisTemplate.hasKey("refresh_token:blacklist:" + jti);
                if (Boolean.TRUE.equals(isBlacklisted)) {
                        throw new UnauthorizedException("Refresh token has been revoked");
                }

                String username = jwtUtil.extractUsername(refreshToken);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> new ResourceNotFound("Username not found"));

                return generateAuthenticationResponse(user);
        }

        public void logout(String refreshToken) {
                if (!jwtUtil.isTokenSignedByApp(refreshToken)) {
                        throw new UnauthorizedException("Invalid refresh token signature");
                }

                if (!jwtUtil.isRefreshToken(refreshToken)) {
                        throw new UnauthorizedException("Invalid token type. Expected refresh token");
                }

                String jti = jwtUtil.getTokenJti(refreshToken);
                long ttlInSeconds = jwtUtil.getTokenExpirationInSeconds(refreshToken);

                if (ttlInSeconds > 0) {
                        redisTemplate.opsForValue().set(
                                "refresh_token:blacklist:" + jti,
                                "revoked",
                                ttlInSeconds,
                                TimeUnit.SECONDS
                        );
                }
        }

        private AuthenticationResponse generateAuthenticationResponse(User user) {
                TokenPair tokenPair = jwtUtil.generateTokens(user);

                return AuthenticationResponse.builder()
                        .accessToken(tokenPair.getAccessToken())
                        .refreshToken(tokenPair.getRefreshToken())
                        .expiresIn(jwtUtil.getAccessTokenExpiration() / 1000)
                        .tokenType("Bearer")
                        .build();
        }
}
