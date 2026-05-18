package com.wms.service;

import com.wms.entity.Report;
import com.wms.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReportService {

    @Autowired
    private ReportRepository reportRepository;

    public List<Report> getAllReports() {
        return reportRepository.findAllByOrderByGeneratedDateDesc();
    }

    public List<Report> getReportsByType(String type) {
        if (type == null || type.isEmpty()) {
            return getAllReports();
        }
        return reportRepository.findByType(type);
    }

    public List<Report> searchReports(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllReports();
        }
        return reportRepository.searchReports(keyword.trim());
    }

    public Optional<Report> getReportById(Long id) {
        return reportRepository.findById(id);
    }

    public Report addReport(Report report) {
        if (report.getTitle() == null || report.getTitle().isEmpty()) {
            throw new RuntimeException("Report title is required.");
        }
        return reportRepository.save(report);
    }

    public Report updateReport(Long id, Report updatedReport) {
        Report existing = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));

        if (updatedReport.getTitle() != null && !updatedReport.getTitle().isEmpty()) {
            existing.setTitle(updatedReport.getTitle());
        }
        if (updatedReport.getType() != null && !updatedReport.getType().isEmpty()) {
            existing.setType(updatedReport.getType());
        }
        if (updatedReport.getContent() != null && !updatedReport.getContent().isEmpty()) {
            existing.setContent(updatedReport.getContent());
        }

        return reportRepository.save(existing);
    }

    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new RuntimeException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
    }

    public long getTotalReports() {
        return reportRepository.count();
    }
}