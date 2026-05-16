package com.wms.repository;

import com.wms.entity.Booking;
import com.wms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomer(Customer customer);

    List<Booking> findByStatus(String status);
    
    List<Booking> findByWeddingPackageId(Long packageId);

    List<Booking> findByEventDateBetween(LocalDate startDate, LocalDate endDate);

    List<Booking> findByCustomerOrderByBookingDateDesc(Customer customer);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.weddingPackage WHERE b.customer = :customer ORDER BY b.bookingDate DESC")
    List<Booking> findByCustomerWithPackage(@Param("customer") Customer customer);

    List<Booking> findAllByOrderByBookingDateDesc();

    /**
     * Fetches all bookings with customer and weddingPackage eagerly loaded.
     * Used by the report dropdown to avoid LazyInitializationException in templates.
     */
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.customer LEFT JOIN FETCH b.weddingPackage ORDER BY b.bookingDate DESC")
    List<Booking> findAllWithDetails();

    /**
     * Fetches a single booking with customer and weddingPackage eagerly loaded.
     */
    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.customer LEFT JOIN FETCH b.weddingPackage WHERE b.id = :id")
    java.util.Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.customer LEFT JOIN FETCH b.weddingPackage WHERE " +
           "LOWER(b.customer.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.customer.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.customer.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.venue) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Booking> searchBookings(@Param("keyword") String keyword);

    /**
     * Returns all active bookings (excluding Cancelled) for a given package on a specific date.
     * Used to check if a package is already booked on that day.
     */
    @Query("SELECT b FROM Booking b WHERE b.weddingPackage.id = :packageId " +
           "AND b.eventDate = :eventDate AND b.status <> 'Cancelled'")
    List<Booking> findActiveBookingsByPackageAndDate(@Param("packageId") Long packageId,
                                                     @Param("eventDate") LocalDate eventDate);

    /**
     * Returns all distinct event dates that are actively booked for a given package.
     * Used to send the full set of blocked dates to the frontend at once.
     */
    @Query("SELECT DISTINCT b.eventDate FROM Booking b WHERE b.weddingPackage.id = :packageId " +
           "AND b.status <> 'Cancelled'")
    List<LocalDate> findBookedDatesByPackageId(@Param("packageId") Long packageId);

    /**
     * Same as above but excludes a specific booking ID (for edit mode —
     * the booking being edited should not block its own current date).
     */
    @Query("SELECT DISTINCT b.eventDate FROM Booking b WHERE b.weddingPackage.id = :packageId " +
           "AND b.status <> 'Cancelled' AND b.id <> :excludeBookingId")
    List<LocalDate> findBookedDatesByPackageIdExcluding(@Param("packageId") Long packageId,
                                                         @Param("excludeBookingId") Long excludeBookingId);
}
