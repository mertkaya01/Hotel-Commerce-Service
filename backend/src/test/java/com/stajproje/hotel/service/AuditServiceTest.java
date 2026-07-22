package com.stajproje.hotel.service;

import com.stajproje.hotel.entity.AuditEventType;
import com.stajproje.hotel.entity.AuditLog;
import com.stajproje.hotel.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository auditLogRepository;

    @InjectMocks private AuditService auditService;

    @Test
    void log_shouldPersistEventWithActorAndDescription() {
        auditService.log(AuditEventType.USER_REGISTERED, "a@b.com", "Yeni üye kaydı");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLog saved = captor.getValue();
        assertThat(saved.getEventType()).isEqualTo(AuditEventType.USER_REGISTERED);
        assertThat(saved.getActorEmail()).isEqualTo("a@b.com");
        assertThat(saved.getDescription()).isEqualTo("Yeni üye kaydı");
    }

    @Test
    void log_shouldNeverThrow_whenPersistenceFails() {
        // BEST-EFFORT: log yazimi patlasa bile ANA AKIS bozulmamali
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("db down"));

        assertThatCode(() ->
                auditService.log(AuditEventType.USER_LOGIN_FAILED, "x@y.com", "Başarısız giriş"))
                .doesNotThrowAnyException();
    }
}
