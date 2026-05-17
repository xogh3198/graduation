package com.project.graduation.domain.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeathReportRepository extends JpaRepository<DeathReport, Long> {
    Optional<DeathReport> findByPlantId(Long plantId);
}
