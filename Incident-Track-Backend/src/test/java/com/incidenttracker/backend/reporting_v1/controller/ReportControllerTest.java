package com.incidenttracker.backend.reporting_v1.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.incidenttracker.backend.reporting_v1.dto.DepartmentPerformanceDto;
import com.incidenttracker.backend.reporting_v1.dto.PieChartResponseDto;
import com.incidenttracker.backend.reporting_v1.dto.PieSliceDto;
import com.incidenttracker.backend.reporting_v1.dto.ReportResponseDto;
import com.incidenttracker.backend.reporting_v1.enums.TrendBucket;
import com.incidenttracker.backend.reporting_v1.service.ReportService;

// Enable Mockito annotations (@Mock/@InjectMocks) for this test class.
@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

    private MockMvc mockMvc;

    // Creates a Mockito mock for isolating dependencies.
    @Mock
    private ReportService reportService;

    // Injects mocks into the class under test.
    @InjectMocks
    private ReportController reportController;

    // Runs before each test to prepare common setup.
    @BeforeEach
    // Setup: create shared fixtures/mocks so each test runs in a predictable state.
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    // Marks a method as a test case.
    @Test
    // Test: runs the globalVolumeTrend_defaultsToYearStartAndToday scenario and checks expected outputs/side effects.
    void globalVolumeTrend_defaultsToYearStartAndToday() throws Exception {
        ReportResponseDto response = ReportResponseDto.builder()
                .reportType("GLOBAL_VOLUME_TREND")
                .incidentCount(10L)
                .build();

        when(reportService.generateGlobalVolumeTrend(any(LocalDate.class), any(LocalDate.class), any(TrendBucket.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/global/volume-trend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("GLOBAL_VOLUME_TREND"))
                .andExpect(jsonPath("$.incidentCount").value(10L));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<TrendBucket> bucketCaptor = ArgumentCaptor.forClass(TrendBucket.class);
        verify(reportService).generateGlobalVolumeTrend(startCaptor.capture(), endCaptor.capture(), bucketCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfYear(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
        Assertions.assertEquals(TrendBucket.DAILY, bucketCaptor.getValue());
    }

    @Test
    // Test: runs the departmentVolumeTrend_usesProvidedParams scenario and checks expected outputs/side effects.
    void departmentVolumeTrend_usesProvidedParams() throws Exception {
        ReportResponseDto response = ReportResponseDto.builder()
                .reportType("DEPARTMENT_VOLUME_TREND")
                .scopeRefId(5L)
                .build();

        when(reportService.generateDepartmentVolumeTrend(any(Long.class), any(LocalDate.class), any(LocalDate.class), any(TrendBucket.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/department/volume-trend")
                        .param("departmentId", "5")
                        .param("start", "2026-02-01")
                        .param("end", "2026-02-07")
                        .param("bucket", "MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("DEPARTMENT_VOLUME_TREND"))
                .andExpect(jsonPath("$.scopeRefId").value(5L));

        ArgumentCaptor<Long> deptCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<TrendBucket> bucketCaptor = ArgumentCaptor.forClass(TrendBucket.class);
        verify(reportService).generateDepartmentVolumeTrend(deptCaptor.capture(), startCaptor.capture(), endCaptor.capture(), bucketCaptor.capture());

        Assertions.assertEquals(5L, deptCaptor.getValue());
        Assertions.assertEquals(LocalDate.of(2026, 2, 1), startCaptor.getValue());
        Assertions.assertEquals(LocalDate.of(2026, 2, 7), endCaptor.getValue());
        Assertions.assertEquals(TrendBucket.MONTHLY, bucketCaptor.getValue());
    }

    @Test
    // Test: runs the departmentPerformance_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void departmentPerformance_defaultsToMonthStartAndToday() throws Exception {
        DepartmentPerformanceDto d1 = DepartmentPerformanceDto.builder()
                .departmentId(1L)
                .departmentName("IT")
                .incidentCount(5L)
                .build();
        DepartmentPerformanceDto d2 = DepartmentPerformanceDto.builder()
                .departmentId(2L)
                .departmentName("HR")
                .incidentCount(3L)
                .build();

        when(reportService.getDepartmentPerformance(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(d1, d2));

        mockMvc.perform(get("/api/reports/departments/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].departmentId").value(1L))
                .andExpect(jsonPath("$[1].departmentId").value(2L));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getDepartmentPerformance(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the departmentSlaSummary_usesProvidedParams scenario and checks expected outputs/side effects.
    void departmentSlaSummary_usesProvidedParams() throws Exception {
        ReportResponseDto response = ReportResponseDto.builder()
                .reportType("DEPARTMENT_SLA_SUMMARY")
                .scopeRefId(2L)
                .build();

        when(reportService.generateDepartmentSlaSummary(any(Long.class), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/department/sla-summary")
                        .param("departmentId", "2")
                        .param("start", "2026-02-01")
                        .param("end", "2026-02-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("DEPARTMENT_SLA_SUMMARY"))
                .andExpect(jsonPath("$.scopeRefId").value(2L));

        ArgumentCaptor<Long> deptCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).generateDepartmentSlaSummary(deptCaptor.capture(), startCaptor.capture(), endCaptor.capture());

        Assertions.assertEquals(2L, deptCaptor.getValue());
        Assertions.assertEquals(LocalDate.of(2026, 2, 1), startCaptor.getValue());
        Assertions.assertEquals(LocalDate.of(2026, 2, 7), endCaptor.getValue());
    }

    @Test
    // Test: runs the departmentsPerformanceReport_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void departmentsPerformanceReport_defaultsToMonthStartAndToday() throws Exception {
        ReportResponseDto response = ReportResponseDto.builder()
                .reportType("DEPARTMENTS_PERFORMANCE_REPORT")
                .build();

        when(reportService.generateDepartmentsPerformanceReport(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/departments/performance-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportType").value("DEPARTMENTS_PERFORMANCE_REPORT"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).generateDepartmentsPerformanceReport(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the incidentsByDepartment_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void incidentsByDepartment_defaultsToMonthStartAndToday() throws Exception {
        PieChartResponseDto response = PieChartResponseDto.builder()
                .title("Incidents by Department")
                .data(List.of(
                        PieSliceDto.builder().label("IT").value(4L).build(),
                        PieSliceDto.builder().label("HR").value(2L).build()
                ))
                .build();

        when(reportService.getIncidentCountByDepartment(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/charts/incidents-by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incidents by Department"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].label").value("IT"))
                .andExpect(jsonPath("$.data[0].value").value(4L));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getIncidentCountByDepartment(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the incidentsByCategory_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void incidentsByCategory_defaultsToMonthStartAndToday() throws Exception {
        PieChartResponseDto response = PieChartResponseDto.builder()
                .title("Incidents by Category")
                .data(List.of(PieSliceDto.builder().label("Network").value(5L).build()))
                .build();

        when(reportService.getIncidentCountByCategory(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/charts/incidents-by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incidents by Category"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].label").value("Network"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getIncidentCountByCategory(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the incidentsByStatus_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void incidentsByStatus_defaultsToMonthStartAndToday() throws Exception {
        PieChartResponseDto response = PieChartResponseDto.builder()
                .title("Incidents by Status")
                .data(List.of(PieSliceDto.builder().label("OPEN").value(7L).build()))
                .build();

        when(reportService.getIncidentCountByStatus(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/charts/incidents-by-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incidents by Status"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].label").value("OPEN"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getIncidentCountByStatus(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the slaBreachesByDepartment_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void slaBreachesByDepartment_defaultsToMonthStartAndToday() throws Exception {
        PieChartResponseDto response = PieChartResponseDto.builder()
                .title("SLA Breaches by Department")
                .data(List.of(PieSliceDto.builder().label("Finance").value(1L).build()))
                .build();

        when(reportService.getSlaBreachesByDepartment(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/charts/sla-breaches-by-department"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("SLA Breaches by Department"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].label").value("Finance"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getSlaBreachesByDepartment(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }

    @Test
    // Test: runs the incidentsBySeverity_defaultsToMonthStartAndToday scenario and checks expected outputs/side effects.
    void incidentsBySeverity_defaultsToMonthStartAndToday() throws Exception {
        PieChartResponseDto response = PieChartResponseDto.builder()
                .title("Incidents by Severity")
                .data(List.of(PieSliceDto.builder().label("HIGH").value(9L).build()))
                .build();

        when(reportService.getIncidentCountBySeverity(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/reports/charts/incidents-by-severity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Incidents by Severity"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].label").value("HIGH"));

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(reportService).getIncidentCountBySeverity(startCaptor.capture(), endCaptor.capture());

        LocalDate expectedStart = LocalDate.now().withDayOfMonth(1);
        LocalDate expectedEnd = LocalDate.now();
        Assertions.assertEquals(expectedStart, startCaptor.getValue());
        Assertions.assertEquals(expectedEnd, endCaptor.getValue());
    }
}
