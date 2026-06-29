#!/usr/bin/env bash

set -euo pipefail

IMAGE_NAME="${IMAGE_NAME:-student-management-system}"
CONTAINER_NAME="${CONTAINER_NAME:-student-management-system}"
HOST_PORT="${HOST_PORT:-8080}"

docker build -t "$IMAGE_NAME" .
docker rm -f "$CONTAINER_NAME" >/dev/null 2>&1 || true

docker run -d \
  --name "$CONTAINER_NAME" \
  -p "$HOST_PORT":8080 \
  -e SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-h2}" \
  -e DOWNSTREAM_BASE_URL="${DOWNSTREAM_BASE_URL:-http://18.216.74.156:8080}" \
  -e DOWNSTREAM_AGGREGATION_PATH="${DOWNSTREAM_AGGREGATION_PATH:-/name/aggregation}" \
  -e DOWNSTREAM_DEFAULT_NAME="${DOWNSTREAM_DEFAULT_NAME:-Steven}" \
  "$IMAGE_NAME"
