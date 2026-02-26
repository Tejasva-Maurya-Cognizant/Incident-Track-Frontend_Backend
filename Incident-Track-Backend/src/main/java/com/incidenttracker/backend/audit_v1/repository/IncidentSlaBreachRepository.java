package com.incidenttracker.backend.audit_v1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.incidenttracker.backend.audit_v1.entity.IncidentSlaBreach;

import java.util.Optional;

public interface IncidentSlaBreachRepository extends JpaRepository<IncidentSlaBreach, Long> {
    Optional<IncidentSlaBreach> findByIncident_IncidentId(Long incidentId);

    boolean existsByIncident_IncidentId(Long incidentId);

    // ---- Pageable versions ----
    Page<IncidentSlaBreach> findAll(Pageable pageable);
}
