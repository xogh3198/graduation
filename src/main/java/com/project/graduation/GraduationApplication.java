package com.project.graduation;

import com.project.graduation.domain.Plant;
import com.project.graduation.domain.PlantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class GraduationApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraduationApplication.class, args);
	}

	// 테스트를 위한 더미 데이터 자동 생성 로직
	@Bean
	public CommandLineRunner initData(PlantRepository plantRepository) {
		return args -> {
			// DB에 식물이 하나도 없을 때만 실행
			if (plantRepository.count() == 0) {
				Plant plant = new Plant();
				plant.setUserId(1L);
				plant.setDeviceId("pi-001");
				plant.setName("쑥쑥이");
				plant.setSpecies("방울토마토");
				plant.setPlantedDate(LocalDate.now());
				plant.setLevel(1);
				plant.setStatus("HAPPY");
				
				plantRepository.save(plant);
				System.out.println("✅ 테스트용 1번 식물 데이터가 DB에 생성되었습니다!");
			}
		};
	}
}