# plantId 규칙

프로젝트 전반에서 아래 규칙을 사용합니다.

| 용도 | 형식 | 예시 |
|------|------|------|
| DB PK | `Plant.id` (Long) | `1` |
| REST API path | Long (`/api/v1/plants/1`) | `1` |
| AI 서버 요청 | `plant-{id}` | `plant-1` |
| AWS IoT MQTT topic | `plants/plant-{id}/status/photo` | `plants/plant-1/status/photo` |
| 라즈베리 센서 MQTT | `device/sensor/{deviceId}` | `device/sensor/pi-001` |
| 디바이스 식별 | `Plant.deviceId` | `pi-001` |

## MQTT 사진 업로드 (S3 완료 알림)

- **Topic:** `plants/plant-1/status/photo`
- **Payload:**
  ```json
  {
    "plantId": "plant-1",
    "imageUrl": "https://bucket.s3.../plants/plant-1/photo.jpg"
  }
  ```
- `plantId` 생략 시 topic의 두 번째 세그먼트(`plant-1`) 사용

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
