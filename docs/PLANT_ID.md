# plantId 규칙

프로젝트 전반에서 아래 규칙을 사용합니다.

| 용도 | 형식 | 예시 |
|------|------|------|
| DB PK | `Plant.id` (Long) | `1` |
| REST API path | Long (`/api/v1/plants/1`) | `1` |
| AI 서버 요청 | `plant-{id}` | `plant-1` |
| AWS IoT MQTT topic (사진 URL 요청) | `plants/plant-{id}/photo/request` | `plants/plant-1/photo/request` |
| AWS IoT MQTT topic (사진 URL 응답) | `plants/plant-{id}/photo/response` | `plants/plant-1/photo/response` |
| AWS IoT MQTT topic (사진 완료 알림, 선택) | `plants/plant-{id}/status/photo` | `plants/plant-1/status/photo` |
| AWS IoT MQTT topic (센서) | `plants/plant-{id}/telemetry` | `plants/plant-1/telemetry` |
| AWS IoT MQTT topic (제어) | `plants/plant-{id}/command` | `plants/plant-1/command` |
| 라즈베리 센서 MQTT | `device/sensor/{deviceId}` | `device/sensor/pi-001` |
| 디바이스 식별 | `Plant.deviceId` | `pi-001` |

## MQTT 사진 업로드 (Presigned URL — 권장)

라즈베리 설정 예시: [`raspberry/mqtt.env.example`](../raspberry/mqtt.env.example)  
실행 예시: [`raspberry/photo_upload_mqtt.py`](../raspberry/photo_upload_mqtt.py)

### 1) URL 요청 — Pi → 서버

- **Topic:** `plants/plant-1/photo/request`
- **QoS:** `1`
- **Payload:**
  ```json
  {
    "plantId": "plant-1",
    "deviceId": "rpi4-001",
    "contentType": "image/jpeg",
    "fileName": "photo.jpg"
  }
  ```
- `plantId`: 식물 등록 응답의 `externalPlantId` (예: `plant-1`)
- `deviceId`: 식물 등록 시 `deviceId` (예: `rpi4-001`)
- `contentType`: PUT 업로드 시 사용할 MIME (기본 `image/jpeg`)
- `fileName`: 참고용 (서버 키 생성에만 참고, 필수 아님)
- `plantId` 생략 시 topic 두 번째 세그먼트(`plant-1`) 사용

### 2) URL 응답 — 서버 → Pi (구독)

- **Topic:** `plants/plant-1/photo/response`
- **QoS:** `1`
- **Payload (성공):**
  ```json
  {
    "plantId": "plant-1",
    "uploadUrl": "https://bucket.s3.us-east-1.amazonaws.com/...?X-Amz-...",
    "bucket": "your-bucket",
    "s3Key": "plants/plant-1/photos/1717500000000-uuid.jpg",
    "contentType": "image/jpeg",
    "imageUrl": "https://your-bucket.s3.us-east-1.amazonaws.com/plants/plant-1/photos/....jpg",
    "expiresInSeconds": 900,
    "expiresAt": "2026-06-04T13:10:00Z"
  }
  ```
- **Payload (실패):**
  ```json
  {
    "plantId": "plant-1",
    "error": "등록되지 않은 plantId입니다: plant-1"
  }
  ```

### 3) S3 업로드 — Pi → S3 (MQTT 없음)

```bash
curl -X PUT -T photo.jpg \
  -H "Content-Type: image/jpeg" \
  "<uploadUrl>"
```

- `Content-Type`은 response의 `contentType`과 **동일**해야 함
- 업로드 후 **추가 MQTT 전송 없음**

## MQTT 사진 업로드 (S3 완료 알림 — 선택, AI 분석 트리거용)

Presigned URL 플로우만 쓰면 서버 AI 분석이 자동으로 안 돕니다.  
업로드 후 분석까지 필요하면 아래를 **추가** publish 하거나 S3 Event를 쓰세요.

