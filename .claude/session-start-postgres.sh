#!/bin/bash
# Sandbox-only Postgres bootstrap. This remote Claude Code environment can't
# run a Docker daemon, so it can't use docker/docker-compose.yml like a real
# dev setup would. CLAUDE_CODE_REMOTE distinguishes that from a normal local
# terminal session, where this repo's .claude/settings.json also applies but
# where we must NOT touch a contributor's own Postgres/local.conf.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

service postgresql start

su postgres -c "psql -tAc \"SELECT 1 FROM pg_roles WHERE rolname='docker'\"" | grep -q 1 \
  || su postgres -c "psql -c \"CREATE ROLE docker WITH LOGIN SUPERUSER PASSWORD 'docker';\""

su postgres -c "psql -tAc \"SELECT 1 FROM pg_database WHERE datname='flightdb'\"" | grep -q 1 \
  || su postgres -c "psql -c \"CREATE DATABASE flightdb OWNER docker;\""

mkdir -p /home/user/flight-database/modules/app/src/main/resources
cat > /home/user/flight-database/modules/app/src/main/resources/local.conf <<'CONF'
db-config {
  base-url = "jdbc:postgresql://localhost:5432"
  db-name  = "flightdb"
  access {
    username = "docker"
    password = "docker"
  }
}
CONF
