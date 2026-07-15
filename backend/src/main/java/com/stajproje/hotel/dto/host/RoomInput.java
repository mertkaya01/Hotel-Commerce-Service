package com.stajproje.hotel.dto.host;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RoomInput {

    @NotNull(message = "Oda tipi secilmeli")
    private String roomType; // SINGLE / DOUBLE / SUITE / DELUXE

    @NotNull
    @Min(value = 1, message = "Kapasite en az 1")
    private Integer capacity;

    @NotNull
    @Min(value = 1, message = "Fiyat 0'dan buyuk olmali")
    private BigDecimal pricePerNight;
}
