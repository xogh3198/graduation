package com.project.graduation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Plant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // MVP 단계에서는 간단히 userId로 매핑
    
    private String deviceId; // 라즈베리파이 기기 ID (MQTT 토픽과 매핑됨)
    
    private String name;    // 식물 애칭 (예: 쑥쑥이)
    private String species; // 식물 종류 (예: 방울토마토, 바질)
    
    private LocalDate plantedDate; // 심은 날짜
    private Integer level = 1;     // 생장 단계 (경험치 기반)
    private String status = "HAPPY"; // 감정/건강 상태 (HAPPY, THIRSTY, SICK 등)

    public void addExperience() {
        this.level += 1;
    }
}