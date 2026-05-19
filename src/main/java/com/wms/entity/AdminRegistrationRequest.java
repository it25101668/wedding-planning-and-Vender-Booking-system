package com.wms.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_registration_requests")
public class AdminRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    // PENDING, APPROVED, REJECTED
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_note", length = 300)
    private String reviewNote;

    @PrePersist
    protected void onCreate() {
        requestedAt = LocalDateTime.now();
    }

    // ── Getters & Setters ─────────────────────────────────
    public Long getId()                         { return id; }
    public void setId(Long id)                  { this.id = id; }

    public String getFullName()                 { return fullName; }
    public void setFullName(String fullName)    { this.fullName = fullName; }

    public String getUsername()                 { return username; }
    public void setUsername(String username)    { this.username = username; }

    public String getEmail()                    { return email; }
    public void setEmail(String email)          { this.email = email; }

    public String getPhone()                    { return phone; }
    public void setPhone(String phone)          { this.phone = phone; }

    public String getPassword()                 { return password; }
    public void setPassword(String password)    { this.password = password; }

    public String getStatus()                   { return status; }
    public void setStatus(String status)        { this.status = status; }

    public LocalDateTime getRequestedAt()       { return requestedAt; }
    public void setRequestedAt(LocalDateTime t) { this.requestedAt = t; }

    public LocalDateTime getReviewedAt()        { return reviewedAt; }
    public void setReviewedAt(LocalDateTime t)  { this.reviewedAt = t; }

    public String getReviewNote()               { return reviewNote; }
    public void setReviewNote(String reviewNote){ this.reviewNote = reviewNote; }
}
