package com.group.hackathon_G5Dev.api.controller;

import com.group.hackathon_G5Dev.api.dto.request.LoginRequest;
import com.group.hackathon_G5Dev.api.dto.request.RegisterRequest;
import com.group.hackathon_G5Dev.api.dto.response.AuthResponse;
import com.group.hackathon_G5Dev.domain.model.User;
import com.group.hackathon_G5Dev.domain.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        String token = authService.generateToken(user);
        return ResponseEntity.ok(toAuthResponse(user, token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.authenticate(request.email(), request.password());
        String token = authService.generateToken(user);
        return ResponseEntity.ok(toAuthResponse(user, token));
    }

    private AuthResponse toAuthResponse(User user, String token) {
        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
