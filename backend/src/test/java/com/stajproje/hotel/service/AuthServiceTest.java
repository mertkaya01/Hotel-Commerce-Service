package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.auth.AuthResponse;
import com.stajproje.hotel.dto.auth.RegisterRequest;
import com.stajproje.hotel.entity.Role;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.EmailAlreadyExistsException;
import com.stajproje.hotel.repository.UserRepository;
import com.stajproje.hotel.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("mert@example.com");
        req.setPassword("sifre1234");
        req.setFirstName("Mert");
        req.setLastName("Test");
        return req;
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("mert@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest()))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldNormalizeEmail_beforeCheckAndSave() {
        // Kullanici "  Mert@Example.COM " girse bile normalize edilip saklanmali,
        // boylece farkli case ile mukerrer hesap acilamaz.
        RegisterRequest req = registerRequest();
        req.setEmail("  Mert@Example.COM ");

        when(userRepository.existsByEmail("mert@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASHED_PW");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(req);

        // mukerrer kontrolu normalize edilmis e-posta ile yapilmali
        verify(userRepository).existsByEmail("mert@example.com");

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("mert@example.com");
        assertThat(response.getEmail()).isEqualTo("mert@example.com");
    }

    @Test
    void register_shouldHashPasswordAndAssignUserRole() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode("sifre1234")).thenReturn("HASHED_PW");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        // sifre asla duz metin kaydedilmemeli
        assertThat(saved.getPassword()).isEqualTo("HASHED_PW");
        assertThat(saved.getPassword()).isNotEqualTo("sifre1234");
        assertThat(saved.getRole()).isEqualTo(Role.USER);
        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getEmail()).isEqualTo("mert@example.com");
    }

    @Test
    void register_shouldCreateUnverifiedUser_andTriggerVerificationEmail() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("HASHED_PW");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateToken(any(UserDetails.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(registerRequest());

        // yeni kullanici DOGRULANMAMIS olmali
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().isEmailVerified()).isFalse();
        assertThat(response.isEmailVerified()).isFalse();

        // dogrulama maili tetiklenmeli
        verify(emailVerificationService).createAndSend(any(User.class));
    }
}
