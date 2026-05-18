package com.example.user_management.repository;

import com.example.user_management.entity.AdminRegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AdminRegistrationRequestRepository
        extends JpaRepository<AdminRegistrationRequest, Long> {

    List<AdminRegistrationRequest> findByStatus(String status);

    List<AdminRegistrationRequest> findAllByOrderByRequestedAtDesc();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByStatus(String status);
}
