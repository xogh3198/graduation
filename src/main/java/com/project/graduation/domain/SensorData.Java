package com.project.graduation.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
public class SensorData {
    
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String deviceId;
    private String topic;       // 어디서 온 데이터인지 (예: device/sensor/livingroom)
    private Double temperature; // 온도
    private Double humidity;    // 습도    
    
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now(); // 저장 시 현재 시간 자동 입력
    }
}