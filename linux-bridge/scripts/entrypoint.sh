#!/bin/bash
# Entry point for Nimay OMP Bridge i containeren.
#
# Genererer config.json ved oppstart hvis den mangler, og starter deretter
# broen med de argumentene den ble kalt med.
#
# Config-stien kan overstyres med OMP_CONFIG_DIR. Da ligger config.json
# pa en bind-mount fra verten, og tokenet overlever omstarter. Uten
# overstyring ligger config.json i containerens lag, og hver omstart
# genererer et nytt token - paringen i appen gaar da tapt.
set -e

BRIDGE_DIR="/opt/nimay-omp-bridge"
CONFIG_DIR="${OMP_CONFIG_DIR:-$BRIDGE_DIR}"
CONFIG="$CONFIG_DIR/config.json"
TEMPLATE="$BRIDGE_DIR/config.template.json"

if [ ! -f "$CONFIG" ] && [ -f "$TEMPLATE" ]; then
  mkdir -p "$CONFIG_DIR"
  PORT="${OMP_PORT:-7802}"
  TOKEN="$(head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n')"
  sed -e "s/__PORT__/$PORT/" -e "s/__TOKEN__/$TOKEN/" "$TEMPLATE" > "$CONFIG"
  chmod 600 "$CONFIG"
  echo "[bridge] genererte $CONFIG med nytt token"
fi

exec "$@"
