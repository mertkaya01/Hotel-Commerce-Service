package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.user.ChangePasswordRequest;
import com.stajproje.hotel.dto.user.UpdateProfileRequest;
import com.stajproje.hotel.dto.user.UserProfileResponse;
import com.stajproje.hotel.entity.Role;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        userRepository.save(user);

        return toResponse(user);
    }

    /**
     * Kullaniciyi "ev sahibi" (ADMIN) rolune yukseltir; boylece otel yonetimi
     * yetkisi kazanir. NOT: Gercek bir uygulamada bu genellikle onay/dogrulama
     * gerektirir; demo icin dogrudan yukseltiyoruz.
     */
    @Transactional
    public UserProfileResponse becomeHost(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));

        user.setRole(Role.ADMIN);
        userRepository.save(user);

        return toResponse(user);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));

        // mevcut sifre dogru mu?
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mevcut sifre hatali");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditService.log(com.stajproje.hotel.entity.AuditEventType.PASSWORD_CHANGED,
                user.getEmail(), "Şifre değiştirildi");
    }

    private UserProfileResponse toResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .emailVerified(user.isEmailVerified())
                .build();
    }
}
