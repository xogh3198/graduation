package com.project.graduation.config;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.domain.report.DeathReport;
import com.project.graduation.domain.report.DeathReportRepository;
import com.project.graduation.domain.sensor.SensorData;
import com.project.graduation.domain.sensor.SensorDataRepository;
import com.project.graduation.domain.sensor.SensorType;
import com.project.graduation.domain.user.User;
import com.project.graduation.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PlantRepository plantRepository;
    private final SensorDataRepository sensorDataRepository;
    private final DeathReportRepository deathReportRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init-dummy-data:true}")
    private boolean initDummyData;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!initDummyData) {
            return;
        }
        if (userRepository.count() > 0) {
            log.info("더미 데이터가 이미 있어 초기화를 건너뜁니다.");
            return;
        }

        User user = createUser("test@example.com", "password123", "테스트", "식물왕");

        Plant plant1 = createPlant(user.getId(), "pi-001", "쑥쑥이", "방울토마토", 30, 3, PlantStatus.good);
        Plant plant2 = createPlant(user.getId(), "pi-002", "바질이", "바질", 45, 2, PlantStatus.warning);
        Plant plant3 = createPlant(user.getId(), "pi-003", "시든이", "로즈마리", 60, 1, PlantStatus.dead);

        createLatestSensorSnapshot(plant1.getId(), "pi-001", 35.0, 22.0, 12000.0, "건조함", false, "없음");
        createLatestSensorSnapshot(plant2.getId(), "pi-002", 55.0, 26.0, 8000.0, "적정", false, "없음");
        createLatestSensorSnapshot(plant3.getId(), "pi-003", 80.0, 28.0, 500.0, "과습", true, "흰가루병");

        createSensorHistory(plant1.getId(), "pi-001");
        createSensorHistory(plant2.getId(), "pi-002");

        createDeathReport(plant3.getId());

        log.info("✅ db-2ne1 더미 데이터 초기화 완료 (user: test@example.com / password123)");
    }

    private User createUser(String email, String rawPassword, String name, String nickname) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setName(name);
        user.setNickname(nickname);
        user.setFcmToken("dummy-fcm-token-" + email);
        return userRepository.save(user);
    }

    private Plant createPlant(
            Long userId, String deviceId, String name, String species,
            int age, int level, PlantStatus status) {
        Plant plant = new Plant();
        plant.setUserId(userId);
        plant.setDeviceId(deviceId);
        plant.setName(name);
        plant.setSpecies(species);
        plant.setAge(age);
        plant.setPlantedDate(LocalDate.now().minusDays(age));
        plant.setLevel(level);
        plant.setStatus(status);
        return plantRepository.save(plant);
    }

    private void createLatestSensorSnapshot(
            Long plantId, String deviceId,
            double moisture, double temperature, double light,
            String soilStatus, boolean hasBug, String disease) {
        SensorData data = new SensorData();
        data.setPlantId(plantId);
        data.setDeviceId(deviceId);
        data.setTopic("device/sensor/" + deviceId);
        data.setMoisture(moisture);
        data.setTemperature(temperature);
        data.setLight(light);
        data.setSoilStatus(soilStatus);
        data.setHasBug(hasBug);
        data.setDisease(disease);
        data.setTimestamp(LocalDateTime.now());
        sensorDataRepository.save(data);
    }

    private void createSensorHistory(Long plantId, String deviceId) {
        LocalDateTime base = LocalDateTime.now().minusDays(3);
        saveHistoryPoint(plantId, deviceId, SensorType.moisture, "30", base);
        saveHistoryPoint(plantId, deviceId, SensorType.moisture, "35", base.plusHours(12));
        saveHistoryPoint(plantId, deviceId, SensorType.temperature, "21", base.plusDays(1));
        saveHistoryPoint(plantId, deviceId, SensorType.temperature, "22", base.plusDays(1).plusHours(6));
        saveHistoryPoint(plantId, deviceId, SensorType.light, "11000", base.plusDays(2));
        saveHistoryPoint(plantId, deviceId, SensorType.light, "12000", base.plusDays(2).plusHours(8));
    }

    private void saveHistoryPoint(
            Long plantId, String deviceId, SensorType type, String value, LocalDateTime timestamp) {
        SensorData data = new SensorData();
        data.setPlantId(plantId);
        data.setDeviceId(deviceId);
        data.setTopic("device/sensor/" + deviceId + "/history");
        data.setType(type);
        data.setValue(value);
        data.setTimestamp(timestamp);
        sensorDataRepository.save(data);
    }

    private void createDeathReport(Long plantId) {
        DeathReport report = new DeathReport();
        report.setPlantId(plantId);
        report.setDeathDate(LocalDateTime.parse("2024-05-17T10:00:00"));
        report.setReason("과습으로 인한 뿌리 썩음");
        report.setDescription("토양 수분이 지속적으로 80%를 초과하여 뿌리가 호흡하지 못했습니다.");
        report.setTips("다음 식물을 키울 때는 물 빠짐이 좋은 흙을 사용하고, 겉흙이 마를 때만 급수하세요.");
        deathReportRepository.save(report);
    }
}
