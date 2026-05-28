package com.project.graduation.config;

import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.domain.sensor.SensorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 라즈베리파이 미연동 시 프론트 테스트용.
 * 식물 등록 API에 {@link #DEMO_DEVICE_ID} 를 넣으면 아래 센서 더미와 연결됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    /** 프론트 식물 등록 시 입력할 데모 기기 ID */
    public static final String DEMO_DEVICE_ID = "pi-demo-001";

    private final SensorDataRepository sensorDataRepository;

    @Value("${app.init-dummy-data:true}")
    private boolean initDummyData;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!initDummyData) {
            return;
        }
        if (sensorDataRepository.existsByDeviceId(DEMO_DEVICE_ID)) {
            log.info("데모 라즈베리 센서 더미가 이미 있습니다. deviceId={}", DEMO_DEVICE_ID);
            return;
        }

        seedDemoRaspberrySensorData();
        log.info("데모 라즈베리 센서 더미 생성 완료 — 식물 등록 시 deviceId: {}", DEMO_DEVICE_ID);
    }

    private void seedDemoRaspberrySensorData() {
        createLatestSnapshot(35.0, 22.0, 12000.0, "건조함", false, "없음");

        LocalDateTime base = LocalDateTime.now().minusDays(3);
        saveHistoryPoint(SensorType.moisture, "30", base);
        saveHistoryPoint(SensorType.moisture, "35", base.plusHours(12));
        saveHistoryPoint(SensorType.temperature, "21", base.plusDays(1));
        saveHistoryPoint(SensorType.temperature, "22", base.plusDays(1).plusHours(6));
        saveHistoryPoint(SensorType.light, "11000", base.plusDays(2));
        saveHistoryPoint(SensorType.light, "12000", base.plusDays(2).plusHours(8));
    }

    private void createLatestSnapshot(
            double moisture, double temperature, double light,
            String soilStatus, boolean hasBug, String disease) {
        SensorData data = new SensorData();
        data.setDeviceId(DEMO_DEVICE_ID);
        data.setTopic("device/sensor/" + DEMO_DEVICE_ID);
        data.setMoisture(moisture);
        data.setTemperature(temperature);
        data.setLight(light);
        data.setSoilStatus(soilStatus);
        data.setHasBug(hasBug);
        data.setDisease(disease);
        data.setTimestamp(LocalDateTime.now());
        sensorDataRepository.save(data);
    }

    private void saveHistoryPoint(SensorType type, String value, LocalDateTime timestamp) {
        SensorData data = new SensorData();
        data.setDeviceId(DEMO_DEVICE_ID);
        data.setTopic("device/sensor/" + DEMO_DEVICE_ID + "/history");
        data.setType(type);
        data.setValue(value);
        data.setTimestamp(timestamp);
        sensorDataRepository.save(data);
    }
}
