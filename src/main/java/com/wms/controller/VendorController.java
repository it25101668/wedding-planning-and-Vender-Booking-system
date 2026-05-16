package com.wms.controller;

import com.wms.entity.Vendor;
import com.wms.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping
    public String listVendors(@RequestParam(value = "search", required = false) String search,
                              @RequestParam(value = "category", required = false) String category,
                              Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("vendors", vendorService.searchVendors(search));
            model.addAttribute("search", search);
        } else if (category != null && !category.isEmpty()) {
            model.addAttribute("vendors", vendorService.getVendorsByCategory(category));
            model.addAttribute("selectedCategory", category);
        } else {
            model.addAttribute("vendors", vendorService.getAllVendors());
        }
        model.addAttribute("totalVendors", vendorService.getTotalVendors());
        return "admin/vendors";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("vendor", new Vendor());
        model.addAttribute("formTitle", "Register New Vendor");
        model.addAttribute("formAction", "/admin/vendors/save");
        return "admin/vendor-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return vendorService.getVendorById(id).map(vendor -> {
            model.addAttribute("vendor", vendor);
            model.addAttribute("formTitle", "Edit Vendor");
            model.addAttribute("formAction", "/admin/vendors/update/" + id);
            return "admin/vendor-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Vendor not found!");
            return "redirect:/admin/vendors";
        });
    }

    @PostMapping("/save")
    public String saveVendor(@ModelAttribute Vendor vendor, RedirectAttributes ra) {
        try {
            vendorService.addVendor(vendor);
            ra.addFlashAttribute("successMsg", "Vendor registered successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vendors";
    }

    @PostMapping("/update/{id}")
    public String updateVendor(@PathVariable Long id, @ModelAttribute Vendor vendor, RedirectAttributes ra) {
        try {
            vendorService.updateVendor(id, vendor);
            ra.addFlashAttribute("successMsg", "Vendor updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vendors";
    }

    @GetMapping("/delete/{id}")
    public String deleteVendor(@PathVariable Long id, RedirectAttributes ra) {
        try {
            vendorService.deleteVendor(id);
            ra.addFlashAttribute("successMsg", "Vendor deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vendors";
    }
}
