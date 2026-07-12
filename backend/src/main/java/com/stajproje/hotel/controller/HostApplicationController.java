package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.host.HostApplicationRequest;
import com.stajproje.hotel.dto.host.HostApplicationResponse;
import com.stajproje.hotel.security.UserPrincipal;
import com.stajproje.hotel.service.HostApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/host-applications")
@RequiredArgsConstructor
public class HostApplicationController {

    private final HostApplicationService applicationService;

    // --- Kullanıcı ---

    @PostMapping
    public ResponseEntity<HostApplicationResponse> apply(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody HostApplicationRequest request) {
        HostApplicationResponse response = applicationService.apply(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<HostApplicationResponse> myApplication(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(applicationService.getMyApplication(principal.getId()));
    }

    // --- Admin (ev sahibi) — sadece ADMIN erişebilir ---

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<HostApplicationResponse> pending() {
        return applicationService.listPending();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public HostApplicationResponse approve(@PathVariable Long id) {
        return applicationService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public HostApplicationResponse reject(@PathVariable Long id) {
        return applicationService.reject(id);
    }
}
