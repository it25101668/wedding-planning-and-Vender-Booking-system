package com.wms.controller;

import com.wms.entity.AdminRegistrationRequest;
import com.wms.entity.User;
import com.wms.repository.UserRepository;
import com.wms.service.AdminRegistrationService;
import com.wms.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.stream.Collectors;

@Controller
public class AdminRegistrationController {

    @Autowired
    private AdminRegistrationService registrationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    // ─── Helper: is the current session a Super Admin? ────────
    private boolean isSuperAdmin(HttpSession session) {
        Boolean flag = (Boolean) session.getAttribute("isSuperAdmin");
        if (flag != null && flag) return true;
        User user = (User) session.getAttribute("adminUser");
        return user != null && "SUPER_ADMIN".equals(user.getRole());
    }

    // ─── Public: show registration form ───────────────────────
    @GetMapping("/admin/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("request", new AdminRegistrationRequest());
        return "admin/register";
    }

    // ─── Public: submit registration request ──────────────────
    @PostMapping("/admin/register")
    public String submitRegister(@ModelAttribute("request") AdminRegistrationRequest req,
                                  RedirectAttributes ra) {
        try {
            registrationService.submitRequest(req);
            ra.addFlashAttribute("successMsg",
                "✅ Registration request submitted! Please wait for Super Admin approval.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/register";
    }

    // ─── SUPER_ADMIN: view all requests + current admins ──────
    @GetMapping("/admin/registration-requests")
    public String listRequests(HttpSession session, Model model, RedirectAttributes ra) {
        User adminUser = (User) session.getAttribute("adminUser");
        if (adminUser == null) return "redirect:/login";
        if (!isSuperAdmin(session)) {
            ra.addFlashAttribute("errorMsg", "⛔ Access denied. Only Super Admin can manage registration requests.");
            return "redirect:/admin/dashboard";
        }
        model.addAttribute("adminUser", adminUser);
        model.addAttribute("requests", registrationService.getAllRequests());
        model.addAttribute("pendingCount", registrationService.countPending());
        // Current admins (ADMIN role only — excludes SUPER_ADMIN)
        model.addAttribute("currentAdmins",
            userService.getAllUsers().stream()
                .filter(u -> "ADMIN".equals(u.getRole()))
                .collect(Collectors.toList()));
        return "admin/registration-requests";
    }

    // ─── SUPER_ADMIN: approve request ─────────────────────────
    @PostMapping("/admin/registration-requests/approve/{id}")
    public String approveRequest(@PathVariable Long id,
                                  HttpSession session,
                                  RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) return "redirect:/login";
        if (!isSuperAdmin(session)) {
            ra.addFlashAttribute("errorMsg", "⛔ Only Super Admin can approve requests.");
            return "redirect:/admin/dashboard";
        }
        try {
            registrationService.approveRequest(id);
            ra.addFlashAttribute("successMsg", "✅ Request approved! New admin account created.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/registration-requests";
    }

    // ─── SUPER_ADMIN: reject request ──────────────────────────
    @PostMapping("/admin/registration-requests/reject/{id}")
    public String rejectRequest(@PathVariable Long id,
                                 @RequestParam(value = "note", required = false) String note,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) return "redirect:/login";
        if (!isSuperAdmin(session)) {
            ra.addFlashAttribute("errorMsg", "⛔ Only Super Admin can reject requests.");
            return "redirect:/admin/dashboard";
        }
        try {
            registrationService.rejectRequest(id, note);
            ra.addFlashAttribute("successMsg", "🚫 Request rejected.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/registration-requests";
    }

    // ─── SUPER_ADMIN: remove an existing admin ─────────────────
    @PostMapping("/admin/registration-requests/remove-admin/{id}")
    public String removeAdmin(@PathVariable Long id,
                              HttpSession session,
                              RedirectAttributes ra) {
        if (session.getAttribute("adminUser") == null) return "redirect:/login";
        if (!isSuperAdmin(session)) {
            ra.addFlashAttribute("errorMsg", "⛔ Only Super Admin can remove admins.");
            return "redirect:/admin/dashboard";
        }
        try {
            User target = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found."));

            if ("SUPER_ADMIN".equals(target.getRole())) {
                ra.addFlashAttribute("errorMsg", "⛔ Cannot remove the Super Admin account.");
                return "redirect:/admin/registration-requests";
            }

            // Directly update via repository to avoid updateUser() conflict checks
            target.setRole("USER");
            target.setActive(false);
            userRepository.save(target);

            ra.addFlashAttribute("successMsg",
                "✅ Admin '" + target.getUsername() + "' has been removed.");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/registration-requests";
    }
}
