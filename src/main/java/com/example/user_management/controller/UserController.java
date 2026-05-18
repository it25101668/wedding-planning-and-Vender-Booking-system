package com.example.user_management.controller;

import com.example.user_management.entity.Customer;
import com.example.user_management.entity.User;
import com.example.user_management.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String listUsers(@RequestParam(value = "search", required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("users", userService.searchUsers(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("users", userService.getAllUsers());
        }
        model.addAttribute("totalUsers", userService.getTotalUsers());
        model.addAttribute("activeUsers", userService.getActiveUsers());

        // Also load customers for the dashboard
        model.addAttribute("customers", userService.getAllCustomers());
        model.addAttribute("totalCustomers", userService.getTotalCustomers());

        return "admin/users";
    }

    // =================== DEDICATED CUSTOMER LIST PAGE ===================

    @GetMapping("/customers")
    public String listCustomers(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Customer> customers;
        if (search != null && !search.isEmpty()) {
            customers = userService.getAllCustomers().stream()
                    .filter(c -> (c.getFirstName() + " " + c.getLastName()).toLowerCase().contains(search.toLowerCase())
                            || c.getEmail().toLowerCase().contains(search.toLowerCase())
                            || (c.getPhone() != null && c.getPhone().contains(search)))
                    .collect(Collectors.toList());
            model.addAttribute("search", search);
        } else {
            customers = userService.getAllCustomers();
        }
        model.addAttribute("customers", customers);
        model.addAttribute("totalCustomers", userService.getTotalCustomers());
        return "admin/customers";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("formTitle", "Add New User");
        model.addAttribute("formAction", "/admin/users/save");
        return "admin/user-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.getUserById(id).map(user -> {
            model.addAttribute("user", user);
            model.addAttribute("formTitle", "Edit User");
            model.addAttribute("formAction", "/admin/users/update/" + id);
            return "admin/user-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "User not found!");
            return "redirect:/admin/users";
        });
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes ra) {
        try {
            userService.addUser(user);
            ra.addFlashAttribute("successMsg", "✅ User added successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes ra) {
        try {
            userService.updateUser(id, user);
            ra.addFlashAttribute("successMsg", "✅ User updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("successMsg", "✅ User deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // =================== ADMIN CUSTOMER MANAGEMENT ===================

    @GetMapping("/customers/edit/{id}")
    public String showEditCustomerForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return userService.getCustomerById(id).map(customer -> {
            model.addAttribute("customer", customer);
            model.addAttribute("formTitle", "Edit Customer");
            model.addAttribute("formAction", "/admin/users/customers/update/" + id);
            return "admin/customer-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Customer not found!");
            return "redirect:/admin/users";
        });
    }

    @PostMapping("/customers/update/{id}")
    public String updateCustomer(@PathVariable Long id, @ModelAttribute Customer customer, RedirectAttributes ra) {
        try {
            userService.updateCustomerProfile(id, customer);
            ra.addFlashAttribute("successMsg", "✅ Customer details updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ " + e.getMessage());
        }
        return "redirect:/admin/users/customers";
    }

    @GetMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deleteCustomer(id);
            ra.addFlashAttribute("successMsg", "✅ Customer and their bookings deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", "❌ Failed to delete customer: " + e.getMessage());
        }
        return "redirect:/admin/users/customers";
    }
}
