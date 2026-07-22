package com.stajproje.hotel.service;

import com.stajproje.hotel.dto.host.HostApplicationRequest;
import com.stajproje.hotel.dto.host.HostApplicationResponse;
import com.stajproje.hotel.entity.HostApplication;
import com.stajproje.hotel.entity.HostApplicationStatus;
import com.stajproje.hotel.entity.Role;
import com.stajproje.hotel.entity.User;
import com.stajproje.hotel.exception.HostApplicationException;
import com.stajproje.hotel.repository.HostApplicationRepository;
import com.stajproje.hotel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HostApplicationService {

    private final HostApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    /** Kullanıcı ev sahibi başvurusu yapar -> PENDING. */
    @Transactional
    public HostApplicationResponse apply(Long userId, HostApplicationRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanici bulunamadi"));

        if (user.getRole() == Role.ADMIN) {
            throw new HostApplicationException("Zaten ev sahibisiniz");
        }

        HostApplication application = applicationRepository.findByUserId(userId).orElse(null);
        if (application != null && application.getStatus() == HostApplicationStatus.PENDING) {
            throw new HostApplicationException("Başvurunuz zaten değerlendiriliyor");
        }

        if (application == null) {
            application = HostApplication.builder().user(user).build();
        }
        // reddedilmiş bir başvuru varsa yeniden değerlendirmeye alınır
        application.setFirstName(request.getFirstName());
        application.setLastName(request.getLastName());
        application.setBirthDate(request.getBirthDate());
        application.setDescription(request.getDescription());
        application.setStatus(HostApplicationStatus.PENDING);
        application.setReviewedAt(null);

        applicationRepository.save(application);
        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOST_APPLICATION_SUBMITTED,
                user.getEmail(), "Ev sahibi başvurusu yapıldı");
        return toResponse(application);
    }

    /** Kullanıcının kendi başvuru durumu (yoksa null). */
    public HostApplicationResponse getMyApplication(Long userId) {
        return applicationRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    /** Admin: değerlendirilmeyi bekleyen başvurular. */
    public List<HostApplicationResponse> listPending() {
        return applicationRepository.findByStatusOrderByCreatedAtAsc(HostApplicationStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /** Admin: başvuruyu onaylar -> başvuran ADMIN (ev sahibi) olur. */
    @Transactional
    public HostApplicationResponse approve(Long applicationId) {
        HostApplication application = getPendingOrThrow(applicationId);
        application.setStatus(HostApplicationStatus.APPROVED);
        application.setReviewedAt(LocalDateTime.now());

        User applicant = application.getUser();
        applicant.setRole(Role.ADMIN);
        userRepository.save(applicant);
        applicationRepository.save(application);

        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOST_APPLICATION_APPROVED,
                "Ev sahibi başvurusu onaylandı: " + applicant.getEmail());
        return toResponse(application);
    }

    /** Admin: başvuruyu reddeder. */
    @Transactional
    public HostApplicationResponse reject(Long applicationId) {
        HostApplication application = getPendingOrThrow(applicationId);
        application.setStatus(HostApplicationStatus.REJECTED);
        application.setReviewedAt(LocalDateTime.now());
        applicationRepository.save(application);
        auditService.log(com.stajproje.hotel.entity.AuditEventType.HOST_APPLICATION_REJECTED,
                "Ev sahibi başvurusu reddedildi: " + application.getUser().getEmail());
        return toResponse(application);
    }

    private HostApplication getPendingOrThrow(Long applicationId) {
        HostApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new HostApplicationException("Başvuru bulunamadı"));
        if (application.getStatus() != HostApplicationStatus.PENDING) {
            throw new HostApplicationException("Bu başvuru zaten değerlendirilmiş");
        }
        return application;
    }

    private HostApplicationResponse toResponse(HostApplication a) {
        return HostApplicationResponse.builder()
                .id(a.getId())
                .firstName(a.getFirstName())
                .lastName(a.getLastName())
                .email(a.getUser().getEmail())
                .birthDate(a.getBirthDate())
                .description(a.getDescription())
                .status(a.getStatus().name())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
