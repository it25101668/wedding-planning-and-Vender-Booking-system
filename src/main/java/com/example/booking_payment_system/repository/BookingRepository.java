package com.example.booking_payment_system.repository;

import com.example.booking_payment_system.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerEmail(String customerEmail);

    List<Booking> findByStatus(String status);

    long countByStatus(String status);

    List<Booking> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCustomerEmailOrderByBookingDateDesc(String customerEmail);

    List<Booking> findAllByOrderByBookingDateDesc();

    @Query("SELECT b FROM Booking b WHERE " +
            "LOWER(b.customerName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.customerEmail) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.venue) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Booking> searchBookings(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT b.eventDate FROM Booking b WHERE b.packageName = :packageName " +
            "AND b.status <> 'Cancelled'")
    List<LocalDate> findBookedDatesByPackageName(@Param("packageName") String packageName);
}
