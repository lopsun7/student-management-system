#!/usr/bin/env bash
set -euo pipefail

: "${APP_NAME:?APP_NAME is required}"
: "${APP_DIR:?APP_DIR is required}"
: "${BUILD_NUMBER:?BUILD_NUMBER is required}"
: "${EC2_HOST:?EC2_HOST is required}"
: "${EC2_USER:?EC2_USER is required}"
: "${SPRING_PROFILE:?SPRING_PROFILE is required}"
: "${DOWNSTREAM_DEFAULT_NAME:?DOWNSTREAM_DEFAULT_NAME is required}"

BUILD_TARBALL="${APP_NAME}-${BUILD_NUMBER}.tar.gz"
REMOTE_TARBALL="/tmp/${BUILD_TARBALL}"
RELEASE_DIR="${APP_DIR}/releases/${BUILD_NUMBER}"

cleanup() {
	rm -f "${BUILD_TARBALL}"
}
trap cleanup EXIT

tar \
	--exclude='.git' \
	--exclude='target' \
	--exclude="${BUILD_TARBALL}" \
	-czf "${BUILD_TARBALL}" .

scp -o StrictHostKeyChecking=no "${BUILD_TARBALL}" "${EC2_USER}@${EC2_HOST}:${REMOTE_TARBALL}"

ssh -o StrictHostKeyChecking=no "${EC2_USER}@${EC2_HOST}" \
	"APP_NAME='${APP_NAME}' \
	APP_DIR='${APP_DIR}' \
	RELEASE_DIR='${RELEASE_DIR}' \
	REMOTE_TARBALL='${REMOTE_TARBALL}' \
	EC2_USER='${EC2_USER}' \
	SPRING_PROFILE='${SPRING_PROFILE}' \
	DOWNSTREAM_DEFAULT_NAME='${DOWNSTREAM_DEFAULT_NAME}' \
	bash -s" <<'ENDSSH'
set -euo pipefail

sudo mkdir -p "${APP_DIR}" "${RELEASE_DIR}"
sudo tar xzf "${REMOTE_TARBALL}" -C "${RELEASE_DIR}"
sudo chown -R "${EC2_USER}:${EC2_USER}" "${RELEASE_DIR}"
rm -f "${REMOTE_TARBALL}"

sudo ln -sfnT "${RELEASE_DIR}" "${APP_DIR}/current"
cd "${APP_DIR}/current"

sudo docker build -t "${APP_NAME}" .
sudo docker rm -f "${APP_NAME}" || true
sudo docker run -d \
	--name "${APP_NAME}" \
	-p 8080:8080 \
	-e SPRING_PROFILES_ACTIVE="${SPRING_PROFILE}" \
	-e DOWNSTREAM_DEFAULT_NAME="${DOWNSTREAM_DEFAULT_NAME}" \
	"${APP_NAME}"

for attempt in 1 2 3 4 5 6 7 8 9 10; do
	if curl -fsS http://localhost:8080/actuator/health; then
		exit 0
	fi
	sleep 3
done

sudo docker logs --tail 100 "${APP_NAME}"
exit 1
ENDSSH
