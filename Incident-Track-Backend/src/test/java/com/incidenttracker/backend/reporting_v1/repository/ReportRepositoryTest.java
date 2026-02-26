package com.incidenttracker.backend.reporting_v1.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.incidenttracker.backend.reporting_v1.entity.Report;
import com.incidenttracker.backend.reporting_v1.enums.ReportScope;
import com.incidenttracker.backend.reporting_v1.enums.ReportType;

// Slice test: loads only JPA components with an in-memory DB setup.
@DataJpaTest
// Use the "test" Spring profile (loads application-test.properties).
@ActiveProfiles("test")
class ReportRepositoryTest {

    // Injects a Spring-managed bean into the test.
    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Marks a method as a test case.
    @Test
    // Test: runs the findByScopeOrderByGeneratedAtDesc_returnsReportsInDescOrder scenario and checks expected outputs/side effects.
    void findByScopeOrderByGeneratedAtDesc_returnsReportsInDescOrder() throws Exception {
        Report first = persistReport(ReportType.VOLUME_TREND, ReportScope.GLOBAL, 10L);
        Thread.sleep(5);
        Report second = persistReport(ReportType.SLA_COMPLIANCE, ReportScope.GLOBAL, 20L);

        List<Report> result = reportRepository.findByScopeOrderByGeneratedAtDesc(ReportScope.GLOBAL);

        assertEquals(2, result.size());
        assertEquals(second.getReportId(), result.get(0).getReportId());
        assertEquals(first.getReportId(), result.get(1).getReportId());
    }

    @Test
    // Test: runs the findByReportTypeAndScopeOrderByGeneratedAtDesc_filtersAndOrders scenario and checks expected outputs/side effects.
    void findByReportTypeAndScopeOrderByGeneratedAtDesc_filtersAndOrders() throws Exception {
        Report match1 = persistReport(ReportType.VOLUME_TREND, ReportScope.DEPARTMENT, 10L);
        Thread.sleep(5);
        Report match2 = persistReport(ReportType.VOLUME_TREND, ReportScope.DEPARTMENT, 20L);
        persistReport(ReportType.SLA_COMPLIANCE, ReportScope.DEPARTMENT, 30L);
        persistReport(ReportType.VOLUME_TREND, ReportScope.GLOBAL, 40L);

        List<Report> result = reportRepository.findByReportTypeAndScopeOrderByGeneratedAtDesc(
                ReportType.VOLUME_TREND, ReportScope.DEPARTMENT);

        assertEquals(2, result.size());
        assertEquals(match2.getReportId(), result.get(0).getReportId());
        assertEquals(match1.getReportId(), result.get(1).getReportId());
        assertTrue(result.stream().allMatch(r -> r.getReportType() == ReportType.VOLUME_TREND));
        assertTrue(result.stream().allMatch(r -> r.getScope() == ReportScope.DEPARTMENT));
    }

    private Report persistReport(ReportType type, ReportScope scope, Long incidentCount) {
        Report report = Report.builder()
                .reportType(type)
                .scope(scope)
                .incidentCount(incidentCount)
                .resolvedIncidentCount(0L)
                .slaBreachedCount(0L)
                .slaComplianceRate(100.0)
                .averageResolutionTimeHours(0.0)
                .startDate(LocalDate.of(2026, 2, 1))
                .endDate(LocalDate.of(2026, 2, 7))
                .seriesJson("[]")
                .metricsJson("{}")
                .build();
        entityManager.persist(report);
        entityManager.flush();
        return report;
    }

}
