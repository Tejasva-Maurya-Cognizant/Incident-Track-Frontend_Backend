package com.incidenttracker.backend.reporting_v1.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.incidenttracker.backend.reporting_v1.dto.DepartmentPerformanceDto;
import com.incidenttracker.backend.reporting_v1.dto.PieChartResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.ReportResponseDto;
import com.incidenttracker.backend.reporting_v1.enums.TrendBucket;

@Service
// @RequiredArgsConstructor
public interface ReportService {
    public ReportResponseDto generateGlobalVolumeTrend(LocalDate start, LocalDate end, TrendBucket bucket);

    public ReportResponseDto generateDepartmentVolumeTrend(Long departmentId, LocalDate start, LocalDate end, TrendBucket bucket);

    public ReportResponseDto generateDepartmentSlaSummary(Long departmentId, LocalDate start, LocalDate end);

    public List<DepartmentPerformanceDto> getDepartmentPerformance(LocalDate start, LocalDate end);

    public ReportResponseDto generateDepartmentsPerformanceReport(LocalDate start, LocalDate end);

    public List<ReportResponseDto> getAllReports();


    //  -------------- Pie Chart ------------------------
    PieChartResponseDto getIncidentCountByDepartment(LocalDate start, LocalDate end);

    PieChartResponseDto getIncidentCountByCategory(LocalDate start, LocalDate end);

    PieChartResponseDto getIncidentCountByStatus(LocalDate start, LocalDate end);

    PieChartResponseDto getSlaBreachesByDepartment(LocalDate start, LocalDate end);

    PieChartResponseDto getIncidentCountBySeverity(LocalDate start, LocalDate end);


}
