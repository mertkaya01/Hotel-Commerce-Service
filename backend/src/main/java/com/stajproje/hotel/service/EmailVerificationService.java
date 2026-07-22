package com.stajproje.hotel.service;

import com.stajproje.hotel.entity.EmailVerificationToken;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.repository.EmailVerificationTokenRepository;
import com.stajproje.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * E-posta doğrulama akışı: token üretip mail gönderir, gelen token'ı doğrular.
 * Engellemeyen akış — doğrulanmasa da giriş yapılabilir, sadece hesap "doğrulandı"
 * işaretlenir.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final int TOKEN_TTL_HOURS = 24;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // Doğrulama linki frontend'e gider; frontend token'ı backend'e doğrulatır.
    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    /** Yeni token üretip kullanıcıya doğrulama maili gönderir (best-effort). */
    @Transactional
    public void createAndSend(User user) {
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        tokenRepository.save(EmailVerificationToken.builder()
                .token(token)
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(TOKEN_TTL_HOURS))
                .build());

        String link = frontendUrl.replaceAll("/+$", "")
                + "/dogrula?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        emailService.sendVerificationEmail(user.getEmail(),
                user.getFirstName() + " " + user.getLastName(), link);
    }

    /**
     * Token'ı doğrular: geçerliyse kullanıcının emailVerified'ını true yapar.
     * @return doğrulanan kullanıcının e-postası
     */
    @Transactional
    public String verify(String token) {
        EmailVerificationToken record = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz doğrulama bağlantısı"));

        if (record.isUsed()) {
            // Zaten doğrulanmış — idempotent davran, hata verme
            return record.getUser().getEmail();
        }
        if (record.isExpired()) {
            throw new IllegalArgumentException("Doğrulama bağlantısının süresi dolmuş. Yeni bir tane iste.");
        }

        User user = record.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        record.setUsed(true);
        tokenRepository.save(record);

        log.info("E-posta dogrulandi: {}", user.getEmail());
        return user.getEmail();
    }

    /** Giriş yapmış kullanıcı için doğrulama mailini yeniden gönderir. */
    @Transactional
    public void resend(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));
        if (user.isEmailVerified()) {
            return; // zaten doğrulanmış
        }
        createAndSend(user);
    }
}
