package com.wms.repository;

import com.wms.entity.WeddingPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PackageRepository extends JpaRepository<WeddingPackage, Long> {

    List<WeddingPackage> findByStatus(String status);

    List<WeddingPackage> findByPriceLessThanEqual(BigDecimal maxPrice);

    List<WeddingPackage> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    @Query("SELECT p FROM WeddingPackage p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.inclusions) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<WeddingPackage> searchPackages(@Param("keyword") String keyword);
}
