package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.auth.MessageResponse;
import com.stajproje.hotel.dto.user.ChangePasswordRequest;
import com.stajproje.hotel.dto.user.UpdateProfileRequest;
import com.stajproje.hotel.dto.user.UserProfileResponse;
import com.stajproje.hotel.security.UserPrincipal;
import com.stajproje.hotel.service.EmailVerificationService;
import com.stajproje.hotel.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final EmailVerificationService emailVerificationService;

    @GetMapping("/me")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getProfile(principal.getId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(@AuthenticationPrincipal UserPrincipal principal,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(principal.getId(), request);
    }

    @PutMapping("/me/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.getId(), request);
        return ResponseEntity.noContent().build();
    }

    /** Doğrulama mailini yeniden gönder (giriş yapmış kullanıcı için). */
    @PostMapping("/me/resend-verification")
    public MessageResponse resendVerification(@AuthenticationPrincipal UserPrincipal principal) {
        emailVerificationService.resend(principal.getId());
        return new MessageResponse("Doğrulama e-postası gönderildi");
    }
}
