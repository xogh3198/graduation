#!/bin/bash
# EC2에서 수동 실행: RDS에 db-2ne1 생성
# 사용법: RDS_HOST=xxx RDS_PASSWORD=xxx ./scripts/create-rds-database.sh

set -euo pipefail

RDS_HOST="${RDS_HOST:?RDS_HOST required}"
RDS_PASSWORD="${RDS_PASSWORD:?RDS_PASSWORD required}"
RDS_DB_NAME="${RDS_DB_NAME:-db-2ne1}"

exists=$(docker run --rm \
  -e PGPASSWORD="${RDS_PASSWORD}" \
  postgres:16-alpine \
  psql -h "${RDS_HOST}" -U postgres -d postgres -tAc \
  "SELECT 1 FROM pg_database WHERE datname = '${RDS_DB_NAME}'")

if [ "$exists" = "1" ]; then
  echo "데이터베이스 '${RDS_DB_NAME}' 이미 존재합니다."
else
  docker run --rm \
    -e PGPASSWORD="${RDS_PASSWORD}" \
    postgres:16-alpine \
    psql -h "${RDS_HOST}" -U postgres -d postgres -v ON_ERROR_STOP=1 \
    -c "CREATE DATABASE \"${RDS_DB_NAME}\""
  echo "데이터베이스 '${RDS_DB_NAME}' 생성 완료"
fi
