package com.stajproje.hotel.entity;

public enum HotelStatus {
    APPROVED,  // yayında — aramada görünür (CSV importları + onaylananlar)
    PENDING,   // ev sahibi ekledi, onay bekliyor (aramada YOK)
    REJECTED   // reddedildi
}
