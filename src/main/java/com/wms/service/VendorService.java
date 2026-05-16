package com.wms.service;

import com.wms.entity.Vendor;
import com.wms.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    public Optional<Vendor> getVendorById(Long id) {
        return vendorRepository.findById(id);
    }

    public Vendor addVendor(Vendor vendor) {
        return vendorRepository.save(vendor);
    }

    public Vendor updateVendor(Long id, Vendor updatedVendor) {
        Vendor existing = vendorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + id));
        existing.setName(updatedVendor.getName());
        existing.setCategory(updatedVendor.getCategory());
        existing.setEmail(updatedVendor.getEmail());
        existing.setPhone(updatedVendor.getPhone());
        existing.setAddress(updatedVendor.getAddress());
        existing.setPricingTier(updatedVendor.getPricingTier());
        existing.setDescription(updatedVendor.getDescription());
        existing.setStatus(updatedVendor.getStatus());
        return vendorRepository.save(existing);
    }

    public void deleteVendor(Long id) {
        if (!vendorRepository.existsById(id)) {
            throw new RuntimeException("Vendor not found with id: " + id);
        }
        vendorRepository.deleteById(id);
    }

    public List<Vendor> searchVendors(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return vendorRepository.findAll();
        }
        return vendorRepository.searchVendors(keyword.trim());
    }

    public List<Vendor> getVendorsByCategory(String category) {
        return vendorRepository.findByCategory(category);
    }

    public long getTotalVendors() {
        return vendorRepository.count();
    }
}
