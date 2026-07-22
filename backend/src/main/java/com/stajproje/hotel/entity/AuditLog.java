package com.stajproje.hotel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Denetim (audit) kaydı: bir olay olduğunda tek satır yazılır. Yalnızca yönetici
 * (SUPER_ADMIN) görüntüleyebilir. Ana iş akışını bozmamak için "best-effort"
 * yazılır (bkz. AuditService).
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_created", columnList = "createdAt"),
        @Index(name = "idx_audit_type", columnList = "eventType")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // enum yerine varchar: ileride yeni olay tipi eklendiğinde kolon kısıtı sorun çıkarmaz
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(40)")
    private AuditEventType eventType;

    // olayı yapan / ilgili kullanıcının e-postası (anonim/başarısız girişte denenen e-posta)
    @Column(length = 255)
    private String actorEmail;

    @Column(length = 500)
    private String description;

    @Column(length = 64)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
