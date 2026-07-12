package com.stajproje.hotel.exception;

public class HotelNotFoundException extends RuntimeException {

    public HotelNotFoundException(String hotelCode) {
        super("Otel bulunamadi: " + hotelCode);
    }
}
