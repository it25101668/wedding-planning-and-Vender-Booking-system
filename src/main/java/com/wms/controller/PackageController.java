package com.wms.controller;

import com.wms.entity.WeddingPackage;
import com.wms.service.PackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/admin/packages")
public class PackageController {

    @Autowired
    private PackageService packageService;

    /**
     * Returns the absolute path to WMS/Img – the user's dedicated image folder.
     * Resolves "Img" relative to the JVM working directory (the WMS project root).
     */
    private Path getImgDir() throws IOException {
        Path dir = Paths.get(System.getProperty("user.dir")).resolve("Img");
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    // ── LIST ──────────────────────────────────────────────────────────────────
    @GetMapping
    public String listPackages(@RequestParam(value = "search", required = false) String search,
                               Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("packages", packageService.searchPackages(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("packages", packageService.getAllPackages());
        }
        model.addAttribute("totalPackages", packageService.getTotalPackages());
        return "admin/packages";
    }

    // ── ADD FORM ──────────────────────────────────────────────────────────────
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("pkg", new WeddingPackage());
        model.addAttribute("formTitle", "Add New Package");
        model.addAttribute("formAction", "/admin/packages/save");
        return "admin/package-form";
    }

    // ── EDIT FORM ─────────────────────────────────────────────────────────────
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return packageService.getPackageById(id).map(pkg -> {
            model.addAttribute("pkg", pkg);
            model.addAttribute("formTitle", "Edit Package");
            model.addAttribute("formAction", "/admin/packages/update/" + id);
            return "admin/package-form";
        }).orElseGet(() -> {
            ra.addFlashAttribute("errorMsg", "Package not found!");
            return "redirect:/admin/packages";
        });
    }

    // ── SAVE (new) ────────────────────────────────────────────────────────────
    @PostMapping("/save")
    public String savePackage(@ModelAttribute WeddingPackage weddingPackage,
                              RedirectAttributes ra) {
        try {
            packageService.addPackage(weddingPackage);
            ra.addFlashAttribute("successMsg", "Package created successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }

    // ── UPDATE (edit) ─────────────────────────────────────────────────────────
    @PostMapping("/update/{id}")
    public String updatePackage(@PathVariable Long id,
                                @ModelAttribute WeddingPackage weddingPackage,
                                RedirectAttributes ra) {
        try {
            packageService.updatePackage(id, weddingPackage);
            ra.addFlashAttribute("successMsg", "Package updated successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }

    // ── DELETE ────────────────────────────────────────────────────────────────
    @GetMapping("/delete/{id}")
    public String deletePackage(@PathVariable Long id, RedirectAttributes ra) {
        try {
            packageService.deletePackage(id);
            ra.addFlashAttribute("successMsg", "Package deleted successfully!");
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/packages";
    }

    // ── IMAGE UPLOAD (AJAX) ───────────────────────────────────────────────────
    /**
     * Saves uploaded images to WMS/Img/ and returns public /pkg-img/ URLs.
     * Spring serves /pkg-img/** from WMS/Img/ via WebMvcConfig.
     */
    @PostMapping("/upload-images")
    @ResponseBody
    public ResponseEntity<?> uploadImages(
            @RequestParam("files") List<MultipartFile> files) {

        List<String> urls = new ArrayList<>();

        try {
            Path uploadDir = getImgDir();

            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;

                String originalName = file.getOriginalFilename();
                String ext = "";
                if (originalName != null && originalName.contains(".")) {
                    ext = originalName.substring(originalName.lastIndexOf('.'));
                }

                String filename = UUID.randomUUID() + ext;
                Path dest = uploadDir.resolve(filename);
                Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

                // URL served by the custom resource handler registered in WebMvcConfig
                urls.add("/pkg-img/" + filename);
            }

            return ResponseEntity.ok(Map.of("urls", urls));

        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }
}
