package com.project.graduation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PlantRepository extends JpaRepository<Plant, Long> {
    // 유저 ID로 내 식물 목록 찾기
    List<Plant> findByUserId(Long userId);
}