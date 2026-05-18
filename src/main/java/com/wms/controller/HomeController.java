package com.wms.controller;

import com.wms.service.BookingService;
import com.wms.service.PackageService;
import com.wms.service.VendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private VendorService vendorService;

    @Autowired
    private PackageService packageService;

    @Autowired
    private BookingService bookingService;

    /** Public landing / index page */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("totalVendors", vendorService.getTotalVendors());
        model.addAttribute("totalPackages", packageService.getTotalPackages());
        model.addAttribute("totalBookings", bookingService.getTotalBookings());
        model.addAttribute("packages", packageService.getAllPackages());
        return "index";
    }
}
