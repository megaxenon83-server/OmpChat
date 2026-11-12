#!/bin/bash
# Entry point for Nimay OMP Bridge i containeren.
#
# Genererer config.json ved oppstart hvis den mangler, og starter deretter
# broen med de argumentene den ble kalt med.
set -e

BRIDGE_DIR="/opt/nimay-omp-bridge"
CONFIG="$BRIDGE_DIR/config.json"
TEMPLATE="$BRIDGE_DIR/config.template.json"

if [ ! -f "$CONFIG" ] && [ -f "$TEMPLATE" ]; then
  PORT="${OMP_PORT:-7802}"
  TOKEN="$(head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n')"
  sed -e "s/__PORT__/$PORT/" -e "s/__TOKEN__/$TOKEN/" "$TEMPLATE" > "$CONFIG"
  chmod 600 "$CONFIG"
  echo "[bridge] genererte config.json med nytt token"
fi

exec "$@"
