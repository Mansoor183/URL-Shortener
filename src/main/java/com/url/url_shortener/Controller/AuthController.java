package com.url.url_shortener.Controller;

import com.url.url_shortener.DTO.Auth.Request.RefreshTokenRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.url.url_shortener.DTO.Auth.Request.AuthenticationRequest;
import com.url.url_shortener.DTO.Auth.Request.RegisterRequest;
import com.url.url_shortener.DTO.Auth.Response.AuthenticationResponse;
import com.url.url_shortener.Service.AuthenticationService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;


@RestController
@RequestMapping("/api/auth/")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;

    @PostMapping("register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }

    @PostMapping("logout")
    public ResponseEntity<HashMap<String, String>> logout(@RequestBody RefreshTokenRequest request) {
        authenticationService.logout(request.getRefreshToken());
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
