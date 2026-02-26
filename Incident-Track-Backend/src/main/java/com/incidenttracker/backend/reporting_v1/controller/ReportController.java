package com.incidenttracker.backend.reporting_v1.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.incidenttracker.backend.reporting_v1.dto.DepartmentPerformanceDto;
import com.incidenttracker.backend.reporting_v1.dto.PieChartResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.ReportResponseDto;
import com.incidenttracker.backend.reporting_v1.enums.TrendBucket;
import com.incidenttracker.backend.reporting_v1.service.ReportService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    // Example:
    // /api/reports/global/volume-trend?start=2026-02-01&end=2026-02-07&bucket=DAILY
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/global/volume-trend")
    public ReportResponseDto globalVolumeTrend(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "DAILY") TrendBucket bucket
    ) {
        LocalDateRange range = resolveYearToDate(start, end);
        return reportService.generateGlobalVolumeTrend(range.start, range.end, bucket);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/department/volume-trend")
    public ReportResponseDto departmentVolumeTrend(
            @RequestParam Long departmentId,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end,
            @RequestParam(defaultValue = "MONTHLY") TrendBucket bucket
    ) {
        LocalDateRange range = resolveYearToDate(start, end);
        return reportService.generateDepartmentVolumeTrend(departmentId, range.start, range.end, bucket);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/departments/performance")
    public List<DepartmentPerformanceDto> departmentPerformance(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getDepartmentPerformance(range.start, range.end);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/department/sla-summary")
    public ReportResponseDto departmentSlaSummary(
            @RequestParam Long departmentId,
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.generateDepartmentSlaSummary(departmentId, range.start, range.end);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/departments/performance-report")
    public ReportResponseDto departmentsPerformanceReport(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.generateDepartmentsPerformanceReport(range.start, range.end);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/history")
    public List<ReportResponseDto> reportHistory() {
        return reportService.getAllReports();
    }


    // ---------- Pie Chart --------------------------
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/charts/incidents-by-department")
    public PieChartResponseDto incidentsByDepartment(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getIncidentCountByDepartment(range.start, range.end);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/charts/incidents-by-category")
    public PieChartResponseDto incidentsByCategory(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getIncidentCountByCategory(range.start, range.end);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/charts/incidents-by-status")
    public PieChartResponseDto incidentsByStatus(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getIncidentCountByStatus(range.start, range.end);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/charts/sla-breaches-by-department")
    public PieChartResponseDto slaBreachesByDepartment(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getSlaBreachesByDepartment(range.start, range.end);
    }

    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @GetMapping("/charts/incidents-by-severity")
    public PieChartResponseDto incidentsBySeverity(
            @RequestParam(required = false) LocalDate start,
            @RequestParam(required = false) LocalDate end
    ) {
        LocalDateRange range = resolveMonthToDate(start, end);
        return reportService.getIncidentCountBySeverity(range.start, range.end);
    }

    private LocalDateRange resolveYearToDate(LocalDate start, LocalDate end) {
        LocalDate resolvedStart = (start != null) ? start : LocalDate.now().withDayOfYear(1);
        LocalDate resolvedEnd = (end != null) ? end : LocalDate.now();
        return new LocalDateRange(resolvedStart, resolvedEnd);
    }

    private LocalDateRange resolveMonthToDate(LocalDate start, LocalDate end) {
        LocalDate resolvedStart = (start != null) ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate resolvedEnd = (end != null) ? end : LocalDate.now();
        return new LocalDateRange(resolvedStart, resolvedEnd);
    }

    // record 
    private record LocalDateRange(LocalDate start, LocalDate end) {}

    // Converted LocalDateRange to a Java 21 record for cleaner, more readable code.
    // private static class LocalDateRange {
    //     private final LocalDate start;
    //     private final LocalDate end;

    //     private LocalDateRange(LocalDate start, LocalDate end) {
    //         this.start = start;
    //         this.end = end;
    //     }
    // }
}
