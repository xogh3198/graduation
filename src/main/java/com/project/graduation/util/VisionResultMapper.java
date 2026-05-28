package com.project.graduation.util;

import com.project.graduation.dto.ai.VisionAnalyzeResponse;

public final class VisionResultMapper {

    private VisionResultMapper() {
    }

    public static String normalizeDisease(String disease) {
        if (disease == null || disease.isBlank()) {
            return "없음";
        }
        if ("none".equalsIgnoreCase(disease) || "없음".equals(disease)) {
            return "없음";
        }
        return disease;
    }

    public static boolean hasActionableDisease(String disease) {
        String normalized = normalizeDisease(disease);
        return !"없음".equals(normalized);
    }

    public static String mapSoilStatus(VisionAnalyzeResponse vision, String previousSoil) {
        if (Boolean.TRUE.equals(vision.getBug())) {
            return "주의 필요";
        }
        if (vision.getStatus() != null) {
            return switch (vision.getStatus().toLowerCase()) {
                case "dry" -> "건조함";
                case "wet", "overwatered" -> "과습";
                default -> fallbackSoil(previousSoil, vision);
            };
        }
        return fallbackSoil(previousSoil, vision);
    }

    private static String fallbackSoil(String previousSoil, VisionAnalyzeResponse vision) {
        if (vision.getLevel() != null && vision.getLevel() <= 2) {
            return "건조함";
        }
        if (previousSoil != null && !previousSoil.isBlank()) {
            return previousSoil;
        }
        return "적정";
    }
}
