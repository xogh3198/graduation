package com.project.graduation.service.iot;

import com.project.graduation.domain.plant.Plant;
import com.project.graduation.dto.iot.ControlCommandMqttPayload;

public interface IotControlPublisher {

    void publishCommand(Plant plant, ControlCommandMqttPayload payload);
}
