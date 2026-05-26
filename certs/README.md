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

## MQTT 토픽 (라즈베리파이 → 메인 서버)

`plants/{plantId}/status/photo`

페이로드 예:

```json
{
  "plantId": "1",
  "imageUrl": "https://버킷.s3.amazonaws.com/plants/plant-1/photo.jpg"
}
```
