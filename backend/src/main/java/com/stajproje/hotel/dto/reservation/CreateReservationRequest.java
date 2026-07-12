package com.stajproje.hotel.dto.reservation;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CreateReservationRequest {

    @NotNull(message = "Oda secilmeli")
    private Long roomId;

    @NotNull(message = "Giris tarihi bos olamaz")
    @FutureOrPresent(message = "Giris tarihi gecmiste olamaz")
    private LocalDate checkIn;

    @NotNull(message = "Cikis tarihi bos olamaz")
    private LocalDate checkOut;
}
