package com.example.booking_payment_system.controller;

import com.example.booking_payment_system.entity.Booking;
import com.example.booking_payment_system.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller //handle request
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // ── ADMIN ENDPOINTS ──

    @GetMapping("/admin/bookings")
    public String listBookings(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("bookings", bookingService.searchBookings(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("bookings", bookingService.getAllBookings());
        }
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("confirmedBookings", bookingService.getConfirmedBookings());
        return "admin/bookings";
    }

    @GetMapping("/admin/bookings/new")
    public String showAddForm(Model model) {
        model.addAttribute("booking", new Booking());
        model.addAttribute("formTitle", "New Booking");
        model.addAttribute("formAction", "/admin/bookings/save");
        return "admin/booking-form";
    }

    @GetMapping("/admin/bookings/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return bookingService.getBookingById(id).map(booking -> {
            model.addAttribute("booking", booking);
            model.addAttribute("formTitle", "Edit Booking");
            model.addAttribute("formAction", "/admin/bookings/update/" + id);
            return "admin/booking-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Booking not found!");
            return "redirect:/admin/bookings";
        });
    }

    @PostMapping("/admin/bookings/save")
    public String saveBooking(@ModelAttribute Booking booking, RedirectAttributes ra) {
        try {
            bookingService.createBooking(booking);
            ra.addFlashAttribute("successMsg", "Booking created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/admin/bookings/update/{id}")
    public String updateBooking(@PathVariable Long id, @ModelAttribute Booking booking, RedirectAttributes ra) {
        try {
            bookingService.updateBooking(id, booking);
            ra.addFlashAttribute("successMsg", "Booking updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/admin/bookings/delete/{id}")
    public String deleteBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.deleteBooking(id);
            ra.addFlashAttribute("successMsg", "Booking deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @GetMapping("/admin/bookings/history/{email}")
    public String bookingHistory(@PathVariable String email, Model model) {
        model.addAttribute("bookings", bookingService.getBookingHistory(email));
        model.addAttribute("historyMode", true);
        return "admin/bookings";
    }

    // ── CUSTOMER ENDPOINTS ──

    @GetMapping("/customer/book")
    public String showCustomerBookForm(Model model) {
        model.addAttribute("booking", new Booking());
        return "customer/book";
    }

    @GetMapping("/customer/bookings")
    public String listCustomerBookings(@RequestParam String email, Model model) {
        model.addAttribute("bookings", bookingService.getBookingHistory(email));
        return "customer/bookings";
    }

    @PostMapping("/customer/bookings/save")
    public String saveCustomerBooking(@ModelAttribute Booking booking, RedirectAttributes ra) {
        try {
            bookingService.createBooking(booking);
            ra.addFlashAttribute("successMsg", "Your booking request has been submitted! ✨");
            return "redirect:/customer/bookings?email=" + booking.getCustomerEmail();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "Oops! Something went wrong. " + e.getMessage());
            return "redirect:/customer/book";
        }
    }

    // ── UTILITY ENDPOINTS ──

    @GetMapping("/admin/bookings/booked-dates")
    @ResponseBody
    public List<String> getBookedDates(@RequestParam String packageName) {
        return bookingService.getBookedDatesForPackage(packageName);
    }
}
