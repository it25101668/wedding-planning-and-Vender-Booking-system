package com.wms.service;

import com.wms.entity.AdminRegistrationRequest;
import com.wms.entity.User;
import com.wms.repository.AdminRegistrationRequestRepository;
import com.wms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminRegistrationService {

    @Autowired
    private AdminRegistrationRequestRepository requestRepo;

    @Autowired
    private UserRepository userRepository;

    /** Submit a new registration request */
    public AdminRegistrationRequest submitRequest(AdminRegistrationRequest req) {
        // Check against pending/existing requests
        if (requestRepo.existsByUsername(req.getUsername())) {
            throw new RuntimeException("A request with this username already exists.");
        }
        if (requestRepo.existsByEmail(req.getEmail())) {
            throw new RuntimeException("A request with this email already exists.");
        }
        // Check against already-approved users
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("Username is already taken.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email is already registered.");
        }
        req.setStatus("PENDING");
        return requestRepo.save(req);
    }

    /** All requests ordered newest first */
    public List<AdminRegistrationRequest> getAllRequests() {
        return requestRepo.findAllByOrderByRequestedAtDesc();
    }

    /** Only PENDING requests */
    public List<AdminRegistrationRequest> getPendingRequests() {
        return requestRepo.findByStatus("PENDING");
    }

    /** Count of pending requests (for badge on sidebar) */
    public long countPending() {
        return requestRepo.countByStatus("PENDING");
    }

    /** Approve: create User with ADMIN role, mark request APPROVED */
    public void approveRequest(Long id) {
        AdminRegistrationRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found."));

        if ("APPROVED".equals(req.getStatus())) {
            throw new RuntimeException("This request has already been approved.");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("A user with username '" + req.getUsername() + "' already exists.");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("A user with email '" + req.getEmail() + "' already exists.");
        }

        // Create the admin user
        User user = new User();
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setFullName(req.getFullName());
        user.setPhone(req.getPhone());
        user.setPassword(req.getPassword());
        user.setRole("ADMIN");
        user.setActive(true);
        userRepository.save(user);

        // Mark request approved
        req.setStatus("APPROVED");
        req.setReviewedAt(LocalDateTime.now());
        requestRepo.save(req);
    }

    /** Reject: mark request REJECTED with optional note */
    public void rejectRequest(Long id, String note) {
        AdminRegistrationRequest req = requestRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found."));
        req.setStatus("REJECTED");
        req.setReviewedAt(LocalDateTime.now());
        req.setReviewNote(note);
        requestRepo.save(req);
    }

    public Optional<AdminRegistrationRequest> getById(Long id) {
        return requestRepo.findById(id);
    }
}
