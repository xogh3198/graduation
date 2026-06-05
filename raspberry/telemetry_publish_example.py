#!/usr/bin/env python3
"""
센서 telemetry MQTT publish 예시 (라즈베리 → 서버)

토픽: plants/{plantId}/telemetry
"""

import json
import os
import sys
from datetime import datetime, timezone
from pathlib import Path

import paho.mqtt.client as mqtt

ENV_FILE = Path(__file__).with_name("mqtt.env")


def load_env(path: Path) -> dict[str, str]:
    env: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#"):
            continue
        key, _, value = line.partition("=")
        env[key.strip()] = value.strip()
    return env


def build_telemetry_payload(env: dict[str, str], lux: float, soil_moisture_pct: float) -> dict:
    return {
        "messageType": "telemetry",
        "plantId": env["PLANT_ID"],
        "deviceId": env["DEVICE_ID"],
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "sensors": {
            "lux": lux,
            "soilMoisturePct": soil_moisture_pct,
        },
    }


def main() -> None:
    if not ENV_FILE.exists():
        print(f"{ENV_FILE} 없음. cp mqtt.env.example mqtt.env", file=sys.stderr)
        sys.exit(1)

    env = load_env(ENV_FILE)
    topic = env["MQTT_TOPIC_TELEMETRY"]
    payload = build_telemetry_payload(env, lux=850.2, soil_moisture_pct=64.5)

    client = mqtt.Client(
        mqtt.CallbackAPIVersion.VERSION2,
        client_id=env.get("AWS_IOT_CLIENT_ID", env["DEVICE_ID"]),
    )
    client.tls_set(
        ca_certs=env["AWS_IOT_ROOT_CA"],
        certfile=env["AWS_IOT_CERT"],
        keyfile=env["AWS_IOT_PRIVATE_KEY"],
    )
    client.connect(env["AWS_IOT_ENDPOINT"], 8883, 60)
    client.loop_start()

    print(f"publish topic={topic}")
    print(json.dumps(payload, ensure_ascii=False, indent=2))
    client.publish(topic, json.dumps(payload), qos=1)
    client.loop_stop()
    client.disconnect()


if __name__ == "__main__":
    main()
