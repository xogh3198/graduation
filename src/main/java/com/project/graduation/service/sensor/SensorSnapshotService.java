package com.project.graduation.service.sensor;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.dto.ai.VisionAnalyzeResponse;
import com.project.graduation.util.VisionResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorSnapshotService {

    private final SensorDataRepository sensorDataRepository;

    @Transactional
    public void applyVisionResult(Plant plant, VisionAnalyzeResponse vision) {
        SensorData latest = sensorDataRepository.findFirstByPlantIdOrderByTimestampDesc(plant.getId())
                .orElse(null);

        SensorData snapshot = new SensorData();
        snapshot.setPlantId(plant.getId());
        snapshot.setDeviceId(plant.getDeviceId());
        snapshot.setTopic("ai/vision/" + plant.getId());

        if (latest != null) {
            snapshot.setMoisture(latest.getMoisture());
            snapshot.setSoilMoisture(latest.getSoilMoisture());
            snapshot.setTemperature(latest.getTemperature());
            snapshot.setLight(latest.getLight());
        } else {
            snapshot.setMoisture(35.0);
            snapshot.setSoilMoisture(35.0);
            snapshot.setTemperature(22.0);
            snapshot.setLight(12000.0);
        }

        snapshot.setHasBug(Boolean.TRUE.equals(vision.getBug()));
        snapshot.setDisease(VisionResultMapper.normalizeDisease(vision.getDisease()));
        snapshot.setSoilStatus(VisionResultMapper.mapSoilStatus(vision,
                latest != null ? latest.getSoilStatus() : null));

        sensorDataRepository.save(snapshot);
        log.info("AI vision → SensorData 반영 plantId={}, bug={}, disease={}, soil={}",
                plant.getId(), snapshot.getHasBug(), snapshot.getDisease(), snapshot.getSoilStatus());
    }
}
