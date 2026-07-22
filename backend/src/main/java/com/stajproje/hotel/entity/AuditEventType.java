package com.stajproje.hotel.entity;

/**
 * Denetim (audit) kaydı olay tipleri. Yönetim panelinde "kim ne zaman ne yaptı"
 * izlenebilirliği için kullanılır.
 */
public enum AuditEventType {
    USER_REGISTERED,
    USER_LOGIN_SUCCESS,
    USER_LOGIN_FAILED,
    PASSWORD_CHANGED,
    EMAIL_VERIFIED,
    HOTEL_SUBMITTED,
    HOTEL_APPROVED,
    HOTEL_REJECTED,
    RESERVATION_CREATED,
    RESERVATION_CANCELLED,
    HOST_APPLICATION_SUBMITTED,
    HOST_APPLICATION_APPROVED,
    HOST_APPLICATION_REJECTED
}
