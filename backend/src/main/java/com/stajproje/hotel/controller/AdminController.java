package com.stajproje.hotel.controller;

import com.stajproje.hotel.dto.admin.AuditLogPage;
import com.stajproje.hotel.entity.AuditEventType;
import com.stajproje.hotel.service.AuditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Yönetici (SUPER_ADMIN) uçları. Şimdilik denetim (audit) kayıtları.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AuditQueryService auditQueryService;

    @GetMapping("/audit-logs")
    public AuditLogPage auditLogs(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String actor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return auditQueryService.list(type, actor, page, Math.min(size, 100));
    }

    /** Filtre açılır menüsü için mevcut olay tipleri. */
    @GetMapping("/audit-logs/types")
    public AuditEventType[] eventTypes() {
        return AuditEventType.values();
    }
}
