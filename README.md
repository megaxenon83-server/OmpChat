# OmpChat

**Mobilklient for Oh My Pi (OMP) AI-agenten på PC-en din.**

OmpChat kjører ikke modeller selv og snakker ikke direkte med Ollama. Den snakker med en liten bridge på PC-en som driver `omp --mode rpc` i prosjektmappene dine og strømmer svarene til telefonen.

[![Version](https://img.shields.io/badge/version-2.9.46-blue)](https://github.com/megaxenon83-server/OmpChat/releases)
[![Platform](https://img.shields.io/badge/platform-Android%207.0%2B-green)](https://github.com/megaxenon83-server/OmpChat/releases)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

---

## Funksjoner

- **Chat med OMP** — full samtale med verktøy, godkjenninger og streaming
- **Terminal** — xterm.js-basert terminal via WebViewAssetLoader (ingen CDN)
- **Multi-PC** — støtte for flere PC-er med separate profiler og tokens
- **LAN + Tailscale** — automatisk failover mellom LAN og Tailscale-adresser
- **QR-pairing** — skann QR-kode fra PC for rask tilkobling
- **Premium UI** — mørkt/lyst tema, token-indikator, aktivitetsvisning
- **Voice** — tale-til-tekst og tekst-til-tale
- **Wake-on-LAN** — vekk PC-en fra telefonen
- **Gotify** — push-varsler ved ferdige oppgaver

## Arkitektur

```
Telefon (Compose)  →  HTTP + SSE  →  bridge/omp-bridge.mjs  →  NDJSON  →  omp --mode rpc
       ↕                                    ↕                              ↕
  Keystore (token)                    config.json                   prosjektmappe
```

- `bridge/omp-bridge.mjs` starter én OMP-prosess per prosjektmappe (maks 4, inaktive avvikles)
- Eksponerer REST + SSE med bearer-token
- Appen eier ingen samtaler — historikk, titler og verktøykall kommer fra OMP
- Godkjenninger fra OMP sendes til appen som ark med Godkjenn / Godkjenn skriving / Avslå

## Kom i gang

### 1. Installer Bridge på PC-en

**Windows:**
```powershell
# Last ned og kjør NimayOMP-Installer-3.2.0.exe
# Installerer Bridge, OMP-kommandoer, brannmurregel og auto-start
```

**Linux:**
```bash
# Installer Flatpak-pakken
flatpak install net.nimay.ompbridge.flatpak
```

### 2. Installer appen på telefonen

Last ned `OmpChat-2.9.46.apk` fra [Releases](https://github.com/megaxenon83-server/OmpChat/releases) og installer.

### 3. Koble til

1. Kjør `/ompqr` i OMP for å generere QR-kode
2. Åpne OmpChat-appen → skann QR-koden
3. Appen kobler seg automatisk til Bridge

## Bygging

### Android-app

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Tester
./gradlew testDebugUnitTest
```

**Krav:** Android SDK 34, Kotlin 2.0, JDK 17

### Windows-installer

```powershell
# Bygg med Inno Setup
iscc NimayOMP-Installer.iss
```

**Krav:** Inno Setup 6.x, PowerShell 5.1+

## Prosjektstruktur

```
OmpChat/
├── app/                    # Android-app (Kotlin/Compose)
│   ├── src/main/java/      # Kildekode
│   ├── src/main/assets/    # Terminal (xterm.js, lokal)
│   └── src/test/           # Unit-tester
├── bridge/                 # Node.js bridge (runtime)
├── installer/              # Windows installer (Inno Setup)
├── omp-commands/           # OMP-kommandoer (/ompserver, /ompqr)
├── scripts/                # Linux service scripts
├── tools/                  # Hjelpeverktøy
├── wol-service/            # Wake-on-LAN (Docker)
├── .github/                # GitHub Actions
└── docs/                   # Dokumentasjon
```

## Sikkerhet

- Token lagres i Android Keystore (AES/GCM) per PC-profil
- Bridge krever bearer-token for alle endepunkter
- `/api/ping` er offentlig (for tilgjengelighetssjekk)
- `/api/health` krever gyldig token
- Ingen passord eller tokens i klartekst i app eller config

## Feilsøking

| Problem | Løsning |
|---------|---------|
| "Kan ikke koble til" | Sjekk at Bridge kjører (`/ompserver` i OMP) |
| "Token avvist" | Re-pair via QR-kode (`/ompqr` i OMP) |
| Port 7777 blokkert | Sjekk brannmur (TCP 7777) |
| App krasjer | Sjekk logcat for stack trace |

## Versjonshistorikk

Se [CHANGELOG.md](CHANGELOG.md) for full versjonshistorikk.

## Lisens

MIT License — se [LICENSE](LICENSE) for detaljer.

## Bidrag

1. Fork repoet
2. Opprett en branch (`git checkout -b feature/min-funksjon`)
3. Commit endringer (`git commit -m "Legg til min funksjon"`)
4. Push (`git push origin feature/min-funksjon`)
5. Opprett Pull Request
