package com.stajproje.hotel.repository;

import com.stajproje.hotel.entity.HostApplication;
import com.stajproje.hotel.entity.HostApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HostApplicationRepository extends JpaRepository<HostApplication, Long> {

    Optional<HostApplication> findByUserId(Long userId);

    List<HostApplication> findByStatusOrderByCreatedAtAsc(HostApplicationStatus status);
}
