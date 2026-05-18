package com.example.booking_payment_system.service;

import com.example.booking_payment_system.entity.Booking;
import com.example.booking_payment_system.entity.Payment;
import com.example.booking_payment_system.repository.BookingRepository;
import com.example.booking_payment_system.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAllByOrderByBookingDateDesc();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public Booking createBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    public Booking updateBooking(Long id, Booking updatedBooking) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
        
        existing.setCustomerName(updatedBooking.getCustomerName());
        existing.setCustomerEmail(updatedBooking.getCustomerEmail());
        existing.setPackageName(updatedBooking.getPackageName());
        existing.setEventDate(updatedBooking.getEventDate());
        existing.setVenue(updatedBooking.getVenue());
        existing.setGuestCount(updatedBooking.getGuestCount());
        existing.setTotalAmount(updatedBooking.getTotalAmount());
        existing.setStatus(updatedBooking.getStatus());
        existing.setNotes(updatedBooking.getNotes());
        existing.setPaymentMethod(updatedBooking.getPaymentMethod());
        existing.setPaymentStatus(updatedBooking.getPaymentStatus());
        existing.setTransactionId(updatedBooking.getTransactionId());

        return bookingRepository.save(existing);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new RuntimeException("Booking not found: " + id);
        }
        bookingRepository.deleteById(id);
    }

    public List<Booking> searchBookings(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBookings();
        }
        return bookingRepository.searchBookings(keyword.trim());
    }

    public List<Booking> getBookingHistory(String customerEmail) {
        return bookingRepository.findByCustomerEmailOrderByBookingDateDesc(customerEmail);
    }

    public long getTotalBookings() {
        return bookingRepository.count();
    }

    public long getConfirmedBookings() {
        return bookingRepository.countByStatus("Confirmed");
    }

    @Transactional
    public void recordKokoPayment(Long bookingId, int installmentNum) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found."));

        int currentPaid = booking.getInstallmentsPaid();

        if (installmentNum != (currentPaid + 1)) {
            throw new RuntimeException("Installments must be paid in order. Current step: " + (currentPaid + 1));
        }

        // Apply 10% Installment Fee on the first installment
        if (installmentNum == 1 && (booking.getInstallmentsPaid() == 0)) {
            BigDecimal fee = booking.getTotalAmount().multiply(new BigDecimal("0.10"));
            booking.setTotalAmount(booking.getTotalAmount().add(fee));
        }

        BigDecimal totalAmount = booking.getTotalAmount();
        BigDecimal standardInstallment = totalAmount
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);

        BigDecimal amountToPay;
        if (installmentNum == 3) {
            BigDecimal paidSoFar = standardInstallment.multiply(new BigDecimal("2"));
            amountToPay = totalAmount.subtract(paidSoFar);
            booking.setPaymentStatus("Paid");
            booking.setStatus("Confirmed");
        } else {
            amountToPay = standardInstallment;
            booking.setPaymentStatus("Partial");
        }

        booking.setInstallmentsPaid(installmentNum);

        Payment payment = new Payment(
                booking,
                amountToPay,
                "Koko",
                "KOKO-FIN-" + installmentNum + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase()
        );

        paymentRepository.save(payment);
        bookingRepository.save(booking);
    }

    public List<String> getBookedDatesForPackage(String packageName) {
        List<LocalDate> dates = bookingRepository.findBookedDatesByPackageName(packageName);
        return dates.stream()
                .map(LocalDate::toString)
                .collect(Collectors.toList());
    }
}
