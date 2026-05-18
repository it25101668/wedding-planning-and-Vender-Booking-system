package com.example.profile_management_customer_dashboard.controller;

import com.example.profile_management_customer_dashboard.entity.Customer;
import com.example.profile_management_customer_dashboard.entity.User;
import com.example.profile_management_customer_dashboard.service.ProfileService;
import com.example.profile_management_customer_dashboard.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Controller
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private UserRepository userRepository;

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
            return "redirect:/admin/dashboard";
        }
        return "admin/login";
    }

    @PostMapping("/login")
    public String adminLogin(@RequestParam String username,
                             @RequestParam String password,
                             HttpSession session,
                             RedirectAttributes ra) {
        return userRepository.findByUsername(username).map(user -> {
            boolean isAdmin = "ADMIN".equals(user.getRole()) || "SUPER_ADMIN".equals(user.getRole());
            if (user.getPassword().equals(password) && isAdmin && user.isActive()) {
                session.setAttribute("adminUser", user);
                session.setAttribute("isSuperAdmin", "SUPER_ADMIN".equals(user.getRole()));
                ra.addFlashAttribute("successMsg", "Welcome!");
                return "redirect:/admin/profile";
            } else {
                ra.addFlashAttribute("errorMsg", "Invalid credentials.");
                return "redirect:/login";
            }
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "User not found.");
            return "redirect:/login";
        });
    }

    @GetMapping("/admin/profile")
    public String adminProfile(HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) return "redirect:/login";
        
        model.addAttribute("adminUser", userRepository.findById(adminUser.getId()).orElse(adminUser));
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
                String filename = UUID.randomUUID() + "_" + profilePicFile.getOriginalFilename();
                Path dest = getImgDir().resolve(filename);
                Files.copy(profilePicFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                user.setProfilePic("/pkg-img/" + filename);
            }
            User existing = userRepository.findById(adminUser.getId()).get();
            existing.setFullName(user.getFullName());
            existing.setEmail(user.getEmail());
            existing.setPhone(user.getPhone());
            if (user.getProfilePic() != null) existing.setProfilePic(user.getProfilePic());
            
            User updated = userRepository.save(existing);
            session.setAttribute("adminUser", updated);
            ra.addFlashAttribute("successMsg", "Profile updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/profile";
    }

    // =================== CUSTOMER ===================

    @GetMapping("/customer/login")
    public String customerLoginPage(HttpSession session) {
        if (session.getAttribute("customer") != null) return "redirect:/customer/dashboard";
        return "customer/login";
    }

    @PostMapping("/customer/login")
    public String customerLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                RedirectAttributes ra) {
        return profileService.customerLogin(email, password).map(customer -> {
            session.setAttribute("customer", customer);
            return "redirect:/customer/dashboard";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Invalid credentials.");
            return "redirect:/customer/login";
        });
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        model.addAttribute("customer", customer);
        return "customer/dashboard";
    }

    @GetMapping("/customer/profile")
    public String customerProfile(HttpSession session, Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) return "redirect:/customer/login";
        
        model.addAttribute("customer", profileService.getCustomerById(customer.getId()).orElse(customer));
        return "customer/profile";
    }

    @PostMapping("/customer/profile/update")
    public String updateCustomerProfile(@ModelAttribute Customer customer,
                                        @RequestParam(value = "profilePicFile", required = false) MultipartFile profilePicFile,
                                        HttpSession session,
                                        RedirectAttributes ra) {
        Customer sessionCustomer = (Customer) session.getAttribute("customer");
        if (sessionCustomer == null) return "redirect:/customer/login";

        try {
            if (profilePicFile != null && !profilePicFile.isEmpty()) {
                String filename = UUID.randomUUID() + "_" + profilePicFile.getOriginalFilename();
                Path dest = getImgDir().resolve(filename);
                Files.copy(profilePicFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);
                customer.setProfilePic("/pkg-img/" + filename);
            }
            Customer updated = profileService.updateCustomerProfile(sessionCustomer.getId(), customer);
            session.setAttribute("customer", updated);
            ra.addFlashAttribute("successMsg", "Profile updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/profile";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}