#!/bin/bash
# Entry point for Nimay OMP Bridge i containeren.
#
# Genererer config.json ved oppstart hvis den mangler, og starter deretter
# broen med de argumentene den ble kalt med.
#
# Broen leser config.json fra /opt/nimay-omp-bridge (HERE). For at
# tokenet skal overleve omstarter, ligger config.json pa en bind-mount
# fra verten. Derfor skriver vi til mounten, og bruker en symlink slik at
# broen finner den pa vanlig plass.
set -e

BRIDGE_DIR="/opt/nimay-omp-bridge"
CONFIG="$BRIDGE_DIR/config.json"
TEMPLATE="$BRIDGE_DIR/config.template.json"

# Mounten over config.json maa ogsa ha malen, slik at vi kan lage
# config.json selv om bildet endres.
if [ ! -f "$TEMPLATE" ] && [ -f "$(dirname "$CONFIG")/config.template.json" ]; then
  cp "$(dirname "$CONFIG")/config.template.json" "$TEMPLATE" 2>/dev/null || true
fi

if [ ! -f "$CONFIG" ]; then
  PORT="${OMP_PORT:-7802}"
  TOKEN="$(head -c 24 /dev/urandom | od -An -tx1 | tr -d ' \n')"
  if [ -f "$TEMPLATE" ]; then
    # Skriv til mounten, slik at config.json overlever omstarter.
    if [ -w "$(dirname "$CONFIG")" ]; then
      sed -e "s/__PORT__/$PORT/" -e "s/__TOKEN__/$TOKEN/" "$TEMPLATE" \
        > "$(dirname "$CONFIG")/config.json.new"
      mv "$(dirname "$CONFIG")/config.json.new" "$CONFIG" 2>/dev/null || \
        cp "$(dirname "$CONFIG")/config.json.new" "$CONFIG"
      chmod 600 "$CONFIG" 2>/dev/null || true
      echo "[bridge] genererte config.json med nytt token (port $PORT)"
    fi
  fi
fi

exec "$@"
