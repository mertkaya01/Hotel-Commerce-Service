package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.admin.AuditLogPage;
import com.stajproje.hotel.dto.admin.AuditLogResponse;
import com.stajproje.hotel.entity.AuditEventType;
import com.stajproje.hotel.entity.AuditLog;
import com.stajproje.hotel.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/** Yönetici için denetim kayıtlarını filtreli+sayfalı okur (yalnızca okuma). */
@Service
@RequiredArgsConstructor
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogPage list(String type, String actor, int page, int size) {
        AuditEventType eventType = parseType(type);
        String actorFilter = (actor == null || actor.isBlank()) ? null : actor.trim();

        Page<AuditLog> result = auditLogRepository.search(
                eventType, actorFilter, PageRequest.of(page, size));

        return new AuditLogPage(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize());
    }

    private AuditEventType parseType(String type) {
        if (type == null || type.isBlank()) return null;
        try {
            return AuditEventType.valueOf(type.trim());
        } catch (IllegalArgumentException e) {
            return null; // geçersiz tip -> filtre uygulama
        }
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return new AuditLogResponse(
                a.getId(),
                a.getEventType().name(),
                a.getActorEmail(),
                a.getDescription(),
                a.getIpAddress(),
                a.getCreatedAt());
    }
}
