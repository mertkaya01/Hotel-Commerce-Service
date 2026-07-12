package com.stajproje.hotel.dto.hotel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private String roomType;
    private int capacity;
    private BigDecimal pricePerNight;
}
