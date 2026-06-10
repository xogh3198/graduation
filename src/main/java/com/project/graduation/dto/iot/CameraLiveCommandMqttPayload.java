package com.project.graduation.dto.iot;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.project.graduation.dto.cam.KvsTokenResponse;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CameraLiveCommandMqttPayload {

    private final String messageType = "camera_live";
    private final String action;
    private final String plantId;
    private final String deviceId;
    private final KvsCredentials kvs;

    private CameraLiveCommandMqttPayload(String action, String plantId, String deviceId, KvsCredentials kvs) {
        this.action = action;
        this.plantId = plantId;
        this.deviceId = deviceId;
        this.kvs = kvs;
    }

    public static CameraLiveCommandMqttPayload start(String plantId, String deviceId, KvsTokenResponse masterToken) {
        return new CameraLiveCommandMqttPayload(
                "start",
                plantId,
                deviceId,
                KvsCredentials.from(masterToken)
        );
    }

    public static CameraLiveCommandMqttPayload stop(String plantId, String deviceId) {
        return new CameraLiveCommandMqttPayload("stop", plantId, deviceId, null);
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KvsCredentials {
        private final String accessKeyId;
        private final String secretAccessKey;
        private final String sessionToken;
        private final String region;
        private final String channelName;
        private final String expiration;
        private final String role;

        private KvsCredentials(
                String accessKeyId,
                String secretAccessKey,
                String sessionToken,
                String region,
                String channelName,
                String expiration,
                String role) {
            this.accessKeyId = accessKeyId;
            this.secretAccessKey = secretAccessKey;
            this.sessionToken = sessionToken;
            this.region = region;
            this.channelName = channelName;
            this.expiration = expiration;
            this.role = role;
        }

        static KvsCredentials from(KvsTokenResponse token) {
            return new KvsCredentials(
                    token.getAccessKeyId(),
                    token.getSecretAccessKey(),
                    token.getSessionToken(),
                    token.getRegion(),
                    token.getChannelName(),
                    token.getExpiration(),
                    token.getRole()
            );
        }
    }
}
