package com.url.url_shortener.Service;

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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        @Autowired
        private UserRepository userRepository;
        @Autowired
        private PasswordEncoder passwordEncoder;
        @Autowired
        private JwtUtil jwtService;
        @Autowired
        private AuthenticationManager authenticationManager;

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
                String jwtToken = jwtService.generateToken(user);
                return AuthenticationResponse.builder()
                        .token(jwtToken)
                        .build();
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
                                .orElseThrow();
                        String jwtToken = jwtService.generateToken(user);
                        return AuthenticationResponse.builder()
                                .token(jwtToken)
                                .build();
                }
                catch(BadCredentialsException e) {
                        throw new UnauthorizedException("Invalid username or password");
                }
        }
}
