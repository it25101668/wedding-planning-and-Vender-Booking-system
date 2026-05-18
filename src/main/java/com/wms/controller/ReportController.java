package com.wms.controller;

import com.wms.entity.Report;
import com.wms.entity.User;
import com.wms.service.BookingService;
import com.wms.service.ReportService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public String listReports(@RequestParam(required = false) String search,
                              @RequestParam(required = false) String type,
                              HttpSession session,
                              Model model) {
        // Check if admin is logged in
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        List<Report> reports;
        if (search != null && !search.isBlank()) {
            reports = reportService.searchReports(search);
        } else if (type != null && !type.isBlank()) {
            reports = reportService.getReportsByType(type);
        } else {
            reports = reportService.getAllReports();
        }

        model.addAttribute("reports", reports);
        model.addAttribute("totalReports", reportService.getTotalReports());
        model.addAttribute("search", search);
        model.addAttribute("type", type);
        model.addAttribute("newReport", new Report());
        model.addAttribute("allBookings", bookingService.getAllBookingsWithDetails());
        return "admin/reports";
    }

    @PostMapping("/save")
    public String createReport(@ModelAttribute Report report,
                               HttpSession session,
                               RedirectAttributes ra) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/login";
        }

        try {
            // Set the admin who generated the report
            report.setGeneratedBy(adminUser.getFullName() != null ?
                    adminUser.getFullName() : adminUser.getUsername());

            reportService.addReport(report);
            ra.addFlashAttribute("successMsg", "✅ Report created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "⚠️ " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @GetMapping("/view/{id}")
    public String viewReport(@PathVariable Long id,
                             HttpSession session,
                             Model model,
                             RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        return reportService.getReportById(id).map(report -> {
            model.addAttribute("report", report);
            
            // If it's a Payment/Booking report, try to parse it for the modern UI
            boolean isModern = report.getType() != null && (report.getType().equals("Booking") || report.getTitle().contains("Payment"));
            Map<String, String> details = new HashMap<>();
            
            if (isModern && report.getContent() != null) {
                String[] lines = report.getContent().split("\n");
                for (String line : lines) {
                    if (line.contains(":")) {
                        String[] parts = line.split(":", 2);
                        String key = parts[0].trim().replace(" ", "_").toLowerCase();
                        details.put(key, parts[1].trim());
                    }
                }
                model.addAttribute("isModernReport", true);
            } else {
                model.addAttribute("isModernReport", false);
            }
            
            model.addAttribute("details", details);
            return "admin/report-view";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "⚠️ Report not found.");
            return "redirect:/admin/reports";
        });
    }

    @GetMapping("/delete/{id}")
    public String deleteReport(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) {
            return "redirect:/login";
        }

        try {
            reportService.deleteReport(id);
            ra.addFlashAttribute("successMsg", "✅ Report deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "⚠️ " + e.getMessage());
        }
        return "redirect:/admin/reports";
    }

    @Autowired
    private com.wms.service.PdfService pdfService;

    @GetMapping("/download/{id}")
    public void downloadReportPdf(@PathVariable Long id,
                                  HttpServletResponse response,
                                  HttpSession session) throws java.io.IOException {
        if (session.getAttribute("adminUser") == null) {
            response.sendRedirect("/login");
            return;
        }

        reportService.getReportById(id).ifPresent(report -> {
            try {
                response.setContentType("application/pdf");
                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=Report_" + id + ".pdf";
                response.setHeader(headerKey, headerValue);

                pdfService.generateReportPdf(report, response);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * REST endpoint: returns JSON details of a booking for auto-fill in the report modal.
     * GET /admin/reports/booking-details?bookingId=X
     */
    @GetMapping("/booking-details")
    @ResponseBody
    public Map<String, String> getBookingDetails(@RequestParam Long bookingId,
                                                  HttpSession session) {
        Map<String, String> result = new HashMap<>();
        if (session.getAttribute("adminUser") == null) {
            return result;
        }
        bookingService.getBookingByIdWithDetails(bookingId).ifPresent(b -> {
            String customerName = b.getCustomer() != null ? b.getCustomer().getFullName() : "Unknown";
            String customerEmail = b.getCustomer() != null ? b.getCustomer().getEmail() : "";
            String packageName = b.getWeddingPackage() != null ? b.getWeddingPackage().getName() : "N/A";
            String packagePrice = b.getWeddingPackage() != null ? b.getWeddingPackage().getPrice().toString() : "N/A";
            String eventDate = b.getEventDate() != null ? b.getEventDate().toString() : "N/A";
            String venue = b.getVenue() != null ? b.getVenue() : "N/A";
            String paymentMethod = b.getPaymentMethod() != null ? b.getPaymentMethod() : "Cash";
            String status = b.getStatus() != null ? b.getStatus() : "Pending";
            String bookedOn = b.getBookingDate() != null ? b.getBookingDate().toLocalDate().toString() : "N/A";

            result.put("title", "Payment Success Report – " + packageName + " (" + customerName + ")");
            result.put("content",
                "--------------------------------------------------\n" +
                "           OFFICIAL PAYMENT & BOOKING REPORT      \n" +
                "--------------------------------------------------\n" +
                "PAYMENT STATUS  : " + ("Paid".equals(b.getPaymentStatus()) ? "✅ PAYMENT SUCCESSFUL" : "❌ PENDING / UNPAID") + "\n" +
                "Transaction ID  : " + (b.getTransactionId() != null ? b.getTransactionId() : "N/A") + "\n" +
                "Payment Method  : " + paymentMethod + "\n" +
                "--------------------------------------------------\n\n" +
                
                "CUSTOMER DETAILS\n" +
                "Name            : " + customerName + "\n" +
                "Email           : " + customerEmail + "\n\n" +
                
                "PACKAGE DETAILS\n" +
                "Package Name    : " + packageName + "\n" +
                "Package Price   : LKR " + packagePrice + "\n\n" +
                
                "EVENT INFORMATION\n" +
                "Event Date      : " + eventDate + "\n" +
                "Venue           : " + venue + "\n\n" +
                
                "FINAL SUMMARY\n" +
                "Booking Status  : " + status + "\n" +
                "Booked On       : " + bookedOn + "\n" +
                "--------------------------------------------------\n" +
                "CONFIRMED BY    : System Auto-Generation"
            );
        });
        return result;
    }
}