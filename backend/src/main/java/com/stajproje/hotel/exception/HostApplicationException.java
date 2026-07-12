package com.stajproje.hotel.exception;

// Ev sahibi başvurusuyla ilgili durum hataları (zaten beklemede, bulunamadı vb.)
public class HostApplicationException extends RuntimeException {

    public HostApplicationException(String message) {
        super(message);
    }
}
