package com.wms.repository;

import com.wms.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByType(String type);

    List<Report> findAllByOrderByGeneratedDateDesc();

    @Query("SELECT r FROM Report r WHERE " +
            "LOWER(r.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.type) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(r.generatedBy) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Report> searchReports(@Param("keyword") String keyword);
}