#!/bin/bash
# Skriver omp-bin.txt ved siden av broen, slik at den alltid finner den
# faktiske omp-binaryen uten aa vaere avhengig av PATH.
#
# Uten denne fila faller broen tilbake pa en bun-shim-sti
# (~/.bun/bin/omp.exe) som ikke finnes pa Linux, og alle spawn-kall
# feiler med ENOENT - akkurat som feilen pa Windows.
set -u

BRIDGE_DIR="${1:-/opt/nimay-omp-bridge}"
OUT="$BRIDGE_DIR/omp-bin.txt"

# Tjenestebrukerens home. configure.sh kjoeres ofte som root, og da er
# ~ root sin home, ikke brukerens. Et eksplisitt SERVICE_HOME har
# hoyest prioritet, saa kalleeren kan styre hvor omp slaas etter.
if [ -n "${SERVICE_HOME:-}" ]; then
    : # bruker den som er satt utenfor
elif [ -n "${SUDO_USER:-}" ]; then
    SERVICE_HOME="$(getent passwd "$SUDO_USER" | cut -d: -f6)"
else
    SERVICE_HOME="$HOME"
fi

# PATH som tjenesten faktisk faar. Den inkluderer brukerens ~/.local/bin
# og ~/.bun/bin, som er der omp og bun ligger etter npm-installasjon.
SERVICE_PATH="$SERVICE_HOME/.local/bin:$SERVICE_HOME/.bun/bin:/usr/local/bin:/usr/bin:/bin"

# Kandidater, mest sannsynlige foerst.
CANDIDATES=""
if command -v omp >/dev/null 2>&1; then
    CANDIDATES="$(command -v omp)"
fi
for candidate in \
    "$SERVICE_HOME/.local/bin/omp" \
    "$SERVICE_HOME/.bun/bin/omp" \
    "/usr/local/bin/omp" \
    "/usr/bin/omp"; do
    CANDIDATES="$CANDIDATES
$candidate"
done

# Velg foerste kandidat som faktisk KJOERER. En omp som ikke kan startes
# (f.eks. fordi bun mangler i PATH) er like verdtloest som en som mangler.
#
# omp-bin.txt kan ha to former:
#   /path/omp            - en vanlig kjørbar fil
#   /path/bun|/path/cli.js - et bun-skript, startet med bun
# Broen forstaer begge, og den andre forma er noedvendig for omp
# fra npm, siden cli.js har shebang "#!/usr/bin/env bun".
OMP=""
OMP_VALUE=""
while IFS= read -r candidate; do
    [ -z "$candidate" ] && continue
    [ -x "$candidate" ] || continue
    # omp er et bun-skript (shebang "#!/usr/bin/env bun"). Kjøres den
    # med node, gir den en feilmelding i stedet for versjonen. Derfor
    # testes den med bun når den er et skript, og ellers direkte.
    if head -1 "$candidate" 2>/dev/null | grep -q "bun"; then
        BUN="$(command -v bun 2>/dev/null)"
        [ -n "$BUN" ] || continue
        OUT_VER="$(PATH="$SERVICE_PATH:$PATH" "$BUN" "$candidate" --version 2>/dev/null | head -1)"
        if echo "$OUT_VER" | grep -q "^omp/"; then
            OMP="$BUN"
            OMP_VALUE="$BUN|$candidate"
            break
        fi
    else
        OUT_VER="$(PATH="$SERVICE_PATH:$PATH" "$candidate" --version 2>/dev/null | head -1)"
        if echo "$OUT_VER" | grep -q "^omp/"; then
            OMP="$candidate"
            OMP_VALUE="$candidate"
            break
        fi
    fi
done <<EOF
$CANDIDATES
EOF

if [ -z "$OMP" ]; then
    echo "Fant ingen brukbar omp. PATH som ble testet: $SERVICE_PATH" >&2
    echo "Installer med: npm install -g --prefix ~/.local @oh-my-pi/pi-coding-agent" >&2
    echo "Og bun: curl -fsSL https://bun.sh/install | bash" >&2
    exit 1
fi

printf '%s' "$OMP_VALUE" > "$OUT"
chmod 0644 "$OUT"
echo "Skrev $OUT: $OMP_VALUE"
