package com.stajproje.hotel.entity;

public enum HostApplicationStatus {
    PENDING,   // beklemede / değerlendiriliyor
    APPROVED,  // onaylandı -> kullanıcı ev sahibi (ADMIN) oldu
    REJECTED   // reddedildi
}
