#!/bin/bash
# Sandbox-only Postgres bootstrap. This remote Claude Code environment can't
# run a Docker daemon, so it can't use docker/docker-compose.yml like a real
# dev setup would. CLAUDE_CODE_REMOTE distinguishes that from a normal local
# terminal session, where this repo's .claude/settings.json also applies but
# where we must NOT touch a contributor's own Postgres setup.
#
# Provisions a native role/db with the same canonical credentials as
# docker/.env.example, then exports the matching DB_* vars via
# CLAUDE_ENV_FILE so sbt app/run picks them up exactly like it would from a
# real docker/.env — no sandbox-specific config file needed.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

service postgresql start

su postgres -c "psql -tAc \"SELECT 1 FROM pg_roles WHERE rolname='docker'\"" | grep -q 1 \
  || su postgres -c "psql -c \"CREATE ROLE docker WITH LOGIN SUPERUSER PASSWORD 'docker';\""

su postgres -c "psql -tAc \"SELECT 1 FROM pg_database WHERE datname='flightdb'\"" | grep -q 1 \
  || su postgres -c "psql -c \"CREATE DATABASE flightdb OWNER docker;\""

cat >> "$CLAUDE_ENV_FILE" <<'ENV'
export DB_USERNAME="docker"
export DB_PASSWORD="docker"
export DB_NAME="flightdb"
export DB_BASE_URL="jdbc:postgresql://localhost:5432"
ENV
