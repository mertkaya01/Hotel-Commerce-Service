package com.stajproje.hotel.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String email) {
        super("Bu email zaten kayitli: " + email);
    }
}
