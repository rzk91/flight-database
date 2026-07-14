#!/bin/bash
# Sandbox-only: wraps sbt to run in --jvm-client mode. This project's remote
# sandbox image ships sbt without that flag by default; a normal local
# terminal session doesn't need this at all.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

printf '#!/bin/bash\nexec /usr/bin/sbt --jvm-client "$@"\n' > /usr/local/bin/sbt
chmod +x /usr/local/bin/sbt
