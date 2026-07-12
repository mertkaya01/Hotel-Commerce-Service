package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.reservation.CreateReservationRequest;
import com.stajproje.hotel.dto.reservation.ReservationResponse;
import com.stajproje.hotel.entity.Hotel;
import com.stajproje.hotel.entity.Reservation;
import com.stajproje.hotel.entity.ReservationStatus;
import com.stajproje.hotel.entity.Room;
import com.stajproje.hotel.entity.RoomType;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.ReservationNotFoundException;
import com.stajproje.hotel.exception.RoomNotAvailableException;
import com.stajproje.hotel.exception.RoomNotFoundException;
import com.stajproje.hotel.repository.ReservationRepository;
import com.stajproje.hotel.repository.RoomRepository;
import com.stajproje.hotel.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private ReservationService reservationService;

    private Room room;
    private User user;

    @BeforeEach
    void setUp() {
        Hotel hotel = Hotel.builder().id(1L).hotelCode("H1").name("Test Otel").build();
        room = Room.builder()
                .id(10L)
                .hotel(hotel)
                .roomNumber("101")
                .roomType(RoomType.DOUBLE)
                .capacity(2)
                .pricePerNight(new BigDecimal("100.00"))
                .build();
        user = User.builder().id(5L).email("a@b.com").build();
    }

    private CreateReservationRequest request(String checkIn, String checkOut) {
        CreateReservationRequest req = new CreateReservationRequest();
        req.setRoomId(10L);
        req.setCheckIn(LocalDate.parse(checkIn));
        req.setCheckOut(LocalDate.parse(checkOut));
        return req;
    }

    @Test
    void create_shouldSucceed_whenRoomIsAvailable() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(reservationRepository.findOverlapping(anyLong(), any(), any())).thenReturn(List.of());
        when(userRepository.getReferenceById(5L)).thenReturn(user);
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservationResponse response = reservationService.create(5L, request("2026-08-10", "2026-08-15"));

        assertThat(response.getNights()).isEqualTo(5);
        // 100 TL x 5 gece = 500 TL
        assertThat(response.getTotalPrice()).isEqualByComparingTo("500.00");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void create_shouldThrow_whenDatesOverlapExistingReservation() {
        when(roomRepository.findById(10L)).thenReturn(Optional.of(room));
        when(reservationRepository.findOverlapping(anyLong(), any(), any()))
                .thenReturn(List.of(new Reservation()));

        assertThatThrownBy(() -> reservationService.create(5L, request("2026-08-12", "2026-08-18")))
                .isInstanceOf(RoomNotAvailableException.class);

        // cakisma varsa kayit yapilmamali
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void create_shouldThrow_whenCheckOutNotAfterCheckIn() {
        assertThatThrownBy(() -> reservationService.create(5L, request("2026-08-15", "2026-08-15")))
                .isInstanceOf(IllegalArgumentException.class);

        // tarih gecersizse odaya bile bakilmamali
        verify(roomRepository, never()).findById(anyLong());
    }

    @Test
    void create_shouldThrow_whenRoomDoesNotExist() {
        when(roomRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.create(5L, request("2026-08-10", "2026-08-15")))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    void cancel_shouldThrowAccessDenied_whenReservationBelongsToAnotherUser() {
        Reservation reservation = Reservation.builder()
                .id(99L)
                .user(user) // sahibi id=5
                .room(room)
                .checkIn(LocalDate.parse("2026-08-10"))
                .checkOut(LocalDate.parse("2026-08-15"))
                .status(ReservationStatus.CONFIRMED)
                .build();
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));

        // baska bir kullanici (id=7) iptal etmeye calisiyor
        assertThatThrownBy(() -> reservationService.cancel(7L, 99L))
                .isInstanceOf(AccessDeniedException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    void cancel_shouldSetStatusCancelled_whenOwnerCancels() {
        Reservation reservation = Reservation.builder()
                .id(99L)
                .user(user)
                .room(room)
                .checkIn(LocalDate.parse("2026-08-10"))
                .checkOut(LocalDate.parse("2026-08-15"))
                .status(ReservationStatus.CONFIRMED)
                .build();
        when(reservationRepository.findById(99L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        reservationService.cancel(5L, 99L);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void cancel_shouldThrow_whenReservationNotFound() {
        when(reservationRepository.findById(1234L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.cancel(5L, 1234L))
                .isInstanceOf(ReservationNotFoundException.class);
    }
}
