#!/usr/bin/env python3
"""
라즈베리파이 — KVS 라이브 송출 MQTT 핸들러 예시

구독: plants/plant-1/command
  - messageType=command + actuators → 급수/LED (기존)
  - messageType=camera_live + action=start|stop → 영상 송출

start 수신 시 kvs 필드의 credentials로 KVS WebRTC Master 실행.
stop 수신 시 Master 프로세스 종료.
"""
import json
import logging
import os
import subprocess
import sys

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# 실행 중인 KVS Master 프로세스 (예: kvsWebrtcClientMaster)
kvs_master_process = None


def handle_camera_live(payload: dict) -> None:
    global kvs_master_process

    action = payload.get("action")
    kvs = payload.get("kvs") or {}

    if action == "start":
        logger.info("라이브 송출 START — channel=%s", kvs.get("channelName"))
        stop_kvs_master()
        kvs_master_process = start_kvs_master(kvs)
        return

    if action == "stop":
        logger.info("라이브 송출 STOP")
        stop_kvs_master()
        return

    logger.warning("알 수 없는 camera_live action=%s", action)


def start_kvs_master(kvs: dict):
    """
    AWS KVS WebRTC C SDK 샘플 또는 자체 GStreamer 파이프라인 실행.
    환경변수로 credentials 전달 예시.
    """
    env = {
        **os.environ,
        "AWS_ACCESS_KEY_ID": kvs["accessKeyId"],
        "AWS_SECRET_ACCESS_KEY": kvs["secretAccessKey"],
        "AWS_SESSION_TOKEN": kvs["sessionToken"],
        "AWS_DEFAULT_REGION": kvs.get("region", "us-east-1"),
        "CHANNEL_NAME": kvs["channelName"],
    }
    # 실제 바이너리 경로로 교체
    cmd = ["./kvsWebrtcClientMaster", kvs["channelName"]]
    logger.info("KVS Master 실행: %s", cmd)
    return subprocess.Popen(cmd, env=env)


def stop_kvs_master() -> None:
    global kvs_master_process
    if kvs_master_process and kvs_master_process.poll() is None:
        kvs_master_process.terminate()
        try:
            kvs_master_process.wait(timeout=5)
        except subprocess.TimeoutExpired:
            kvs_master_process.kill()
        logger.info("KVS Master 종료")
    kvs_master_process = None


def on_mqtt_message(topic: str, raw_payload: str) -> None:
    payload = json.loads(raw_payload)
    message_type = payload.get("messageType")

    if message_type == "camera_live":
        handle_camera_live(payload)
    elif message_type == "command":
        logger.info("액추에이터 command: %s", payload.get("actuators"))
    else:
        logger.debug("무시 messageType=%s", message_type)


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python camera_live_handler_example.py '<json payload>'")
        sys.exit(1)
    on_mqtt_message("plants/plant-1/command", sys.argv[1])
