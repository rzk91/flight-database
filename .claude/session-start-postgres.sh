#!/bin/bash
# Sandbox-only bootstrap: this remote Claude Code environment can't run a Docker
# daemon, so it can't use docker/docker-compose.yml like a real dev setup would.
# This mirrors the same canonical credentials (docker/docker/flightdb, see
# docker/.env.example) against a native `postgresql` service instead, and feeds
# them to the app via local.conf, since reference.conf no longer has defaults.
set -euo pipefail

service postgresql start

printf '#!/bin/bash\nexec /usr/bin/sbt --jvm-client "$@"\n' > /usr/local/bin/sbt
chmod +x /usr/local/bin/sbt

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
