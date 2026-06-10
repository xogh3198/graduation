# Kinesis Video Streams (WebRTC) 라이브 영상

## 플로우 (프론트 클릭 트리거)

```
[프론트] POST /plants/{plantId}/cam/live/start  (JWT)
    │
    ├─► [백엔드] Master 토큰 발급
    │       MQTT publish → plants/plant-1/command
    │       { messageType: "camera_live", action: "start", kvs: { ... } }
    │
    └─► [백엔드] Viewer 토큰 → REST 응답 (프론트)
            프론트 KVS Viewer SDK 연결

[프론트] POST /plants/{plantId}/cam/live/stop  (JWT)
    │
    └─► MQTT publish → plants/plant-1/command
            { messageType: "camera_live", action: "stop" }
            Pi Master 송출 종료
```

## 사전 준비 (AWS)

1. 리전 **us-east-1**
2. KVS 시그널링 채널: `inha-capstone-07-pi-camera-channel`
3. IAM Role: master / viewer
4. EC2 Role: `sts:AssumeRole`
5. IoT 정책: `command` 토픽 publish/subscribe (기존 `iot:*` 정책이면 추가 불필요)

## 환경 변수 (EC2 / GitHub Secrets)

```env
AWS_KVS_ENABLED=true
AWS_KVS_REGION=us-east-1
AWS_KVS_CHANNEL_NAME=inha-capstone-07-pi-camera-channel
AWS_KVS_MASTER_ROLE_ARN=arn:aws:iam::842544789367:role/inha-capstone-07-kvs-master-role
AWS_KVS_VIEWER_ROLE_ARN=arn:aws:iam::842544789367:role/inha-capstone-07-kvs-viewer-role
AWS_IOT_ENABLED=true
```

## API (프론트)

### 라이브 시작 (메인)

```http
POST /api/v1/plants/{plantId}/cam/live/start
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "status": "success",
  "message": "라이브 송출을 시작했습니다. Viewer 토큰으로 시청하세요.",
  "viewer": {
    "accessKeyId": "...",
    "secretAccessKey": "...",
    "sessionToken": "...",
    "region": "us-east-1",
    "channelName": "inha-capstone-07-pi-camera-channel",
    "expiration": "...",
    "role": "VIEWER"
  }
}
```

### 라이브 중지

```http
POST /api/v1/plants/{plantId}/cam/live/stop
Authorization: Bearer {accessToken}
```

**응답:**
```json
{
  "status": "success",
  "message": "라이브 송출을 중지했습니다."
}
```

### Viewer 토큰 갱신 (1시간 만료 시, 송출 중)

```http
GET /api/v1/plants/{plantId}/cam/viewer-token
Authorization: Bearer {accessToken}
```

## MQTT (라즈베리 — command 토픽)

**구독:** `plants/plant-1/command` (기존 급수/LED와 동일)

### 송출 시작 (서버 → Pi)

```json
{
  "messageType": "camera_live",
  "action": "start",
  "plantId": "plant-1",
  "deviceId": "rpi4-001",
  "kvs": {
    "accessKeyId": "...",
    "secretAccessKey": "...",
    "sessionToken": "...",
    "region": "us-east-1",
    "channelName": "inha-capstone-07-pi-camera-channel",
    "expiration": "...",
    "role": "MASTER"
  }
}
```

### 송출 중지 (서버 → Pi)

```json
{
  "messageType": "camera_live",
  "action": "stop",
  "plantId": "plant-1",
  "deviceId": "rpi4-001"
}
```

Pi 예시 코드: `raspberry/camera_live_handler_example.py`

## 레거시 (선택, 자동 폴링 불필요)

- `GET /api/v1/camera/master-token` + `camera/token/request` MQTT  
  → 프론트 트리거 방식 사용 시 **불필요**
