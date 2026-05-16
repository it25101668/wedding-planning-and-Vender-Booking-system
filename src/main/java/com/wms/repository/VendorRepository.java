package com.wms.repository;

import com.wms.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {

    List<Vendor> findByCategory(String category);

    List<Vendor> findByStatus(String status);

    List<Vendor> findByPricingTier(String pricingTier);

    @Query("SELECT v FROM Vendor v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.category) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.address) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vendor> searchVendors(@Param("keyword") String keyword);
}
