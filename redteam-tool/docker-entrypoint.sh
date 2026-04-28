#!/bin/sh
set -eu

CP="/app/target/classes:/app/src/main/resources"

# Si des arguments sont fournis, ils ont priorité.
if [ "$#" -gt 0 ]; then
  exec java -cp "$CP" fr.redteam.Main "$@"
fi

MODE="${APP_MODE:-gui}"

case "$MODE" in
  gui)
    exec java -cp "$CP" fr.redteam.Main gui
    ;;
  cli)
    exec java -cp "$CP" fr.redteam.Main
    ;;
  list)
    exec java -cp "$CP" fr.redteam.Main list
    ;;
  *)
    echo "APP_MODE invalide: $MODE (utiliser gui|cli|list)" >&2
    exit 1
    ;;
esac
