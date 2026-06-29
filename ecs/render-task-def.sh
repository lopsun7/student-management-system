#!/usr/bin/env bash

set -euo pipefail

: "${ACCOUNT_ID:?Set ACCOUNT_ID}"
: "${REGION:?Set REGION}"
: "${RDS_ENDPOINT:?Set RDS_ENDPOINT}"
: "${DB_USERNAME:?Set DB_USERNAME}"
: "${DB_PASSWORD:?Set DB_PASSWORD}"

sed \
  -e "s|<ACCOUNT_ID>|${ACCOUNT_ID}|g" \
  -e "s|<REGION>|${REGION}|g" \
  -e "s|<RDS_ENDPOINT>|${RDS_ENDPOINT}|g" \
  -e "s|<DB_USERNAME>|${DB_USERNAME}|g" \
  -e "s|<DB_PASSWORD>|${DB_PASSWORD}|g" \
  ecs/task-definition.template.json > ecs/task-definition.rendered.json

echo "Rendered ecs/task-definition.rendered.json"
