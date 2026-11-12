#!/usr/bin/env bash
# Nimay OMP Bridge — standalone installer for any Linux distro.
# Usage: sudo ./install.sh [--user NAME] [--port N] [--voice]
#
# For immutable OS (Bazzite, Silverblue, uBlue, uCore) brukes
# Quadlet-varianten, som kjører broen i en Podman-container:
#   sudo ./install.sh --container
set -euo pipefail

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
INSTALL_DIR="/opt/nimay-omp-bridge"
PAYLOAD="$HERE/payload"

USE_CONTAINER=false
for arg in "$@"; do
  [[ "$arg" == "--container" ]] && USE_CONTAINER=true
done

if [[ $EUID -ne 0 ]]; then
  echo "ERROR: run as root (sudo $0)" >&2
  exit 1
fi

# --- ensure Node.js >= 18 -----------------------------------------------------
need_node=true
if command -v node >/dev/null 2>&1; then
  major="$(node -e 'console.log(process.versions.node.split(".")[0])')"
  (( major >= 18 )) && need_node=false
fi

if [[ "$need_node" == "true" ]]; then
  echo "[info] installing Node.js 20..."
  if command -v apt-get >/dev/null 2>&1; then
    curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
    apt-get install -y nodejs
  elif command -v dnf >/dev/null 2>&1; then
    curl -fsSL https://rpm.nodesource.com/setup_20.x | bash -
    dnf install -y nodejs
  else
    echo "ERROR: unsupported distro; install Node.js >= 18 manually" >&2
    exit 1
  fi
fi

# --- copy payload -------------------------------------------------------------
echo "[info] installing files to $INSTALL_DIR"
mkdir -p "$INSTALL_DIR"
cp -r "$PAYLOAD/bridge/." "$INSTALL_DIR/"
cp -r "$PAYLOAD/tools" "$INSTALL_DIR/" 2>/dev/null || true
cp -r "$PAYLOAD/voice" "$INSTALL_DIR/" 2>/dev/null || true
cp "$PAYLOAD/nimay-omp-bridge.service" "$INSTALL_DIR/"
# configure.sh trenger write-omp-bin.sh ved siden av seg for aa kunne
# peke broen pa riktig omp-binary.
mkdir -p "$INSTALL_DIR/scripts"
cp -r "$HERE/scripts/." "$INSTALL_DIR/scripts/" 2>/dev/null || true

# --- configure + enable -------------------------------------------------------
if [[ "$USE_CONTAINER" == "true" ]]; then
  # Quadlet-varianten for immutable OS (Bazzite, Silverblue, uBlue, uCore).
  if ! command -v podman >/dev/null 2>&1; then
    echo "ERROR: podman er påkrevd for --container" >&2
    exit 1
  fi
  echo "[info] henter container-image..."
  # Det publiserte image-et hentes narere. Kun hvis det ikke finnes,
  # bygges det lokalt fra Containerfile.
  if ! podman pull ghcr.io/megaxenon83-server/omp-bridge:latest 2>/dev/null; then
    echo "[info] publisert image kunne ikke hentes, bygger lokalt..."
    podman build -t ghcr.io/megaxenon83-server/omp-bridge:latest -f "$HERE/Containerfile" "$HERE"
  else
    echo "[info] hentet ghcr.io/megaxenon83-server/omp-bridge:latest"
  fi

  CONTAINER_DIR="/etc/containers/systemd"
  mkdir -p "$CONTAINER_DIR"
  cp "$HERE/nimay-omp-bridge.container" "$CONTAINER_DIR/"

  # Brukernavn bestemmes av configure.sh, slik at native og container
  # installasjon velger samme bruker.
  CONTAINER_USER="$(bash "$HERE/scripts/configure.sh" --print-user "$@" 2>/dev/null || true)"
  if [[ -z "$CONTAINER_USER" ]]; then
    CONTAINER_USER="${SUDO_USER:-$(awk -F: '$3>=1000 && $3<65534 && $7 !~ /(nologin|false)/ {print $1; exit}' /etc/passwd)}"
  fi
  sed -i "s|<BRUKER>|$CONTAINER_USER|g" "$CONTAINER_DIR/nimay-omp-bridge.container"

  # ~/.omp maa vaere eid av UID 1000, ellers kan ikke broen skrive
  # agent-databasen der (EACCES ved mkdir).
  CONTAINER_HOME="$(getent passwd "$CONTAINER_USER" | cut -d: -f6)"
  mkdir -p "$CONTAINER_HOME/.omp"
  chown -R 1000:1000 "$CONTAINER_HOME/.omp"
  echo "[info] ~/.omp eies av UID 1000 for container-tilgang"

  # Advar om portkonflikt. Den native tjenesten holder port 7802, og
  # containeren kan ikke starte mens den kjører.
  if systemctl is-active --quiet nimay-omp-bridge 2>/dev/null; then
    echo "[info] stopper den native tjenesten (den holder port 7802)"
    systemctl stop nimay-omp-bridge
  fi

  systemctl daemon-reload
  systemctl enable --now nimay-omp-bridge
  echo
  echo "================ Nimay OMP Bridge (container) ================"
  echo "  Tjeneste:      nimay-omp-bridge (systemctl status nimay-omp-bridge)"
  echo "  Image:         nimay-omp-bridge:latest"
  echo "  Bruker:        $CONTAINER_USER"
  echo "  Port:          7802"
  echo
  echo "  Token:"
  podman exec nimay-omp-bridge node -e 'console.log(JSON.parse(require("fs").readFileSync("/opt/nimay-omp-bridge/config.json","utf8")).token)' 2>/dev/null | sed 's/^/    /'
  echo "==============================================================="
  exit 0
fi

bash "$HERE/scripts/configure.sh" "$@"
