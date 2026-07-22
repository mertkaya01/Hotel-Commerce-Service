package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.auth.AuthResponse;
import com.stajproje.hotel.dto.auth.LoginRequest;
import com.stajproje.hotel.dto.auth.MessageResponse;
import com.stajproje.hotel.dto.auth.RegisterRequest;
import com.stajproje.hotel.service.AuthService;
import com.stajproje.hotel.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** E-posta doğrulama: maildeki linkten gelen token'ı doğrular (herkese açık). */
    @PostMapping("/verify")
    public MessageResponse verify(@RequestParam String token) {
        String email = emailVerificationService.verify(token);
        return new MessageResponse(email + " başarıyla doğrulandı");
    }
}
