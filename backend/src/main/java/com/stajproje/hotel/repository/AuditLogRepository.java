package com.stajproje.hotel.repository;

import com.stajproje.hotel.entity.AuditEventType;
import com.stajproje.hotel.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Olay tipi ve/veya kullanıcıya göre filtreli, tarihe göre tersten sıralı sayfa.
     * Parametreler null ise o filtre uygulanmaz (opsiyonel filtre kalıbı).
     */
    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:type IS NULL OR a.eventType = :type)
              AND (:actor IS NULL OR LOWER(a.actorEmail) LIKE LOWER(CONCAT('%', :actor, '%')))
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(@Param("type") AuditEventType type,
                          @Param("actor") String actor,
                          Pageable pageable);
}
