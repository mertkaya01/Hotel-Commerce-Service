package com.stajproje.hotel.exception;

public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException() {
        super("Bu oda secilen tarih araliginda musait degil");
    }
}
