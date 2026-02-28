package com.incidenttracker.backend.incident.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.incidenttracker.backend.common.enums.IncidentSeverity;
import com.incidenttracker.backend.common.enums.IncidentStatus;
import com.incidenttracker.backend.incident.entity.Incident;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
        List<Incident> findByReportedBy_UserId(Long UserId);

        List<Incident> findByReportedBy_UserIdAndStatus(Long UserId, IncidentStatus status);

        List<Incident> findByReportedBy_UserIdAndCalculatedSeverity(Long UserId, IncidentSeverity severity);

        List<Incident> findByReportedBy_UserIdAndUrgent(Long UserId, Boolean urgent);

        Optional<Incident> findByIncidentIdAndReportedBy_UserId(Long incidentId, Long UserId);

        Optional<Incident> findByIncidentIdAndCategory_Department_DepartmentId(Long incidentId, Long departmentId);

        List<Incident> findByCategory_Department_DepartmentId(Long departmentId);

        // -------------- Pageable versions ---------------------------------------
        Page<Incident> findByReportedBy_UserId(Long userId, Pageable pageable);

        Page<Incident> findByReportedBy_UserIdAndStatus(Long userId, IncidentStatus status, Pageable pageable);

        Page<Incident> findByReportedBy_UserIdAndCalculatedSeverity(Long userId, IncidentSeverity severity,
                        Pageable pageable);

        Page<Incident> findByReportedBy_UserIdAndUrgent(Long userId, Boolean urgent, Pageable pageable);

        Page<Incident> findAll(Pageable pageable);

        Page<Incident> findByCategory_Department_DepartmentId(Long departmentId, Pageable pageable);

        Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

        Page<Incident> findByStatusAndCategory_Department_DepartmentId(
                        IncidentStatus status, Long departmentId, Pageable pageable);

        // -------------- for audit and compliance -------------------------------------

        // Incidents that are not resolved/closed and SLA due time has passed
        @Query("""
                            select i from Incident i
                            where i.status <> com.incidenttracker.backend.common.enums.IncidentStatus.RESOLVED
                              and i.status <> com.incidenttracker.backend.common.enums.IncidentStatus.CANCELLED
                              and i.slaDueAt is not null
                              and i.slaBreached = false
                              and i.slaDueAt < :now
                        """)
        List<Incident> findSlaOverdueNotMarked(LocalDateTime now);

        // ----------------- for report -------------
        List<Incident> findByReportedDateBetween(LocalDateTime start, LocalDateTime end);

        List<Incident> findByReportedDateBetweenAndCategory_Department_DepartmentId(
                        LocalDateTime start, LocalDateTime end, Long departmentId);

}
