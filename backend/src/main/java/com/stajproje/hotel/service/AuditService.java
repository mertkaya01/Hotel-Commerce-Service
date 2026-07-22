package com.stajproje.hotel.service;

import com.stajproje.hotel.entity.AuditEventType;
import com.stajproje.hotel.entity.AuditLog;
import com.stajproje.hotel.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Denetim (audit) kaydı yazar. İki kural:
 *  1) BEST-EFFORT: log yazımı başarısız olsa da ANA AKIŞI ASLA bozmaz (try/catch).
 *  2) BAĞIMSIZ İŞLEM (REQUIRES_NEW): çağıran metodun transaction'ı geri alınsa bile
 *     (örn. başarısız bir işlem) log kaydı KALICI olur — özellikle *_FAILED olaylar için.
 *
 * actor ve IP, istek thread'inden otomatik yakalanır (SecurityContext + HttpServletRequest).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    /** Kimliği doğrulanmış istek için: actor'ı SecurityContext'ten otomatik alır. */
    public void log(AuditEventType type, String description) {
        log(type, currentActor(), description);
    }

    /** actor'ı açıkça verilen olaylar için (kayıt/giriş — henüz auth context yok). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditEventType type, String actorEmail, String description) {
        try {
            auditLogRepository.save(AuditLog.builder()
                    .eventType(type)
                    .actorEmail(actorEmail)
                    .description(description)
                    .ipAddress(currentIp())
                    .build());
        } catch (Exception e) {
            // Log yazımı asla ana akışı bozmaz
            log.warn("Audit log yazilamadi ({}): {}", type, e.getMessage());
        }
    }

    /** Aktif isteğin kimliği doğrulanmış kullanıcısının e-postası (yoksa null). */
    private String currentActor() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
            // sessizce geç
        }
        return null;
    }

    /** İsteğin gerçek IP'si (proxy arkasında X-Forwarded-For'un ilk değeri). */
    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest req = attrs.getRequest();
            String forwarded = req.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return req.getRemoteAddr();
        } catch (Exception ignored) {
            return null;
        }
    }
}
