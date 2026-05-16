package com.wms.controller;

import com.wms.entity.Customer;
import com.wms.entity.User;
import com.wms.service.ProfileService;
import com.wms.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserService userService;

    @Autowired
    private com.wms.service.PackageService packageService;

    @Autowired
    private com.wms.service.BookingService bookingService;

    @Autowired
    private com.wms.service.VendorService vendorService;

    @Autowired
    private com.wms.service.ReportService reportService;

    @Autowired
    private com.wms.service.AdminRegistrationService adminRegistrationService;

    @Autowired
    private com.wms.service.PdfService pdfService;

    private Path getImgDir() throws IOException {
        Path dir = Paths.get(System.getProperty("user.dir")).resolve("Img");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    // =================== ADMIN ===================

    @GetMapping("/login")
    public String adminLoginPage(HttpSession session, Model model) {
        if (session.getAttribute("adminUser") != null) {
            model.addAttribute("alreadyLoggedIn", true);
            model.addAttribute("adminUser", session.getAttribute("adminUser"));
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes ra) {
        // Prevent double login
        if (session.getAttribute("adminUser") != null) {
            return "redirect:/admin/dashboard";
        }

        return userService.findByUsername(username).map(user -> {
            boolean isAdmin = "ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole());
            if (user.getPassword().equals(password) && isAdmin && user.isActive()) {
                session.setAttribute("adminUser", user);
                session.setAttribute("isSuperAdmin", "SUPER_ADMIN".equals(user.getRole()));
                ra.addFlashAttribute("successMsg", "Welcome " + ("SUPER_ADMIN".equals(user.getRole()) ? "Super Admin" : "Admin") + "! 🎉");
                return "redirect:/admin/dashboard";
            } else {
                ra.addFlashAttribute("errorMsg", "Invalid credentials or insufficient permissions.");
                return "redirect:/login";
            }
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "User not found.");
            return "redirect:/login";
        });
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("adminUser", adminUser);

        // Dashboard stats
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("totalVendors", vendorService.getTotalVendors());
        model.addAttribute("totalPackages", packageService.getTotalPackages());
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("confirmedBookings", bookingService.getConfirmedBookings());
        model.addAttribute("totalReports", reportService.getTotalReports());
        model.addAttribute("totalCustomers", profileService.getTotalCustomers());
        model.addAttribute("pendingAdminRequests", adminRegistrationService.countPending());
        model.addAttribute("customers", profileService.getRecentCustomers());

        return "admin/dashboard";
    }

    @GetMapping("/admin/profile")
    public String adminProfile(HttpSession session, Model model) {
        User sessionUser = (User) session.getAttribute("adminUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        
        // Re-fetch from DB to ensure fresh data
        User adminUser = userService.getUserById(sessionUser.getId())
                .orElse(sessionUser);
        
        // Sync session flags
        session.setAttribute("adminUser", adminUser);
        session.setAttribute("isSuperAdmin", "SUPER_ADMIN".equals(adminUser.getRole()));
        
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("pendingAdminRequests", adminRegistrationService.countPending());
        
        return "admin/profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(@ModelAttribute("adminUser") User user,
                                     @RequestParam(value = "profilePicFile", required = false) MultipartFile profilePicFile,
                                     HttpSession session,
                                     RedirectAttributes ra) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) return "redirect:/login";
        try {
            if (profilePicFile != null && !profilePicFile.isEmpty()) {
                String originalName = profilePicFile.getOriginalFilename();
                String ext = (originalName != null && originalName.contains(".")) ? originalName.substring(originalName.lastIndexOf('.')) : "";
                String filename = UUID.randomUUID() + ext;
                Path dest = getImgDir().resolve(filename);
                Files.copy(profilePicFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePic("/pkg-img/" + filename);
            }

            // PRESERVE ROLE: Ensure the role is not accidentally demoted during profile update
            user.setRole(adminUser.getRole());

            User updated = userService.updateUser(adminUser.getId(), user);
            session.setAttribute("adminUser", updated);
            ra.addFlashAttribute("successMsg", "✅ Profile updated successfully!");
        } catch (RuntimeException | IOException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    // =================== CUSTOMER REGISTRATION & LOGIN ===================

    @GetMapping("/customer/login")
    public String customerLoginPage(HttpSession session, Model model) {
        if (session.getAttribute("customer") != null) {
            model.addAttribute("alreadyLoggedIn", true);
            model.addAttribute("customer", session.getAttribute("customer"));
        }
        return "customer/login";
    }

    @PostMapping("/customer/login")
    public String customerLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes ra) {
        // Prevent double login
        if (session.getAttribute("customer") != null) {
            return "redirect:/customer/dashboard";
        }

        // Validate inputs
        if (email == null || email.trim().isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Email is required");
            return "redirect:/customer/login";
        }
        if (password == null || password.trim().isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Password is required");
            return "redirect:/customer/login";
        }

        return profileService.customerLogin(email.trim(), password).map(customer -> {
            session.setAttribute("customer", customer);
            ra.addFlashAttribute("successMsg", "Welcome back, " + customer.getFirstName() + "! 💍");
            return "redirect:/customer/dashboard";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "❌ Invalid email or password. Please try again.");
            return "redirect:/customer/login";
        });
    }

    @GetMapping("/customer/register")
    public String customerRegisterPage(HttpSession session, Model model) {
        // If already logged in, redirect to dashboard
        if (session.getAttribute("customer") != null) {
            return "redirect:/customer/dashboard";
        }
        model.addAttribute("customer", new Customer());
        return "customer/register";
    }

    @PostMapping("/customer/register")
    public String customerRegister(@ModelAttribute Customer customer,
                                   RedirectAttributes ra) {
        try {
            // Validate required fields
            if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
                throw new RuntimeException("First name is required");
            }
            if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
                throw new RuntimeException("Last name is required");
            }
            if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
                throw new RuntimeException("Email is required");
            }
            if (customer.getPassword() == null || customer.getPassword().length() < 6) {
                throw new RuntimeException("Password must be at least 6 characters");
            }

            // Trim whitespace
            customer.setFirstName(customer.getFirstName().trim());
            customer.setLastName(customer.getLastName().trim());
            customer.setEmail(customer.getEmail().trim().toLowerCase());

            // Add customer to database
            profileService.addCustomer(customer);

            ra.addFlashAttribute("successMsg", "✅ Registration successful! Please login with your email and password.");
            return "redirect:/customer/login";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
            ra.addFlashAttribute("customer", customer);
            return "redirect:/customer/register";
        }
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("bookings", profileService.getCustomerBookings(customer.getId()));
        model.addAttribute("packages", packageService.getAvailablePackages());
        return "customer/dashboard";
    }

    @GetMapping("/customer/profile")
    public String customerProfile(HttpSession session, Model model) {
        Customer sessionCustomer = (Customer) session.getAttribute("customer");
        if (sessionCustomer == null) {
            return "redirect:/customer/login";
        }
        
        // Re-fetch from DB to ensure fresh data after save/refresh
        Customer customer = profileService.getCustomerById(sessionCustomer.getId())
                .orElse(sessionCustomer);
        session.setAttribute("customer", customer);
        
        model.addAttribute("customer", customer);
        model.addAttribute("bookings", profileService.getCustomerBookings(customer.getId()));
        model.addAttribute("formTitle", "Update My Profile");
        model.addAttribute("formAction", "/customer/profile/update");
        return "customer/profile";
    }

    @PostMapping("/customer/profile/update")
    public String updateCustomerProfile(@ModelAttribute Customer customer,
                                        @RequestParam(value = "profilePicFile", required = false) MultipartFile profilePicFile,
                                        HttpSession session,
                                        RedirectAttributes ra) {
        Customer sessionCustomer = (Customer) session.getAttribute("customer");
        if (sessionCustomer == null) {
            return "redirect:/customer/login";
        }

        try {
            // Validate inputs
            if (customer.getFirstName() == null || customer.getFirstName().trim().isEmpty()) {
                throw new RuntimeException("First name is required");
            }
            if (customer.getLastName() == null || customer.getLastName().trim().isEmpty()) {
                throw new RuntimeException("Last name is required");
            }

            if (profilePicFile != null && !profilePicFile.isEmpty()) {
                String originalName = profilePicFile.getOriginalFilename();
                String ext = (originalName != null && originalName.contains(".")) ? originalName.substring(originalName.lastIndexOf('.')) : "";
                String filename = UUID.randomUUID() + ext;
                Path dest = getImgDir().resolve(filename);
                Files.copy(profilePicFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                customer.setProfilePic("/pkg-img/" + filename);
            }

            Customer updated = profileService.updateCustomerProfile(sessionCustomer.getId(), customer);
            session.setAttribute("customer", updated);
            ra.addFlashAttribute("successMsg", "✅ Profile updated successfully!");
        } catch (RuntimeException | IOException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addFlashAttribute("successMsg", "Logged out successfully! 👋");
        return "redirect:/";
    }

    @GetMapping("/customer/bookings")
    public String customerBookings(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        model.addAttribute("bookings", profileService.getCustomerBookings(customer.getId()));
        model.addAttribute("customer", customer);
        return "customer/bookings";
    }

    @GetMapping("/customer/bookings/delete/{id}")
    public String deleteCustomerBooking(@PathVariable Long id,
                                        HttpSession session,
                                        RedirectAttributes ra) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        try {
            bookingService.deleteCustomerBooking(id, customer.getId());
            ra.addFlashAttribute("successMsg", "✅ Booking removed from your history.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/customer/bookings";
    }

    @GetMapping("/customer/payment/download-slip")
    public void downloadPaymentSlip(@RequestParam String bank,
                                    @RequestParam String txnId,
                                    @RequestParam String amount,
                                    HttpSession session,
                                    HttpServletResponse response) throws java.io.IOException {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            response.sendRedirect("/customer/login");
            return;
        }

        response.setContentType("application/pdf");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=PaymentSlip_" + txnId + ".pdf";
        response.setHeader(headerKey, headerValue);

        pdfService.generatePaymentSlipPdf(bank, txnId, amount, customer.getFullName(), customer.getEmail(), response);
    }

    /**
     * REST endpoint for the customer booking form.
     * Returns JSON list of already-booked dates for a given package.
     * GET /customer/booked-dates?packageId=X
     */
    @GetMapping("/customer/booked-dates")
    @ResponseBody
    public java.util.List<String> customerBookedDates(@RequestParam Long packageId) {
        return bookingService.getBookedDatesForPackage(packageId, null);
    }

    // =================== CUSTOMER BOOKING FLOW ===================

    @GetMapping("/customer/packages")
    public String customerPackages(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("packages", packageService.getAvailablePackages());
        return "customer/packages";
    }

    @GetMapping("/customer/packages/{id}")
    public String viewPackageDetails(@PathVariable Long id,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes ra) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        return packageService.getPackageById(id).map(pkg -> {
            model.addAttribute("pkg", pkg);
            model.addAttribute("customer", customer);
            return "customer/customer-package";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found.");
            return "redirect:/customer/packages";
        });
    }

    @GetMapping("/customer/book/{packageId}")
    public String viewBookingForm(@PathVariable Long packageId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        return packageService.getPackageById(packageId).map(pkg -> {
            model.addAttribute("weddingPackage", pkg);
            model.addAttribute("customer", customer);
            return "customer/book";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found.");
            return "redirect:/customer/packages";
        });
    }

    @PostMapping("/customer/book/process")
    public String processBooking(@RequestParam Long packageId,
                                 @RequestParam String eventDate,
                                 @RequestParam(defaultValue = "Cash") String paymentMethod,
                                 @RequestParam(required = false) String transactionId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/customer/login";
        }

        try {
            // Load the package to lock its values
            com.wms.entity.WeddingPackage pkg = packageService.getPackageById(packageId)
                .orElseThrow(() -> new RuntimeException("Package not found."));

            // Build booking with ONLY the event date from the customer
            // Everything else comes locked from the package
            com.wms.entity.Booking booking = new com.wms.entity.Booking();
            booking.setEventDate(java.time.LocalDate.parse(eventDate));
            booking.setVenue(pkg.getLocation());           // locked from package
            
            // Apply 10% fee for Koko
            java.math.BigDecimal finalAmount = pkg.getPrice();
            if ("Koko".equalsIgnoreCase(paymentMethod)) {
                finalAmount = finalAmount.multiply(new java.math.BigDecimal("1.10"));
            }
            booking.setTotalAmount(finalAmount);
            
            booking.setPaymentMethod(paymentMethod);       // Store chosen method
            booking.setStatus("Pending");

            // Set payment status and auto-confirm if verified (for Card/Koko mock)
            if (transactionId != null && !transactionId.isEmpty()) {
                booking.setPaymentStatus("Paid");
                booking.setTransactionId(transactionId);
                booking.setStatus("Confirmed"); // Auto-fill Booking Status as Confirmed if Paid
            } else {
                booking.setPaymentStatus("Unpaid");
                booking.setStatus("Pending");   // Keep as Pending if not paid immediately
            }

            com.wms.entity.Booking savedBooking = bookingService.createBooking(booking, customer.getId(), packageId);

            ra.addFlashAttribute("successMsg",
                "✅ Your booking request has been submitted successfully! We'll contact you soon.");
            return "redirect:/customer/dashboard";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ Failed to submit booking: " + e.getMessage());
            return "redirect:/customer/book/" + packageId;
        }
    }

    /**
     * AJAX endpoint — processes and SAVES Koko installment payment from dashboard
     */
    @PostMapping("/customer/bookings/pay-koko/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<java.util.Map<String, Object>> payKokoInstallment(
                                                           @PathVariable Long id, 
                                                           @RequestParam int installmentNum,
                                                           HttpSession session) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        Customer customer = (Customer) session.getAttribute("customer");
        
        if (customer == null) {
            response.put("success", false);
            response.put("message", "Session expired. Please login again.");
            return org.springframework.http.ResponseEntity.status(403).body(response);
        }

        try {
            com.wms.entity.Booking booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found."));
            
            if (!booking.getCustomer().getId().equals(customer.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access.");
                return org.springframework.http.ResponseEntity.status(403).body(response);
            }

            // Record payment
            bookingService.recordKokoPayment(id, installmentNum);

            response.put("success", true);
            response.put("message", "Installment " + installmentNum + " recorded!");
            return org.springframework.http.ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return org.springframework.http.ResponseEntity.status(400).body(response);
        }
    }
}