#!/usr/bin/env python3
"""
사진 업로드 MQTT 플로우 (라즈베리파이)

1. plants/{plantId}/photo/request  publish  → Presigned URL 요청
2. plants/{plantId}/photo/response subscribe → uploadUrl 수신
3. uploadUrl 로 HTTP PUT → 종료 (추가 MQTT 없음)

사용:
  pip install paho-mqtt requests
  cp mqtt.env.example mqtt.env   # 값 수정
  python photo_upload_mqtt.py
"""

from __future__ import annotations

import json
import os
import sys
import threading
import time
from pathlib import Path

import paho.mqtt.client as mqtt
import requests

ENV_FILE = Path(__file__).with_name("mqtt.env")


def load_env(path: Path) -> dict[str, str]:
    if not path.exists():
        print(f"설정 파일 없음: {path}", file=sys.stderr)
        print("  cp mqtt.env.example mqtt.env 후 값을 수정하세요.", file=sys.stderr)
        sys.exit(1)

    env: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        key, _, value = line.partition("=")
        env[key.strip()] = value.strip()
    return env


def build_photo_request_payload(env: dict[str, str]) -> dict:
    return {
        "plantId": env["PLANT_ID"],
        "deviceId": env["DEVICE_ID"],
        "contentType": env.get("PHOTO_CONTENT_TYPE", "image/jpeg"),
        "fileName": Path(env.get("PHOTO_FILE", "photo.jpg")).name,
    }


def upload_to_s3(upload_url: str, photo_path: Path, content_type: str) -> None:
    with photo_path.open("rb") as f:
        resp = requests.put(
            upload_url,
            data=f,
            headers={"Content-Type": content_type},
            timeout=60,
        )
    resp.raise_for_status()
    print(f"S3 업로드 완료 status={resp.status_code} file={photo_path}")


def main() -> None:
    env = load_env(ENV_FILE)

    plant_id = env["PLANT_ID"]
    photo_path = Path(env.get("PHOTO_FILE", "photo.jpg"))
    content_type = env.get("PHOTO_CONTENT_TYPE", "image/jpeg")

    request_topic = env["MQTT_TOPIC_PHOTO_REQUEST"]
    response_topic = env["MQTT_TOPIC_PHOTO_RESPONSE"]

    if photo_path != Path(f"./{photo_path.name}") and not photo_path.is_absolute():
        photo_path = Path(__file__).parent / photo_path
    if not photo_path.exists():
        print(f"사진 파일 없음: {photo_path}", file=sys.stderr)
        sys.exit(1)

    done = threading.Event()
    upload_result: dict | None = None

    def on_connect(client, userdata, flags, reason_code, properties=None):
        if reason_code != 0:
            print(f"MQTT 연결 실패 reason_code={reason_code}", file=sys.stderr)
            done.set()
            return
        print(f"MQTT 연결 OK, response 구독: {response_topic}")
        client.subscribe(response_topic, qos=1)

    def on_message(client, userdata, msg):
        nonlocal upload_result
        payload = json.loads(msg.payload.decode("utf-8"))
        print(f"response 수신 topic={msg.topic} payload={json.dumps(payload, ensure_ascii=False)}")

        if payload.get("error"):
            print(f"서버 오류: {payload['error']}", file=sys.stderr)
            done.set()
            return

        upload_url = payload.get("uploadUrl")
        if not upload_url:
            print("uploadUrl 없음", file=sys.stderr)
            done.set()
            return

        resp_content_type = payload.get("contentType") or content_type
        try:
            upload_to_s3(upload_url, photo_path, resp_content_type)
            upload_result = payload
        except Exception as e:
            print(f"S3 PUT 실패: {e}", file=sys.stderr)
        finally:
            done.set()

    client = mqtt.Client(
        mqtt.CallbackAPIVersion.VERSION2,
        client_id=env.get("AWS_IOT_CLIENT_ID", env["DEVICE_ID"]),
        protocol=mqtt.MQTTv311,
    )
    client.tls_set(
        ca_certs=env["AWS_IOT_ROOT_CA"],
        certfile=env["AWS_IOT_CERT"],
        keyfile=env["AWS_IOT_PRIVATE_KEY"],
    )
    client.on_connect = on_connect
    client.on_message = on_message

    endpoint = env["AWS_IOT_ENDPOINT"]
    print(f"AWS IoT 연결: ssl://{endpoint}:8883")
    client.connect(endpoint, 8883, keepalive=60)
    client.loop_start()

    time.sleep(1)
    request_payload = build_photo_request_payload(env)
    print(f"request publish topic={request_topic} payload={json.dumps(request_payload, ensure_ascii=False)}")
    client.publish(request_topic, json.dumps(request_payload), qos=1)

    if not done.wait(timeout=30):
        print("응답 타임아웃 (30초)", file=sys.stderr)
        sys.exit(1)

    client.loop_stop()
    client.disconnect()

    if upload_result:
        print(f"imageUrl (업로드 후 접근 URL): {upload_result.get('imageUrl')}")
        sys.exit(0)
    sys.exit(1)


if __name__ == "__main__":
    main()
