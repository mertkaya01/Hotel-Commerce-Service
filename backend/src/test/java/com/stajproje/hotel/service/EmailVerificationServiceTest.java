package com.stajproje.hotel.service;

import com.stajproje.hotel.entity.EmailVerificationToken;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.repository.EmailVerificationTokenRepository;
import com.stajproje.hotel.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * E-posta dogrulama akisi: token uretimi + mail tetikleme, dogrulama, sure dolmus /
 * gecersiz / zaten kullanilmis durumlar.
 */
@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private AuditService auditService;

    @InjectMocks private EmailVerificationService service;

    private User user() {
        return User.builder().id(5L).email("a@b.com").firstName("Ad").lastName("Soyad")
                .emailVerified(false).build();
    }

    @Test
    void createAndSend_shouldSaveToken_andTriggerEmailWithLink() {
        ReflectionTestUtils.setField(service, "frontendUrl", "https://site.app/");
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(inv -> inv.getArgument(0));

        service.createAndSend(user());

        // token kaydedilmeli (24 saat gecerli, kullanilmamis)
        ArgumentCaptor<EmailVerificationToken> tok = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(tok.capture());
        assertThat(tok.getValue().getToken()).isNotBlank();
        assertThat(tok.getValue().isUsed()).isFalse();
        assertThat(tok.getValue().getExpiresAt()).isAfter(LocalDateTime.now());

        // mail, /dogrula?token=... linkiyle tetiklenmeli
        ArgumentCaptor<String> link = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendVerificationEmail(eq("a@b.com"), anyString(), link.capture());
        assertThat(link.getValue()).startsWith("https://site.app/dogrula?token=");
    }

    @Test
    void verify_shouldMarkUserVerified_andTokenUsed() {
        User u = user();
        EmailVerificationToken t = EmailVerificationToken.builder()
                .token("abc").user(u).expiresAt(LocalDateTime.now().plusHours(1)).used(false).build();
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(t));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        String email = service.verify("abc");

        assertThat(email).isEqualTo("a@b.com");
        assertThat(u.isEmailVerified()).isTrue();
        assertThat(t.isUsed()).isTrue();
    }

    @Test
    void verify_shouldThrow_whenTokenInvalid() {
        when(tokenRepository.findByToken("yok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify("yok"))
                .isInstanceOf(IllegalArgumentException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void verify_shouldThrow_whenTokenExpired() {
        EmailVerificationToken t = EmailVerificationToken.builder()
                .token("old").user(user()).expiresAt(LocalDateTime.now().minusHours(1)).used(false).build();
        when(tokenRepository.findByToken("old")).thenReturn(Optional.of(t));

        assertThatThrownBy(() -> service.verify("old"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("süresi");
        verify(userRepository, never()).save(any());
    }

    @Test
    void verify_shouldBeIdempotent_whenAlreadyUsed() {
        User u = user();
        u.setEmailVerified(true);
        EmailVerificationToken t = EmailVerificationToken.builder()
                .token("done").user(u).expiresAt(LocalDateTime.now().plusHours(1)).used(true).build();
        when(tokenRepository.findByToken("done")).thenReturn(Optional.of(t));

        // zaten kullanilmis token -> hata verme, e-postayi don
        String email = service.verify("done");
        assertThat(email).isEqualTo("a@b.com");
        verify(userRepository, never()).save(any());
    }
}
