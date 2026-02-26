package com.incidenttracker.backend.reporting_v1.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.reporting_v1.dto.DepartmentPerformanceDto;
import com.incidenttracker.backend.reporting_v1.dto.PieChartResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.PieSliceDto;
import com.incidenttracker.backend.reporting_v1.dto.ReportResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.SlaSummaryDto;
import com.incidenttracker.backend.reporting_v1.dto.TrendPointDto;
import com.incidenttracker.backend.reporting_v1.entity.Report;
import com.incidenttracker.backend.reporting_v1.enums.ReportScope;
import com.incidenttracker.backend.reporting_v1.enums.ReportType;
import com.incidenttracker.backend.reporting_v1.enums.TrendBucket;
import com.incidenttracker.backend.reporting_v1.repository.ReportRepository;
import com.incidenttracker.backend.reporting_v1.service.ReportService;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.task.repository.TaskRepository;
import com.incidenttracker.backend.user.entity.User;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

        private final IncidentRepository incidentRepository;
        private final TaskRepository taskRepository;
        private final ReportRepository reportRepository;
        private final DepartmentRepository departmentRepository;
        private final SecurityService securityService;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        @Transactional(readOnly = true)
        public List<ReportResponseDto> getAllReports() {
                List<Report> reports = reportRepository.findAllByOrderByGeneratedAtDesc();
                return reports.stream()
                                .map(this::mapStoredReportToResponse)
                                .toList();
        }

        @Override
        @Transactional
        public ReportResponseDto generateGlobalVolumeTrend(LocalDate start, LocalDate end, TrendBucket bucket) {

                User currentUser = getCurrentUserOrThrow();

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay(); // inclusive end

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                // --- Core Metrics ---
                long incidentCount = incidents.size();
                long resolvedCount = incidents.stream().filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();
                long breachedCount = incidents.stream().filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();

                double complianceRate = (incidentCount == 0)
                                ? 100.0
                                : ((double) (incidentCount - breachedCount) / (double) incidentCount) * 100.0;

                double avgResolutionHours = computeAvgResolutionHours(incidents);

                // --- Trend Series (stream grouping) ---
                List<TrendPointDto> series = buildTrendSeries(incidents, bucket);

                String seriesJson;
                try {
                        seriesJson = objectMapper.writeValueAsString(series);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize trend series", e);
                }

                Report report = Report.builder()
                                .reportType(ReportType.VOLUME_TREND)
                                .scope(ReportScope.GLOBAL)
                                .scopeRefId(null)
                                .incidentCount(incidentCount)
                                .resolvedIncidentCount(resolvedCount)
                                .slaBreachedCount(breachedCount)
                                .slaComplianceRate(round2(complianceRate))
                                .averageResolutionTimeHours(round2(avgResolutionHours))
                                .startDate(start)
                                .endDate(end)
                                .seriesJson(seriesJson)
                                .generatedBy(currentUser)
                                .build();

                Report saved = reportRepository.save(report);

                return mapReportToResponse(saved, series);
        }

        @Override
        @Transactional
        public ReportResponseDto generateDepartmentVolumeTrend(Long departmentId, LocalDate start, LocalDate end,
                        TrendBucket bucket) {

                User currentUser = getCurrentUserOrThrow();

                Department dept = departmentRepository.findById(departmentId)
                                .orElseThrow(() -> new RuntimeException("Department not found"));

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository
                                .findByReportedDateBetweenAndCategory_Department_DepartmentId(from, to, departmentId);

                long incidentCount = incidents.size();
                long resolvedCount = incidents.stream().filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();
                long breachedCount = incidents.stream().filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();

                double complianceRate = (incidentCount == 0)
                                ? 100.0
                                : ((double) (incidentCount - breachedCount) / (double) incidentCount) * 100.0;

                double avgResolutionHours = computeAvgResolutionHours(incidents);

                List<TrendPointDto> series = buildTrendSeries(incidents, bucket);

                String seriesJson;
                try {
                        seriesJson = objectMapper.writeValueAsString(series);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize trend series", e);
                }

                Report report = Report.builder()
                                .reportType(ReportType.VOLUME_TREND)
                                .scope(ReportScope.DEPARTMENT)
                                .scopeRefId(departmentId)
                                .incidentCount(incidentCount)
                                .resolvedIncidentCount(resolvedCount)
                                .slaBreachedCount(breachedCount)
                                .slaComplianceRate(round2(complianceRate))
                                .averageResolutionTimeHours(round2(avgResolutionHours))
                                .startDate(start)
                                .endDate(end)
                                .seriesJson(seriesJson)
                                .metricsJson("{\"departmentName\":\"" + dept.getDepartmentName() + "\"}") // optional
                                .generatedBy(currentUser)
                                .build();

                Report saved = reportRepository.save(report);

                return mapReportToResponse(saved, series);
        }

        @Override
        public List<DepartmentPerformanceDto> getDepartmentPerformance(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                // group incidents by department
                Map<Long, List<Incident>> byDept = incidents.stream()
                                .filter(i -> i.getCategory() != null && i.getCategory().getDepartment() != null)
                                .collect(Collectors.groupingBy(i -> i.getCategory().getDepartment().getDepartmentId()));

                // fetch dept names once
                Map<Long, String> deptNames = departmentRepository.findAll().stream()
                                .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));

                // build performance list
                return byDept.entrySet().stream()
                                .map(entry -> {
                                        Long deptId = entry.getKey();
                                        List<Incident> deptIncidents = entry.getValue();

                                        long count = deptIncidents.size();
                                        long resolved = deptIncidents.stream()
                                                        .filter(i -> i.getStatus() == IncidentStatus.RESOLVED).count();
                                        long breached = deptIncidents.stream()
                                                        .filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();

                                        double compliance = (count == 0) ? 100.0
                                                        : ((double) (count - breached) / (double) count) * 100.0;

                                        double avgResHours = computeAvgResolutionHours(deptIncidents);

                                        return DepartmentPerformanceDto.builder()
                                                        .departmentId(deptId)
                                                        .departmentName(deptNames.getOrDefault(deptId, "Unknown"))
                                                        .incidentCount(count)
                                                        .resolvedIncidentCount(resolved)
                                                        .slaBreachedCount(breached)
                                                        .slaComplianceRate(round2(compliance))
                                                        .averageResolutionTimeHours(round2(avgResHours))
                                                        .build();
                                })
                                .sorted(Comparator.comparing(DepartmentPerformanceDto::getIncidentCount).reversed())
                                .toList();
        }

        @Override
        @Transactional
        public ReportResponseDto generateDepartmentSlaSummary(Long departmentId, LocalDate start, LocalDate end) {

                User currentUser = getCurrentUserOrThrow();

                Department dept = departmentRepository.findById(departmentId)
                                .orElseThrow(() -> new RuntimeException("Department not found"));

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository
                                .findByReportedDateBetweenAndCategory_Department_DepartmentId(from, to, departmentId);

                long incidentCount = incidents.size();
                long resolvedCount = countStatus(incidents, IncidentStatus.RESOLVED);
                long breachedCount = incidents.stream().filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();

                long openCount = countStatus(incidents, IncidentStatus.OPEN);
                long inProgressCount = countStatus(incidents, IncidentStatus.IN_PROGRESS);
                long cancelledCount = countStatus(incidents, IncidentStatus.CANCELLED);

                double complianceRate = (incidentCount == 0)
                                ? 100.0
                                : ((double) (incidentCount - breachedCount) / (double) incidentCount) * 100.0;

                // avg resolution time (hours) for RESOLVED incidents in this dept+range
                double avgResolutionHours = computeAvgResolutionHours(incidents);

                // store summary in metricsJson (optional)
                SlaSummaryDto summary = SlaSummaryDto.builder()
                                .incidentCount(incidentCount)
                                .resolvedIncidentCount(resolvedCount)
                                .slaBreachedCount(breachedCount)
                                .slaComplianceRate(round2(complianceRate))
                                .openCount(openCount)
                                .inProgressCount(inProgressCount)
                                .cancelledCount(cancelledCount)
                                .build();

                String metricsJson;
                try {
                        metricsJson = objectMapper.writeValueAsString(summary);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize SLA summary metrics", e);
                }

                Report report = Report.builder()
                                .reportType(ReportType.SLA_COMPLIANCE)
                                .scope(ReportScope.DEPARTMENT)
                                .scopeRefId(departmentId)
                                .incidentCount(incidentCount)
                                .resolvedIncidentCount(resolvedCount)
                                .slaBreachedCount(breachedCount)
                                .slaComplianceRate(round2(complianceRate))
                                .averageResolutionTimeHours(round2(avgResolutionHours))
                                .startDate(start)
                                .endDate(end)
                                .metricsJson(metricsJson)
                                .seriesJson(null) // not a trend report
                                .metricsJson("{\"departmentName\":\"" + dept.getDepartmentName() + "\"}") // optional
                                .generatedBy(currentUser)
                                .build();

                Report saved = reportRepository.save(report);

                return mapReportToResponse(saved, summary); // returning summary object here
        }

        @Override
        @Transactional
        public ReportResponseDto generateDepartmentsPerformanceReport(LocalDate start, LocalDate end) {

                User currentUser = getCurrentUserOrThrow();

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                Map<Long, String> deptNames = departmentRepository.findAll().stream()
                                .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));

                Map<Long, List<Incident>> byDept = incidents.stream()
                                .filter(i -> i.getCategory() != null && i.getCategory().getDepartment() != null)
                                .collect(Collectors.groupingBy(i -> i.getCategory().getDepartment().getDepartmentId()));

                List<DepartmentPerformanceDto> performance = byDept.entrySet().stream()
                                .map(entry -> {
                                        Long deptId = entry.getKey();
                                        List<Incident> deptIncidents = entry.getValue();

                                        long count = deptIncidents.size();
                                        long resolved = countStatus(deptIncidents, IncidentStatus.RESOLVED);
                                        long breached = deptIncidents.stream()
                                                        .filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();
                                        double compliance = (count == 0) ? 100.0
                                                        : ((double) (count - breached) / (double) count) * 100.0;
                                        double avgResHours = computeAvgResolutionHours(deptIncidents);

                                        return DepartmentPerformanceDto.builder()
                                                        .departmentId(deptId)
                                                        .departmentName(deptNames.getOrDefault(deptId, "Unknown"))
                                                        .incidentCount(count)
                                                        .resolvedIncidentCount(resolved)
                                                        .slaBreachedCount(breached)
                                                        .slaComplianceRate(round2(compliance))
                                                        .averageResolutionTimeHours(round2(avgResHours))
                                                        .build();
                                })
                                .sorted(Comparator.comparing(DepartmentPerformanceDto::getIncidentCount).reversed())
                                .toList();

                // store list as JSON series
                String seriesJson;
                try {
                        seriesJson = objectMapper.writeValueAsString(performance);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize department performance series", e);
                }

                // store global summary in core columns too (optional, useful)
                long totalIncidentCount = incidents.size();
                long totalResolved = countStatus(incidents, IncidentStatus.RESOLVED);
                long totalBreached = incidents.stream().filter(i -> Boolean.TRUE.equals(i.getSlaBreached())).count();
                double totalCompliance = (totalIncidentCount == 0)
                                ? 100.0
                                : ((double) (totalIncidentCount - totalBreached) / (double) totalIncidentCount) * 100.0;
                double totalAvgRes = computeAvgResolutionHours(incidents);

                Report report = Report.builder()
                                .reportType(ReportType.DEPARTMENT_PERFORMANCE)
                                .scope(ReportScope.GLOBAL)
                                .scopeRefId(null)
                                .incidentCount(totalIncidentCount)
                                .resolvedIncidentCount(totalResolved)
                                .slaBreachedCount(totalBreached)
                                .slaComplianceRate(round2(totalCompliance))
                                .averageResolutionTimeHours(round2(totalAvgRes))
                                .startDate(start)
                                .endDate(end)
                                .seriesJson(seriesJson)
                                .metricsJson(null)
                                .generatedBy(currentUser)
                                .build();

                Report saved = reportRepository.save(report);

                return mapReportToResponse(saved, performance); // return list
        }

        // ======================================= Incidents by Department (Pie)
        // ========================================

        @Override
        public PieChartResponseDto getIncidentCountByDepartment(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                // deptId -> count
                Map<Long, Long> counts = incidents.stream()
                                .filter(i -> i.getCategory() != null && i.getCategory().getDepartment() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getCategory().getDepartment().getDepartmentId(),
                                                Collectors.counting()));

                Map<Long, String> deptNames = departmentRepository.findAll().stream()
                                .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));

                List<PieSliceDto> data = counts.entrySet().stream()
                                .map(e -> PieSliceDto.builder()
                                                .label(deptNames.getOrDefault(e.getKey(), "Unknown"))
                                                .value(e.getValue())
                                                .build())
                                .sorted(Comparator.comparing(PieSliceDto::getValue).reversed())
                                .toList();

                return PieChartResponseDto.builder()
                                .title("Incidents by Department")
                                .data(data)
                                .build();
        }

        // Incidents by Category (Pie)

        @Override
        public PieChartResponseDto getIncidentCountByCategory(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                Map<String, Long> counts = incidents.stream()
                                .filter(i -> i.getCategory() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getCategory().getCategoryName(), // parent category name
                                                Collectors.counting()));

                List<PieSliceDto> data = counts.entrySet().stream()
                                .map(e -> PieSliceDto.builder()
                                                .label(e.getKey())
                                                .value(e.getValue())
                                                .build())
                                .sorted(Comparator.comparing(PieSliceDto::getValue).reversed())
                                .toList();

                return PieChartResponseDto.builder()
                                .title("Incidents by Category")
                                .data(data)
                                .build();
        }

        // Incidents by Status (Pie)
        @Override
        public PieChartResponseDto getIncidentCountByStatus(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                Map<String, Long> counts = incidents.stream()
                                .collect(Collectors.groupingBy(
                                                i -> i.getStatus().name(),
                                                Collectors.counting()));

                List<PieSliceDto> data = counts.entrySet().stream()
                                .map(e -> PieSliceDto.builder()
                                                .label(e.getKey())
                                                .value(e.getValue())
                                                .build())
                                .sorted(Comparator.comparing(PieSliceDto::getValue).reversed())
                                .toList();

                return PieChartResponseDto.builder()
                                .title("Incidents by Status")
                                .data(data)
                                .build();
        }

        // SLA Breaches by Department (Pie)

        @Override
        public PieChartResponseDto getSlaBreachesByDepartment(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                Map<Long, Long> counts = incidents.stream()
                                .filter(i -> Boolean.TRUE.equals(i.getSlaBreached()))
                                .filter(i -> i.getCategory() != null && i.getCategory().getDepartment() != null)
                                .collect(Collectors.groupingBy(
                                                i -> i.getCategory().getDepartment().getDepartmentId(),
                                                Collectors.counting()));

                Map<Long, String> deptNames = departmentRepository.findAll().stream()
                                .collect(Collectors.toMap(Department::getDepartmentId, Department::getDepartmentName));

                List<PieSliceDto> data = counts.entrySet().stream()
                                .map(e -> PieSliceDto.builder()
                                                .label(deptNames.getOrDefault(e.getKey(), "Unknown"))
                                                .value(e.getValue())
                                                .build())
                                .sorted(Comparator.comparing(PieSliceDto::getValue).reversed())
                                .toList();

                return PieChartResponseDto.builder()
                                .title("SLA Breaches by Department")
                                .data(data)
                                .build();
        }

        // count by severity
        @Override
        public PieChartResponseDto getIncidentCountBySeverity(LocalDate start, LocalDate end) {

                LocalDateTime from = start.atStartOfDay();
                LocalDateTime to = end.plusDays(1).atStartOfDay();

                List<Incident> incidents = incidentRepository.findByReportedDateBetween(from, to);

                Map<String, Long> counts = incidents.stream()
                                .collect(Collectors.groupingBy(
                                                i -> i.getCalculatedSeverity().name(),
                                                Collectors.counting()));

                List<PieSliceDto> data = counts.entrySet().stream()
                                .map(e -> PieSliceDto.builder()
                                                .label(e.getKey()) // CRITICAL / HIGH / MEDIUM / LOW
                                                .value(e.getValue())
                                                .build())
                                .sorted(Comparator.comparing(PieSliceDto::getValue).reversed())
                                .toList();

                return PieChartResponseDto.builder()
                                .title("Incidents by Severity")
                                .data(data)
                                .build();
        }

        // ---------- helpers ----------

        private List<TrendPointDto> buildTrendSeries(List<Incident> incidents, TrendBucket bucket) {
                if (bucket == TrendBucket.MONTHLY) {
                        Map<YearMonth, List<Incident>> grouped = incidents.stream()
                                        .collect(Collectors.groupingBy(i -> YearMonth.from(i.getReportedDate())));

                        return grouped.entrySet().stream()
                                        .sorted(Map.Entry.comparingByKey())
                                        .map(e -> TrendPointDto.builder()
                                                        .label(e.getKey().toString()) // "2026-02"
                                                        .incidentCount((long) e.getValue().size())
                                                        .slaBreachedCount(e.getValue().stream().filter(
                                                                        i -> Boolean.TRUE.equals(i.getSlaBreached()))
                                                                        .count())
                                                        .build())
                                        .toList();
                }

                // DAILY
                DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
                Map<LocalDate, List<Incident>> grouped = incidents.stream()
                                .collect(Collectors.groupingBy(i -> i.getReportedDate().toLocalDate()));

                return grouped.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(e -> TrendPointDto.builder()
                                                .label(e.getKey().format(fmt)) // "2026-02-07"
                                                .incidentCount((long) e.getValue().size())
                                                .slaBreachedCount(e.getValue().stream()
                                                                .filter(i -> Boolean.TRUE.equals(i.getSlaBreached()))
                                                                .count())
                                                .build())
                                .toList();
        }

        private double computeAvgResolutionHours(List<Incident> incidents) {
                List<Incident> resolvedIncidents = incidents.stream()
                                .filter(i -> i.getStatus() == IncidentStatus.RESOLVED)
                                .toList();

                if (resolvedIncidents.isEmpty())
                        return 0.0;

                List<Long> incidentIds = resolvedIncidents.stream().map(Incident::getIncidentId).toList();

                // fetch all completed tasks for these incidents
                List<Task> completedTasks = taskRepository
                                .findByIncident_IncidentIdInAndCompletedDateIsNotNull(incidentIds);

                // incidentId -> max completedDate
                Map<Long, LocalDateTime> incidentMaxCompleted = completedTasks.stream()
                                .collect(Collectors.toMap(
                                                t -> t.getIncident().getIncidentId(),
                                                Task::getCompletedDate,
                                                (a, b) -> a.isAfter(b) ? a : b));

                // compute duration from reportedDate -> resolvedAt(max task completed)
                List<Double> hours = new ArrayList<>();
                for (Incident i : resolvedIncidents) {
                        LocalDateTime resolvedAt = incidentMaxCompleted.get(i.getIncidentId());
                        if (resolvedAt != null) {
                                long minutes = Duration.between(i.getReportedDate(), resolvedAt).toMinutes();
                                hours.add(minutes / 60.0);
                        }
                }

                if (hours.isEmpty())
                        return 0.0;
                return hours.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        }

        private double round2(double v) {
                return Math.round(v * 100.0) / 100.0;
        }

        private long countStatus(List<Incident> incidents, IncidentStatus status) {
                return incidents.stream().filter(i -> i.getStatus() == status).count();
        }

        private User getCurrentUserOrThrow() {
                return securityService.getCurrentUser()
                                .orElseThrow(() -> new RuntimeException("Authentication required"));
        }

        private ReportResponseDto mapReportToResponse(Report report, Object series) {
                return ReportResponseDto.builder()
                                .reportId(report.getReportId())
                                .reportType(report.getReportType().name())
                                .scope(report.getScope().name())
                                .scopeRefId(report.getScopeRefId())
                                .incidentCount(report.getIncidentCount())
                                .resolvedIncidentCount(report.getResolvedIncidentCount())
                                .slaBreachedCount(report.getSlaBreachedCount())
                                .slaComplianceRate(report.getSlaComplianceRate())
                                .averageResolutionTimeHours(report.getAverageResolutionTimeHours())
                                .startDate(report.getStartDate())
                                .endDate(report.getEndDate())
                                .series(series)
                                .generatedAt(report.getGeneratedAt())
                                .build();
        }

        private ReportResponseDto mapStoredReportToResponse(Report report) {
                Object series = parseJsonOrNull(report.getSeriesJson());
                if (series == null) {
                        series = parseJsonOrNull(report.getMetricsJson());
                }
                return mapReportToResponse(report, series);
        }

        private Object parseJsonOrNull(String json) {
                if (json == null || json.isBlank()) {
                        return null;
                }
                try {
                        return objectMapper.readValue(json, Object.class);
                } catch (Exception e) {
                        return json;
                }
        }
}
