package com.stajproje.hotel.batch;

import com.stajproje.hotel.entity.Role;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Uygulama açılışında bir "platform admini" (ev sahibi başvurularını değerlendirebilen)
 * hesabı yoksa oluşturur: admin@otel.com / admin1234.
 * Bu hesap, ev sahibi başvuru akışının bootstrap edilmesini sağlar.
 */
@Slf4j
@Component
@Order(0)
@Profile("!test")
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@otel.com";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        User admin = userRepository.findByEmail(ADMIN_EMAIL).orElse(null);

        if (admin == null) {
            admin = User.builder()
                    .email(ADMIN_EMAIL)
                    .password(passwordEncoder.encode("admin1234"))
                    .firstName("Platform")
                    .lastName("Yönetici")
                    .role(Role.SUPER_ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Varsayilan platform yoneticisi olusturuldu: {} / admin1234", ADMIN_EMAIL);
        } else if (admin.getRole() != Role.SUPER_ADMIN) {
            // mevcut hesabın rolünü SUPER_ADMIN'e sabitle (eski kurulumlardan kalmış olabilir)
            admin.setRole(Role.SUPER_ADMIN);
            userRepository.save(admin);
            log.info("{} rolu SUPER_ADMIN olarak guncellendi", ADMIN_EMAIL);
        }
    }
}
