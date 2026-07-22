package com.stajproje.hotel.dto.admin;

import java.time.LocalDateTime;

/** Yönetim panelinde gösterilen tek denetim kaydı. */
public record AuditLogResponse(
        Long id,
        String eventType,
        String actorEmail,
        String description,
        String ipAddress,
        LocalDateTime createdAt
) {
}
