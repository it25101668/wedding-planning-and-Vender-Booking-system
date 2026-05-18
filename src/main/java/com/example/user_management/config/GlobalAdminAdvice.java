package com.example.user_management.config;

import com.example.user_management.entity.User;
import com.example.user_management.service.AdminRegistrationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * 🛠️ Global Controller Advice
 * Ensures sidebar data (like Pending Admin Requests and Super Admin status)
 * is available to all Admin controllers without manually adding it to every method.
 */
@ControllerAdvice(basePackages = "com.example.user_management.controller")
public class GlobalAdminAdvice {

    @Autowired
    private AdminRegistrationService adminRegistrationService;

    @ModelAttribute
    public void addAdminAttributes(HttpSession session, Model model) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser != null) {
            // Ensure isSuperAdmin flag is always in sync with the current user object
            boolean isSuperAdmin = "SUPER_ADMIN".equals(adminUser.getRole());
            session.setAttribute("isSuperAdmin", isSuperAdmin);

            // Add objects to the model for Thymeleaf templates
            model.addAttribute("adminUser", adminUser);
            model.addAttribute("pendingAdminRequests", adminRegistrationService.countPending());
        }
    }
}
