package com.stajproje.hotel.entity;

public enum Role {
    USER,         // normal kullanıcı
    ADMIN,        // ev sahibi (property owner) — kendi otellerini yönetir
    SUPER_ADMIN   // platform yöneticisi — ev sahibi başvurularını değerlendirir
}
