package com.project.graduation.util;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * plantId 규칙:
 * <ul>
 *   <li>DB PK: {@code Plant.id} (Long, 예: 1)</li>
 *   <li>외부/AI/MQTT 토픽: {@code plant-{id}} (예: plant-1)</li>
 *   <li>디바이스: {@code Plant.deviceId} (예: pi-001) — 라즈베리 MQTT 센서용</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class PlantIdResolver {

    public static final String EXTERNAL_PREFIX = "plant-";

    private final PlantRepository plantRepository;

    public String toExternalId(Long plantId) {
        if (plantId == null) {
            return null;
        }
        return EXTERNAL_PREFIX + plantId;
    }

    public Optional<Long> resolvePlantId(String plantRef) {
        return resolvePlant(plantRef).map(Plant::getId);
    }

    public Optional<Plant> resolvePlant(String plantRef) {
        if (plantRef == null || plantRef.isBlank()) {
            return Optional.empty();
        }
        String trimmed = plantRef.trim();

        if (trimmed.startsWith(EXTERNAL_PREFIX)) {
            String suffix = trimmed.substring(EXTERNAL_PREFIX.length());
            return parseLong(suffix).flatMap(plantRepository::findById);
        }

        Optional<Plant> byNumeric = parseLong(trimmed).flatMap(plantRepository::findById);
        if (byNumeric.isPresent()) {
            return byNumeric;
        }

        return plantRepository.findByDeviceId(trimmed);
    }

    private Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}
