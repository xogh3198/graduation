package com.project.graduation.domain.sensor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {

    Optional<SensorData> findFirstByPlantIdOrderByTimestampDesc(Long plantId);

    List<SensorData> findByPlantIdAndTypeAndTimestampBetweenOrderByTimestampAsc(
            Long plantId, SensorType type, LocalDateTime start, LocalDateTime end);

    @Query("""
            SELECT s FROM SensorData s
            WHERE s.plantId = :plantId AND s.type IS NOT NULL
              AND s.timestamp BETWEEN :start AND :end
            ORDER BY s.timestamp ASC
            """)
    List<SensorData> findHistory(
            @Param("plantId") Long plantId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
