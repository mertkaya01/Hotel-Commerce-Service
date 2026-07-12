package com.stajproje.hotel.repository;

import com.stajproje.hotel.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUserId(Long userId);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status = 'CONFIRMED'
              AND r.checkIn < :checkOut
              AND r.checkOut > :checkIn
            """)
    List<Reservation> findOverlapping(@Param("roomId") Long roomId,
                                       @Param("checkIn") LocalDate checkIn,
                                       @Param("checkOut") LocalDate checkOut);
}