- **Topic:** `plants/plant-1/status/photo`
- **Payload:**
  ```json
  {
    "plantId": "plant-1",
    "imageUrl": "https://bucket.s3.../plants/plant-1/photo.jpg"
  }
  ```
- `plantId` 생략 시 topic의 두 번째 세그먼트(`plant-1`) 사용

## MQTT 센서 텔레메트리 (AWS IoT Core)

- **Topic:** `plants/plant-1/telemetry`
- **Payload:**
  ```json
  {
    "messageType": "telemetry",
    "readingId": 123,
    "plantId": "plant-1",
    "deviceId": "rpi4-001",
    "timestamp": "2026-05-31T16:30:00+09:00",
    "sensors": {
      "lux": 850.2,
      "soilRaw": 18320,
      "soilVoltage": 1.82,
      "soilMoisturePct": 64.5,
      "temperatureC": null,
      "humidityPct": null
    },
    "actuators": {
      "growLedBrightnessPct": 0
    }
  }
  ```
- `lux` → `light`, `soilMoisturePct` → `soilMoisture`(%), `humidityPct` → `moisture`(%) 매핑
- `plantId` 또는 `deviceId`로 DB 식물 연결

## MQTT 액추에이터 제어 (AWS IoT Core, 서버 → 라즈베리)

- **Topic:** `plants/plant-1/command`
- **급수 API:** `POST /api/v1/plants/{plantId}/control/water`
  ```json
  { "amount": 100 }
  ```
  **응답:**
  ```json
  { "status": "success", "message": "100ml 급수 명령을 전송했습니다.", "amount": 100 }
  ```
- **조명 API:** `POST /api/v1/plants/{plantId}/control/led`
  ```json
  { "status": "ON" }
  ```
  **응답:**
  ```json
  { "status": "success", "message": "LED가 켜졌습니다.", "brightnessPct": 100 }
  ```
- **IoT publish payload (급수):**
  ```json
  {
    "messageType": "command",
    "plantId": "plant-1",
    "deviceId": "rpi4-001",
    "actuators": { "waterMl": 100 }
  }
  ```
- **IoT publish payload (조명 ON):**
  ```json
  {
    "messageType": "command",
    "plantId": "plant-1",
    "deviceId": "rpi4-001",
    "actuators": { "growLedBrightnessPct": 100 }
  }
  ```
- `telemetry` 토픽은 **센서 업링크 전용**, 제어는 **`command` 토픽** 사용

## 프론트 테스트용 데모 라즈베리 (센서만)

서버 기동 시 `pi-demo-001` 기기의 센서 더미가 들어갑니다 (`plant_id` 없음).

1. 회원가입·로그인 (본인 계정)
2. `POST /api/v1/plants` 에 `"deviceId": "pi-demo-001"` 로 식물 등록
3. `GET /api/v1/plants/{plantId}/sensors/latest` 로 데이터 조회

## 식물 등록 API

`POST /api/v1/plants`

```json
{
  "name": "닉네임",
  "species": "종류",
  "age": 30,
  "level": 1,
  "deviceId": "pi-001"
}
```

응답 예:

```json
{
  "plantId": 1,
  "status": "success",
  "deviceId": "pi-001",
  "externalPlantId": "plant-1"
}
```

- `deviceId`: 라즈베리 센서 MQTT `device/sensor/pi-001` 과 동일해야 함
- `externalPlantId`: S3 사진 MQTT `plants/plant-1/status/photo` 에 사용

## 메인 서버 처리

`PlantIdResolver`가 다음 순서로 식물을 찾습니다.

1. `plant-{숫자}` → `Plant.id`
2. 숫자만 (`1`) → `Plant.id`
3. `deviceId` (`pi-001`) → `Plant.deviceId`

AI 서버로 보낼 때는 항상 **`plant-{id}`** 형식으로 통일합니다.
