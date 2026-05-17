package com.project.graduation.domain.report;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "death_reports")
@Getter
@Setter
@NoArgsConstructor
public class DeathReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long plantId;

    private LocalDateTime deathDate;

    private String reason;

    @Column(length = 2000)
    private String description;

    @Column(length = 2000)
    private String tips;
}
