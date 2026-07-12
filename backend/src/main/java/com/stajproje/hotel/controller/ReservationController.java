package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.reservation.CreateReservationRequest;
import com.stajproje.hotel.dto.reservation.ReservationResponse;
import com.stajproje.hotel.security.UserPrincipal;
import com.stajproje.hotel.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@AuthenticationPrincipal UserPrincipal principal,
                                                        @Valid @RequestBody CreateReservationRequest request) {
        ReservationResponse response = reservationService.create(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public List<ReservationResponse> getMyReservations(@AuthenticationPrincipal UserPrincipal principal) {
        return reservationService.getMyReservations(principal.getId());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        reservationService.cancel(principal.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
