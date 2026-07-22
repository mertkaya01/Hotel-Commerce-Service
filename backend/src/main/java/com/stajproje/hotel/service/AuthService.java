package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.auth.AuthResponse;
import com.stajproje.hotel.dto.auth.LoginRequest;
import com.stajproje.hotel.dto.auth.RegisterRequest;
import com.stajproje.hotel.entity.Role;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.EmailAlreadyExistsException;
import com.stajproje.hotel.repository.UserRepository;
import com.stajproje.hotel.security.JwtService;
import com.stajproje.hotel.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;

    public AuthResponse register(RegisterRequest request) {
        // E-postayi normalize et: "Test@X.com " ve "test@x.com" ayni hesap olmali.
        // (existsByEmail buyuk/kucuk harf duyarli oldugu icin normalize etmezsek
        // ayni e-postayla farkli case'lerde mukerrer hesap acilabilir.)
        String email = normalizeEmail(request.getEmail());

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .role(Role.USER)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        // Dogrulama maili gonder (best-effort/@Async: gonderim kaydı bozmaz).
        // Engellemeyen akis: kullanici hemen giris yapabilir, sadece "dogrulanmadi".
        emailVerificationService.createAndSend(user);

        String token = jwtService.generateToken(new UserPrincipal(user));
        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        // Girisde de normalize et ki kayittaki normalize edilmis e-postayla eslessin.
        String email = normalizeEmail(request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword()));

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        String token = jwtService.generateToken(new UserPrincipal(user));
        return buildAuthResponse(user, token);
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
