package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.host.HotelListingRequest;
import com.stajproje.hotel.dto.host.HotelListingResponse;
import com.stajproje.hotel.security.UserPrincipal;
import com.stajproje.hotel.service.HotelListingService;
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
@RequestMapping("/api/hotel-listings")
@RequiredArgsConstructor
public class HotelListingController {

    private final HotelListingService listingService;

    // --- Ev sahibi (ADMIN) ---

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HotelListingResponse> submit(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody HotelListingRequest request) {
        HotelListingResponse response = listingService.submit(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN')")
    public List<HotelListingResponse> myListings(@AuthenticationPrincipal UserPrincipal principal) {
        return listingService.getMyListings(principal.getId());
    }

    // --- Platform yöneticisi (SUPER_ADMIN) ---

    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<HotelListingResponse> pending() {
        return listingService.listPending();
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public HotelListingResponse approve(@PathVariable Long id) throws Exception {
        return listingService.approve(id);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public HotelListingResponse reject(@PathVariable Long id) {
        return listingService.reject(id);
    }
}
