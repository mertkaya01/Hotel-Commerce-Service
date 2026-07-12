package com.stajproje.hotel.exception;

public class ReservationNotFoundException extends RuntimeException {

    public ReservationNotFoundException(Long id) {
        super("Rezervasyon bulunamadi: " + id);
    }
}
