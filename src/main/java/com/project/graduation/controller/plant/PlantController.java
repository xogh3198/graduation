package com.project.graduation.controller.plant;

import com.project.graduation.dto.plant.*;
import com.project.graduation.dto.common.StatusResponse;
import com.project.graduation.security.AuthUser;
import com.project.graduation.service.plant.PlantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreatePlantResponse> createPlant(@Valid @RequestBody CreatePlantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plantService.createPlant(AuthUser.getCurrentUserId(), request));
    }

    @PatchMapping("/{plantId}/name")
    public ResponseEntity<UpdatePlantNameResponse> updateName(
            @PathVariable Long plantId,
            @Valid @RequestBody UpdatePlantNameRequest request) {
        return ResponseEntity.ok(plantService.updateName(AuthUser.getCurrentUserId(), plantId, request));
    }

    @DeleteMapping("/{plantId}")
    public ResponseEntity<StatusResponse> deletePlant(@PathVariable Long plantId) {
        plantService.deletePlant(AuthUser.getCurrentUserId(), plantId);
        return ResponseEntity.ok(new StatusResponse("success"));
    }
}
