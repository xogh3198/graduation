package com.project.graduation.util;

import com.project.graduation.domain.sensor.HealthStatus;

public final class SensorStatusEvaluator {

    private SensorStatusEvaluator() {
    }

    public static HealthStatus evaluateMoisture(Double value) {
        if (value == null) return HealthStatus.warning;
        if (value < 20 || value > 80) return HealthStatus.critical;
        if (value < 30 || value > 70) return HealthStatus.warning;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateTemperature(Double value) {
        if (value == null) return HealthStatus.warning;
        if (value < 10 || value > 35) return HealthStatus.critical;
        if (value < 15 || value > 30) return HealthStatus.warning;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateLight(Double value) {
        if (value == null) return HealthStatus.warning;
        if (value < 1000) return HealthStatus.critical;
        if (value < 3000) return HealthStatus.warning;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateSoilMoisture(Double value) {
        if (value == null) return HealthStatus.warning;
        if (value < 20) return HealthStatus.critical;
        if (value < 30) return HealthStatus.warning;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateSoil(String soilStatus) {
        if (soilStatus == null) return HealthStatus.warning;
        if (soilStatus.contains("건조") || soilStatus.contains("과습")) return HealthStatus.warning;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateBug(Boolean hasBug) {
        if (Boolean.TRUE.equals(hasBug)) return HealthStatus.critical;
        return HealthStatus.good;
    }

    public static HealthStatus evaluateDisease(String disease) {
        if (disease == null || disease.isBlank() || "없음".equals(disease)) return HealthStatus.good;
        return HealthStatus.critical;
    }
}
