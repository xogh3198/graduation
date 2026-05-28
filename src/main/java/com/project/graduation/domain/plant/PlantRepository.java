package com.project.graduation.domain.plant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    List<Plant> findByUserId(Long userId);
    Optional<Plant> findByIdAndUserId(Long id, Long userId);
    Optional<Plant> findByDeviceId(String deviceId);

    boolean existsByDeviceId(String deviceId);
}
