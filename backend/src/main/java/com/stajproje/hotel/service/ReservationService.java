package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.reservation.CreateReservationRequest;
import com.stajproje.hotel.dto.reservation.ReservationResponse;
import com.stajproje.hotel.entity.Reservation;
import com.stajproje.hotel.entity.ReservationStatus;
import com.stajproje.hotel.entity.Room;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.ReservationNotFoundException;
import com.stajproje.hotel.exception.RoomNotAvailableException;
import com.stajproje.hotel.exception.RoomNotFoundException;
import com.stajproje.hotel.repository.ReservationRepository;
import com.stajproje.hotel.repository.RoomRepository;
import com.stajproje.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @Transactional
    public ReservationResponse create(Long userId, CreateReservationRequest request) {
        if (!request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new IllegalArgumentException("Cikis tarihi giris tarihinden sonra olmali");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException(request.getRoomId()));

        List<Reservation> overlapping = reservationRepository.findOverlapping(
                room.getId(), request.getCheckIn(), request.getCheckOut());
        if (!overlapping.isEmpty()) {
            throw new RoomNotAvailableException();
        }

        User user = userRepository.getReferenceById(userId);

        Reservation reservation = Reservation.builder()
                .user(user)
                .room(room)
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(ReservationStatus.CONFIRMED)
                .build();

        reservationRepository.save(reservation);
        return toResponse(reservation);
    }

    public List<ReservationResponse> getMyReservations(Long userId) {
        return reservationRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void cancel(Long userId, Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException(reservationId));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Bu rezervasyon size ait degil");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    private ReservationResponse toResponse(Reservation reservation) {
        Room room = reservation.getRoom();
        long nights = ChronoUnit.DAYS.between(reservation.getCheckIn(), reservation.getCheckOut());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        return ReservationResponse.builder()
                .id(reservation.getId())
                .hotelName(room.getHotel().getName())
                .hotelCode(room.getHotel().getHotelCode())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType().name())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .nights(nights)
                .totalPrice(totalPrice)
                .status(reservation.getStatus().name())
                .build();
    }
}
