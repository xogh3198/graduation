# AWS IoT Core 인증서

Spring Boot가 IoT Core MQTT를 구독하려면 아래 3개 파일이 필요합니다.

1. `AmazonRootCA1.pem` — [AWS Root CA](https://www.amazontrust.com/repository/AmazonRootCA1.pem)
2. `xxxx-certificate.pem.crt` — IoT Core 콘솔에서 생성한 **사물(Thing) 인증서**
3. `xxxx-private.pem.key` — 위 인증서의 **Private key**

EC2 `/home/ec2-user/app/certs/` 에 두고 `.env`에 경로 설정:

```env
AWS_IOT_ENABLED=true
AWS_IOT_ENDPOINT=amtgq6rjeeepu-ats.iot.us-east-1.amazonaws.com
AWS_IOT_CERT_PATH=/app/certs/xxxx-certificate.pem.crt
AWS_IOT_PRIVATE_KEY_PATH=/app/certs/xxxx-private.pem.key
AWS_IOT_ROOT_CA_PATH=/app/certs/AmazonRootCA1.pem
AI_SERVER_BASE_URL=http://100.95.251.67:30800
```

## MQTT 토픽 (라즈베리파이 ↔ 메인 서버)

설정 파일: [`raspberry/mqtt.env.example`](../raspberry/mqtt.env.example)

| 방향 | 용도 | 토픽 | QoS |
|------|------|------|-----|
| Pi → 서버 | 센서 | `plants/plant-1/telemetry` | 1 |
| Pi → 서버 | 사진 URL 요청 | `plants/plant-1/photo/request` | 1 |
| 서버 → Pi | 사진 URL 응답 (구독) | `plants/plant-1/photo/response` | 1 |
| 서버 → Pi | 급수/LED 제어 (구독) | `plants/plant-1/command` | 1 |

### 사진 URL 요청 payload (Pi → 서버)

```json
{
  "plantId": "plant-1",
  "deviceId": "rpi4-001",
  "contentType": "image/jpeg",
  "fileName": "photo.jpg"
}
```

### 사진 URL 응답 payload (서버 → Pi)

```json
{
  "plantId": "plant-1",
  "uploadUrl": "https://...",
  "contentType": "image/jpeg",
  "imageUrl": "https://bucket.s3.us-east-1.amazonaws.com/...",
  "expiresInSeconds": 900
}
```

Pi는 `uploadUrl`로 HTTP PUT 후 종료. 상세: [docs/PLANT_ID.md](../docs/PLANT_ID.md)

## AWS IoT 정책 (라즈베리 Thing)

| 동작 | Resource |
|------|----------|
| `iot:Connect` | `*` |
| `iot:Publish` | `plants/plant-1/telemetry`, `plants/plant-1/photo/request` |
| `iot:Subscribe` | `plants/plant-1/photo/response`, `plants/plant-1/command` |
| `iot:Receive` | 위 subscribe 토픽과 동일 |

`plant-1`은 실제 `externalPlantId`로 바꿉니다.

---

## (구) 사진 완료 알림 — 선택

| Pi → 서버 | `plants/plant-1/status/photo` | `plantId`, `imageUrl` |

plantId 규칙 상세: [docs/PLANT_ID.md](../docs/PLANT_ID.md)
