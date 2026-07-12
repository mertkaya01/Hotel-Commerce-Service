package com.stajproje.hotel.repository;

import com.stajproje.hotel.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    Optional<Hotel> findByHotelCode(String hotelCode);

    boolean existsByHotelCode(String hotelCode);
}
