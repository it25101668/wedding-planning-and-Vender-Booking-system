package com.example.booking_payment_system.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 100)
    private String customerEmail;

    @Column(name = "package_name", length = 100)
    private String packageName;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate;

    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(length = 200)
    private String venue;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "guest_count")
    private Integer guestCount;

    @Column(length = 20)
    private String status = "Pending"; // Pending, Confirmed, Cancelled, Completed

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod = "Cash"; // Cash, Card, Koko

    @Column(name = "payment_status", length = 20)
    private String paymentStatus = "Unpaid"; // Unpaid, Paid, Partial

    @Column(name = "installments_paid")
    private Integer installmentsPaid = 0; // Tracks 0, 1, 2, 3 installments

    @Column(name = "transaction_id", length = 50)
    private String transactionId;

    @PrePersist
    protected void onCreate() {
        bookingDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public LocalDateTime getBookingDate() { return bookingDate; }
    public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }

    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Integer getInstallmentsPaid() {
        return installmentsPaid == null ? 0 : installmentsPaid;
    }
    public void setInstallmentsPaid(Integer installmentsPaid) { this.installmentsPaid = installmentsPaid; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
