package com.project.graduation;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.domain.plant.PlantRepository;
import com.project.graduation.domain.plant.PlantStatus;
import com.project.graduation.domain.report.DeathReport;
import com.project.graduation.domain.report.DeathReportRepository;
import com.project.graduation.domain.user.User;
import com.project.graduation.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication //spring test
public class GraduationApplication {

    public static void main(String[] args) {
        SpringApplication.run(GraduationApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(
            PlantRepository plantRepository,
            UserRepository userRepository,
            DeathReportRepository deathReportRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            User user = userRepository.findByEmail("test@example.com").orElseGet(() -> {
                User u = new User();
                u.setEmail("test@example.com");
                u.setPassword(passwordEncoder.encode("password123"));
                u.setName("테스트");
                u.setNickname("식물왕");
                return userRepository.save(u);
            });

            if (plantRepository.count() == 0) {
                Plant plant = new Plant();
                plant.setUserId(user.getId());
                plant.setDeviceId("pi-001");
                plant.setName("쑥쑥이");
                plant.setSpecies("방울토마토");
                plant.setAge(30);
                plant.setPlantedDate(LocalDate.now());
                plant.setLevel(1);
                plant.setStatus(PlantStatus.good);
                Plant saved = plantRepository.save(plant);

                DeathReport report = new DeathReport();
                report.setPlantId(saved.getId());
                report.setDeathDate(LocalDateTime.parse("2024-05-17T10:00:00"));
                report.setReason("과습으로 인한 뿌리 썩음");
                report.setDescription("토양 수분이 지속적으로 80%를 초과하여 뿌리가 호흡하지 못했습니다.");
                report.setTips("다음 식물을 키울 때는 물 빠짐이 좋은 흙을 사용하고, 겉흙이 마를 때만 급수하세요.");
                deathReportRepository.save(report);

                System.out.println("✅ 테스트용 사용자/식물 데이터가 생성되었습니다.");
            }
        };
    }
}
