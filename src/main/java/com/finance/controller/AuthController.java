package com.finance.controller;

import com.finance.dto.AuthDTO;
import com.finance.entity.User;
import com.finance.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

@PostMapping(value = "/register", consumes = org.springframework.http.MediaType.ALL_VALUE)
    public ResponseEntity<AuthDTO.AuthResponse> register(@Valid @RequestBody AuthDTO.RegisterRequest request) {
        try {
            Long userId = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthDTO.AuthResponse("User registered successfully", userId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new AuthDTO.AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO.AuthResponse> login(@Valid @RequestBody AuthDTO.LoginRequest request, HttpSession session) {
        try {
            User user = authService.login(request, session);
            return ResponseEntity.ok(new AuthDTO.AuthResponse("Login successful"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthDTO.AuthResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthDTO.AuthResponse> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(new AuthDTO.AuthResponse("Logout successful"));
    }
}
