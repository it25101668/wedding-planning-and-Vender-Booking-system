package com.wms.repository;

import com.wms.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Customer> findByActive(boolean active);

    @Query("SELECT c FROM Customer c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchCustomers(@Param("keyword") String keyword);

    @Query("SELECT c FROM Customer c LEFT JOIN c.user u WHERE u IS NULL OR u.role <> 'ADMIN' ORDER BY c.createdAt DESC")
    List<Customer> findRecentCustomers();

    @Query("SELECT COUNT(c) FROM Customer c LEFT JOIN c.user u WHERE u IS NULL OR u.role <> 'ADMIN'")
    long countNonAdminCustomers();
}
