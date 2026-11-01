# OmpChat

**Mobile client for the Oh My Pi (OMP) AI agent on your PC.**

OmpChat does not run models itself and does not talk directly to Ollama. It talks to a small bridge on the PC that drives `omp --mode rpc` in your project folders and streams responses to the phone.

[![Version](https://img.shields.io/badge/version-2.9.46-blue)](https://github.com/megaxenon83-server/OmpChat/releases)
[![Platform](https://img.shields.io/badge/platform-Android%207.0%2B-green)](https://github.com/megaxenon83-server/OmpChat/releases)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

---

## Features

- **Chat with OMP** — full conversation with tools, approvals, and streaming
- **Terminal** — xterm.js-based terminal via WebViewAssetLoader (no CDN)
- **Multi-PC** — support for multiple PCs with separate profiles and tokens
- **LAN + Tailscale** — automatic failover between LAN and Tailscale addresses
- **QR pairing** — scan QR code from PC for quick connection
- **Premium UI** — dark/light theme, token indicator, activity view
- **Voice** — speech-to-text and text-to-speech
- **Wake-on-LAN** — wake up the PC from your phone
- **Gotify** — push notifications for completed tasks

## Architecture

```
Phone (Compose)  →  HTTP + SSE  →  bridge/omp-bridge.mjs  →  NDJSON  →  omp --mode rpc
       ↕                                    ↕                              ↕
  Keystore (token)                    config.json                   project folder
```

- `bridge/omp-bridge.mjs` starts one OMP process per project folder (max 4, inactive ones are terminated)
- Exposes REST + SSE with bearer token
- The app owns no conversations — history, titles, and tool calls come from OMP
- Approvals from OMP are sent to the app as sheets with Approve / Approve writing for this session / Reject

## Getting Started

### 1. Install Bridge on the PC

**Windows:**
```powershell
# Download and run NimayOMP-Installer-3.2.0.exe
# Installs Bridge, OMP commands, firewall rule, and auto-start
```

**Linux:**
```bash
# Install Flatpak package
flatpak install net.nimay.ompbridge.flatpak
```

### 2. Install the app on your phone

Download `OmpChat-2.9.46.apk` from [Releases](https://github.com/megaxenon83-server/OmpChat/releases) and install.

### 3. Connect

1. Run `/ompqr` in OMP to generate a QR code
2. Open the OmpChat app → scan the QR code
3. The app connects to the Bridge automatically

## Building

### Android app

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease

# Tests
./gradlew testDebugUnitTest
```

**Requirements:** Android SDK 34, Kotlin 2.0, JDK 17

### Windows installer

```powershell
# Build with Inno Setup
iscc NimayOMP-Installer.iss
```

**Requirements:** Inno Setup 6.x, PowerShell 5.1+

## Project Structure

```
OmpChat/
├── app/                    # Android app (Kotlin/Compose)
│   ├── src/main/java/      # Source code
│   ├── src/main/assets/    # Terminal (xterm.js, local)
│   └── src/test/           # Unit tests
├── bridge/                 # Node.js bridge (runtime)
├── installer/              # Windows installer (Inno Setup)
├── omp-commands/           # OMP commands (/ompserver, /ompqr)
├── scripts/                # Linux service scripts
├── tools/                  # Helper tools
├── wol-service/            # Wake-on-LAN (Docker)
├── .github/                # GitHub Actions
└── docs/                   # Documentation
```

## Security

- Token stored in Android Keystore (AES/GCM) per PC profile
- Bridge requires bearer token for all endpoints
- `/api/ping` is public (for availability check)
- `/api/health` requires valid token
- No passwords or tokens in plaintext in app or config

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Cannot connect" | Check that Bridge is running (`/ompserver` in OMP) |
| "Token rejected" | Re-pair via QR code (`/ompqr` in OMP) |
| Port 7777 blocked | Check firewall (TCP 7777) |
| App crashes | Check logcat for stack trace |

## Version History

See [CHANGELOG.md](CHANGELOG.md) for full version history.

## License

MIT License — see [LICENSE](LICENSE) for details.

## Contributing

1. Fork the repository
2. Create a branch (`git checkout -b feature/my-feature`)
3. Commit changes (`git commit -m "Add my feature"`)
4. Push (`git push origin feature/my-feature`)
5. Open a Pull Request
