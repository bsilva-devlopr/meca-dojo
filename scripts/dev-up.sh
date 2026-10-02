#!/usr/bin/env sh
set -eu

docker compose --project-name meca-dojo up -d postgres redis minio
