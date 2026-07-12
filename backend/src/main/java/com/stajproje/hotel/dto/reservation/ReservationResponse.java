package com.stajproje.hotel.dto.reservation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ReservationResponse {

    private Long id;
    private String hotelName;
    private String hotelCode;
    private String roomNumber;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private long nights;
    private BigDecimal totalPrice;
    private String status;
}
