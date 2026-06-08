package com.project.graduation.domain.plant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "plants")
@Getter
@Setter
@NoArgsConstructor
public class Plant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private String deviceId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String species;

    private Integer age;

    private LocalDate plantedDate;

    private Integer level = 1;

    @Enumerated(EnumType.STRING)
    private PlantStatus status = PlantStatus.good;

    private Boolean autoWaterEnabled = false;

    private Double autoWaterThreshold;

    private Boolean autoLightEnabled = false;

    private Double autoLightThreshold;
}
