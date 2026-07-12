package com.stajproje.hotel.exception;

public class RoomNotFoundException extends RuntimeException {

    public RoomNotFoundException(Long roomId) {
        super("Oda bulunamadi: " + roomId);
    }
}
