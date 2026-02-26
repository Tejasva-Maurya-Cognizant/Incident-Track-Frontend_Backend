package com.incidenttracker.backend.reporting_v1.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.reporting_v1.entity.Report;
import com.incidenttracker.backend.reporting_v1.enums.ReportScope;
import com.incidenttracker.backend.reporting_v1.enums.ReportType;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByScopeOrderByGeneratedAtDesc(ReportScope scope);

    List<Report> findByReportTypeAndScopeOrderByGeneratedAtDesc(ReportType type, ReportScope scope);

    List<Report> findAllByOrderByGeneratedAtDesc();
}
