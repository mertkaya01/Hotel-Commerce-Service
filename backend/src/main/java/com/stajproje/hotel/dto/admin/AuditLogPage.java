package com.stajproje.hotel.dto.admin;

import java.util.List;

/** Sayfalanmış denetim kaydı listesi (frontend'de tablo + sayfalama için). */
public record AuditLogPage(
        List<AuditLogResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
