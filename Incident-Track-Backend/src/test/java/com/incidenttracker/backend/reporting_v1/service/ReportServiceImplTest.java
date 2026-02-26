package com.incidenttracker.backend.reporting_v1.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.incidenttracker.backend.category.entity.Category;
import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.common.enums.TaskStatus;
import com.incidenttracker.backend.common.security.SecurityService;
import com.incidenttracker.backend.department.entity.Department;
import com.incidenttracker.backend.department.repository.DepartmentRepository;
import com.incidenttracker.backend.incident.entity.Incident;
import com.incidenttracker.backend.incident.repository.IncidentRepository;
import com.incidenttracker.backend.reporting_v1.dto.DepartmentPerformanceDto;
import com.incidenttracker.backend.reporting_v1.dto.PieChartResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.ReportResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.SlaSummaryDto;
import com.incidenttracker.backend.reporting_v1.dto.TrendPointDto;
import com.incidenttracker.backend.reporting_v1.enums.TrendBucket;
import com.incidenttracker.backend.reporting_v1.entity.Report;
import com.incidenttracker.backend.reporting_v1.repository.ReportRepository;
import com.incidenttracker.backend.reporting_v1.service.impl.ReportServiceImpl;
import com.incidenttracker.backend.task.entity.Task;
import com.incidenttracker.backend.task.repository.TaskRepository;
import com.incidenttracker.backend.user.entity.User;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SecurityService securityService;

    // Injects mocks into the class under test.
    @InjectMocks
    private ReportServiceImpl reportService;

    // Marks a method as a test case.
    @Test
    // Test: runs the generateGlobalVolumeTrend_computesMetricsAndDailySeries scenario and checks expected outputs/side effects.
    void generateGlobalVolumeTrend_computesMetricsAndDailySeries() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 2);

        Department dept = department(1L, "IT");
        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 1, 1, 0), IncidentStatus.RESOLVED, true, dept, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 2, 10, 0), IncidentStatus.OPEN, false, dept, "Network", IncidentSeverity.LOW);

        when(securityService.getCurrentUser()).thenReturn(Optional.of(user(99L)));
        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2));
        when(taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(List.of(1L)))
                .thenReturn(List.of(completedTask(1L, i1, LocalDateTime.of(2026, 2, 1, 5, 0))));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> savedReport(invocation.getArgument(0), 100L));

        ReportResponseDto response = reportService.generateGlobalVolumeTrend(start, end, TrendBucket.DAILY);

        assertEquals("VOLUME_TREND", response.getReportType());
        assertEquals("GLOBAL", response.getScope());
        assertEquals(2L, response.getIncidentCount());
        assertEquals(1L, response.getResolvedIncidentCount());
        assertEquals(1L, response.getSlaBreachedCount());
        assertEquals(50.0, response.getSlaComplianceRate());
        assertEquals(4.0, response.getAverageResolutionTimeHours());

        List<TrendPointDto> series = castSeries(response.getSeries());
        assertEquals(2, series.size());
        assertEquals("2026-02-01", series.get(0).getLabel());
        assertEquals(1L, series.get(0).getIncidentCount());
        assertEquals(1L, series.get(0).getSlaBreachedCount());
        assertEquals("2026-02-02", series.get(1).getLabel());

        verify(reportRepository).save(any(Report.class));
    }

    @Test
    // Test: runs the generateDepartmentVolumeTrend_buildsMonthlySeries scenario and checks expected outputs/side effects.
    void generateDepartmentVolumeTrend_buildsMonthlySeries() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 3, 10);

        Department dept = department(10L, "IT");
        Incident feb = incident(1L, LocalDateTime.of(2026, 2, 15, 9, 0), IncidentStatus.RESOLVED, false, dept, "Network", IncidentSeverity.MEDIUM);
        Incident mar = incident(2L, LocalDateTime.of(2026, 3, 2, 11, 0), IncidentStatus.OPEN, false, dept, "Network", IncidentSeverity.LOW);

        when(securityService.getCurrentUser()).thenReturn(Optional.of(user(77L)));
        when(departmentRepository.findById(10L)).thenReturn(Optional.of(dept));
        when(incidentRepository.findByReportedDateBetweenAndCategory_Department_DepartmentId(start.atStartOfDay(), end.plusDays(1).atStartOfDay(), 10L))
                .thenReturn(List.of(feb, mar));
        when(taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(List.of(1L))).thenReturn(List.of());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> savedReport(invocation.getArgument(0), 200L));

        ReportResponseDto response = reportService.generateDepartmentVolumeTrend(10L, start, end, TrendBucket.MONTHLY);

        assertEquals("VOLUME_TREND", response.getReportType());
        assertEquals("DEPARTMENT", response.getScope());
        assertEquals(10L, response.getScopeRefId());

        List<TrendPointDto> series = castSeries(response.getSeries());
        assertEquals(2, series.size());
        assertEquals("2026-02", series.get(0).getLabel());
        assertEquals("2026-03", series.get(1).getLabel());
    }

    @Test
    // Test: runs the getDepartmentPerformance_groupsAndSortsByIncidentCount scenario and checks expected outputs/side effects.
    void getDepartmentPerformance_groupsAndSortsByIncidentCount() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Department hr = department(2L, "HR");

        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.RESOLVED, true, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, false, hr, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));
        when(taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(List.of(1L))).thenReturn(List.of());
        when(departmentRepository.findAll()).thenReturn(List.of(it, hr));

        List<DepartmentPerformanceDto> result = reportService.getDepartmentPerformance(start, end);

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getDepartmentId());
        assertEquals("IT", result.get(0).getDepartmentName());
        assertEquals(2L, result.get(0).getIncidentCount());
        assertEquals(50.0, result.get(0).getSlaComplianceRate());
        assertEquals(2L, result.get(1).getDepartmentId());
        assertEquals(1L, result.get(1).getIncidentCount());
    }

    @Test
    // Test: runs the generateDepartmentSlaSummary_returnsSummarySeries scenario and checks expected outputs/side effects.
    void generateDepartmentSlaSummary_returnsSummarySeries() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department dept = department(3L, "Operations");
        Incident resolved = incident(1L, LocalDateTime.of(2026, 2, 1, 8, 0), IncidentStatus.RESOLVED, false, dept, "Network", IncidentSeverity.MEDIUM);
        Incident open = incident(2L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, false, dept, "Network", IncidentSeverity.LOW);
        Incident inProgress = incident(3L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.IN_PROGRESS, true, dept, "Network", IncidentSeverity.HIGH);
        Incident cancelled = incident(4L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.CANCELLED, false, dept, "Network", IncidentSeverity.LOW);

        when(securityService.getCurrentUser()).thenReturn(Optional.of(user(88L)));
        when(departmentRepository.findById(3L)).thenReturn(Optional.of(dept));
        when(incidentRepository.findByReportedDateBetweenAndCategory_Department_DepartmentId(start.atStartOfDay(), end.plusDays(1).atStartOfDay(), 3L))
                .thenReturn(List.of(resolved, open, inProgress, cancelled));
        when(taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(List.of(1L))).thenReturn(List.of());
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> savedReport(invocation.getArgument(0), 300L));

        ReportResponseDto response = reportService.generateDepartmentSlaSummary(3L, start, end);

        assertEquals("SLA_COMPLIANCE", response.getReportType());
        assertEquals("DEPARTMENT", response.getScope());
        assertEquals(3L, response.getScopeRefId());

        SlaSummaryDto summary = assertInstanceOf(SlaSummaryDto.class, response.getSeries());
        assertEquals(4L, summary.getIncidentCount());
        assertEquals(1L, summary.getResolvedIncidentCount());
        assertEquals(1L, summary.getSlaBreachedCount());
        assertEquals(75.0, summary.getSlaComplianceRate());
        assertEquals(1L, summary.getOpenCount());
        assertEquals(1L, summary.getInProgressCount());
        assertEquals(1L, summary.getCancelledCount());
    }

    @Test
    // Test: runs the generateDepartmentsPerformanceReport_returnsPerformanceListAndTotals scenario and checks expected outputs/side effects.
    void generateDepartmentsPerformanceReport_returnsPerformanceListAndTotals() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Department hr = department(2L, "HR");

        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.RESOLVED, true, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, false, hr, "Hardware", IncidentSeverity.MEDIUM);

        when(securityService.getCurrentUser()).thenReturn(Optional.of(user(66L)));
        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));
        when(taskRepository.findByIncident_IncidentIdInAndCompletedDateIsNotNull(List.of(1L))).thenReturn(List.of());
        when(departmentRepository.findAll()).thenReturn(List.of(it, hr));
        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> savedReport(invocation.getArgument(0), 400L));

        ReportResponseDto response = reportService.generateDepartmentsPerformanceReport(start, end);

        assertEquals("DEPARTMENT_PERFORMANCE", response.getReportType());
        assertEquals("GLOBAL", response.getScope());
        assertEquals(3L, response.getIncidentCount());
        assertEquals(1L, response.getResolvedIncidentCount());

        List<DepartmentPerformanceDto> performance = castPerformance(response.getSeries());
        assertEquals(2, performance.size());
        assertEquals(1L, performance.get(0).getDepartmentId());
        assertEquals(2L, performance.get(0).getIncidentCount());
    }

    @Test
    // Test: runs the getIncidentCountByDepartment_buildsPieData scenario and checks expected outputs/side effects.
    void getIncidentCountByDepartment_buildsPieData() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Department hr = department(2L, "HR");

        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, false, hr, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));
        when(departmentRepository.findAll()).thenReturn(List.of(it, hr));

        PieChartResponseDto response = reportService.getIncidentCountByDepartment(start, end);

        assertEquals("Incidents by Department", response.getTitle());
        assertEquals(2, response.getData().size());
        assertEquals("IT", response.getData().get(0).getLabel());
        assertEquals(2L, response.getData().get(0).getValue());
    }

    @Test
    // Test: runs the getIncidentCountByCategory_buildsPieData scenario and checks expected outputs/side effects.
    void getIncidentCountByCategory_buildsPieData() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, false, it, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));

        PieChartResponseDto response = reportService.getIncidentCountByCategory(start, end);

        assertEquals("Incidents by Category", response.getTitle());
        assertEquals(2, response.getData().size());
        assertEquals("Network", response.getData().get(0).getLabel());
        assertEquals(2L, response.getData().get(0).getValue());
    }

    @Test
    // Test: runs the getIncidentCountByStatus_buildsPieData scenario and checks expected outputs/side effects.
    void getIncidentCountByStatus_buildsPieData() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.RESOLVED, false, it, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));

        PieChartResponseDto response = reportService.getIncidentCountByStatus(start, end);

        assertEquals("Incidents by Status", response.getTitle());
        assertEquals(2, response.getData().size());
        assertEquals("OPEN", response.getData().get(0).getLabel());
        assertEquals(2L, response.getData().get(0).getValue());
    }

    @Test
    // Test: runs the getSlaBreachesByDepartment_buildsPieData scenario and checks expected outputs/side effects.
    void getSlaBreachesByDepartment_buildsPieData() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Department hr = department(2L, "HR");

        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, true, it, "Network", IncidentSeverity.HIGH);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.LOW);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, true, hr, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));
        when(departmentRepository.findAll()).thenReturn(List.of(it, hr));

        PieChartResponseDto response = reportService.getSlaBreachesByDepartment(start, end);

        assertEquals("SLA Breaches by Department", response.getTitle());
        assertEquals(2, response.getData().size());
        assertEquals("IT", response.getData().get(0).getLabel());
        assertEquals(1L, response.getData().get(0).getValue());
    }

    @Test
    // Test: runs the getIncidentCountBySeverity_buildsPieData scenario and checks expected outputs/side effects.
    void getIncidentCountBySeverity_buildsPieData() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        LocalDate end = LocalDate.of(2026, 2, 7);

        Department it = department(1L, "IT");
        Incident i1 = incident(1L, LocalDateTime.of(2026, 2, 2, 9, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.CRITICAL);
        Incident i2 = incident(2L, LocalDateTime.of(2026, 2, 3, 10, 0), IncidentStatus.OPEN, false, it, "Network", IncidentSeverity.CRITICAL);
        Incident i3 = incident(3L, LocalDateTime.of(2026, 2, 4, 11, 0), IncidentStatus.OPEN, false, it, "Hardware", IncidentSeverity.MEDIUM);

        when(incidentRepository.findByReportedDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(i1, i2, i3));

        PieChartResponseDto response = reportService.getIncidentCountBySeverity(start, end);

        assertEquals("Incidents by Severity", response.getTitle());
        assertEquals(2, response.getData().size());
        assertEquals("CRITICAL", response.getData().get(0).getLabel());
        assertEquals(2L, response.getData().get(0).getValue());
    }

    private static Department department(Long id, String name) {
        return Department.builder().departmentId(id).departmentName(name).build();
    }

    private static User user(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setEmail("user" + id + "@example.com");
        user.setUsername("user" + id);
        return user;
    }

    private static Incident incident(Long id, LocalDateTime reportedDate, IncidentStatus status, boolean slaBreached,
            Department dept, String categoryName, IncidentSeverity severity) {
        Category category = Category.builder()
                .categoryId(100L)
                .categoryName(categoryName)
                .department(dept)
                .build();

        Incident incident = new Incident();
        incident.setIncidentId(id);
        incident.setReportedDate(reportedDate);
        incident.setStatus(status);
        incident.setSlaBreached(slaBreached);
        incident.setCategory(category);
        incident.setCalculatedSeverity(severity);
        return incident;
    }

    private static Task completedTask(Long id, Incident incident, LocalDateTime completedDate) {
        return Task.builder()
                .taskId(id)
                .incident(incident)
                .completedDate(completedDate)
                .createdDate(completedDate.minusHours(2))
                .dueDate(completedDate.plusHours(2))
                .status(TaskStatus.COMPLETED)
                .assignedTo(new User())
                .assignedBy(new User())
                .build();
    }

    private static Report savedReport(Report report, Long id) {
        report.setReportId(id);
        report.setGeneratedAt(LocalDateTime.of(2026, 2, 9, 0, 0));
        return report;
    }

    @SuppressWarnings("unchecked")
    private static List<TrendPointDto> castSeries(Object series) {
        assertNotNull(series);
        return (List<TrendPointDto>) series;
    }

    @SuppressWarnings("unchecked")
    private static List<DepartmentPerformanceDto> castPerformance(Object series) {
        assertNotNull(series);
        return (List<DepartmentPerformanceDto>) series;
    }
}
