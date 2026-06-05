package com.project.graduation.domain.sensor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@Setter
@NoArgsConstructor
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long plantId;

    private String deviceId;

    private String topic;

    private Double moisture;

    private Double soilMoisture;

    private Double temperature;

    private Double light;

    private String soilStatus;

    private Boolean hasBug;

    private String disease;

    @Enumerated(EnumType.STRING)
    private SensorType type;

    private String value;

    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
