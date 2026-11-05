# OmpChat / Nimay OMP – Prosjektstatus

Sist oppdatert: 2026-11-03 av AI-agent

## Installer-fix (3.2.6)

### Feil funnet og rettet

**1. Installer-krasj (rotårsak):**
I `InitializeWizard` ble `MobilePage` opprettet med `wpInstalling` som `AfterID`:
```pascal
MobilePage := CreateCustomPage(wpInstalling, 'Mobilen', ...);
```
`wpInstalling` er installasjonsfasen, ikke en gyldig veiviser-side. Forårsaket "Could not find page with ID 48557680" umiddelbart etter språkvalg.
**Fix:** Endret `wpInstalling` → `wpReady`.

**2. Error code 2 under installasjon:**
`{sys}\explorer.exe` ble brukt for å starte broen med brukerrettigheter. Men `{sys}` resolver til `C:\Windows\System32\`, og `explorer.exe` ligger i `C:\Windows\`. CreateProcess feilet med kode 2 (fil ikke funnet).
**Fix:** Endret `{sys}\explorer.exe` → `{win}\explorer.exe`.

**3. Autostart (skjult):**
Snarveien i Startup-mappen pekte til `Start-OMP-Bridge.cmd` (åpner konsollvindu). Endret til `wscript.exe` + `start-bridge-hidden.vbs`.

**4. Engelsk språk:** Lagt til i `[Languages]`.

**5. Versjonsbump:** 3.2.4 → 3.2.6

### Verifiserte tester (3.2.6)
| Test | Resultat |
|------|----------|
| Kompilering (ISCC.exe) | BUILD SUCCESSFUL, exit 0 |
| SILENT installasjon til temp-mappe | Fullført, exit 0 |
| config.json opprettet | OK, token + port 7777 |
| Bridge start via explorer.exe (hidden VBS) | OK, /api/ping svarer 200 |
| Ingen CreateProcess-feil i logg | Bekreftet |

### Publisert fil
- **Fil:** `NimayOMP-Setup-3.2.6.exe`
- **Sted:** prosjektroten + `installer\dist\`
- **Størrelse:** 21.99 MB
- **SHA-256:** `643332A69FE17755DB0E3329B83F7C9140551C1CE346E56E5F457585D0766A14`

### Gjenstående manuell testing
- GUI med norsk/engelsk språkvalg, sideflyt, Tilbake/Neste, komponentvalg, avbryt
- Oppgradering over eksisterende installasjon uten tap av innstillinger

## SSH auto-recovery (Debian-server) - 2026-11-04

### Rotarsak
Etter strombrudd feilet ssh.service ved oppstart fordi sshd_config hadde
hardkodet ListenAddress 192.168.50.222 og 100.108.19.128; disse IP-ene
var ikke tildelt enna, sa sshd avsluttet med status 255. Basenheten har
RestartPreventExitStatus=255, sa systemd restartet den ikke. Manuell
reparasjon via privilegert Alpine-container (nsenter) var nodvendig.

### Implementerte endringer (pa serveren)
- /etc/ssh/sshd_config: ListenAddress endret til 0.0.0.0 (backup tatt).
- /etc/systemd/system/ssh.service.d/override.conf: fjernet
  RestartPreventExitStatus, Restart=on-failure, RestartSec=5, StartLimit 5/5min,
  ExecStartPre=ssh-readiness.sh, avhengigheter network-online.target +
  tailscaled.service.
- /usr/local/sbin/ssh-readiness.sh: venter (maks 60s) pa nettverk.
- /usr/local/sbin/ssh-recovery.sh: watchdog som sjekker port 22, begrensede
  forsok med 300s cooldown, logger til /var/log/ssh-recovery.log.
- /etc/systemd/system/ssh-recovery.service + ssh-recovery.timer: timer
  60s etter boot og hver 120s, persistent.
- ssh.service enabled; ssh-recovery.timer enabled.
- Docker sidecar /home/megaxenon/Docker/ssh-recovery: Alpine-container
  (privileged, pid:host, network:host, restart:unless-stopped, ingen nye porter)
  som ekstra sikkerhetsnett via nsenter. Bygget og startet; status healthy.

### Verifisering
1. SSH fungerer na (lytter pa 0.0.0.0:22, tilkobling OK).
2. Simulert tjenestefeil (systemctl stop ssh.service) ble automatisk
   reparert innen ca 30s av ssh-recovery-timeren; bekreftet i logg.
3. Reparasjon uavhengig av OMP/OMP Bridge.
4. Oppstartskonfigurasjon enabled og vedvarende.
5. Ingen konflikt mellom systemd og Docker (begge lag observert OK).
6. Eksisterende 55 Docker-tjenester urort; ny ssh-recovery-container lagt til.
Ingen full serveromstart utfort (ikke godkjent).

### Sikkerhetskopier
- /root/ssh-recovery-backup-<timestamp>/
- /etc/ssh/sshd_config.bak.sshrecovery

## Hotfix – OMP binary discovery (2.9.16)
- Rotårsak: ~/.bun/bin/omp.exe er en Bun-shim (8KB) som ikke finner node_modules når den kjøres fra en annen kontekst.
- Fiks: Bridge og ompserver bruker nå Bun direkte med cli.js i stedet for den ødelagte shimmen.
- Bridge 2.9.16, installer 3.2.7.

## Nimay OMP Bridge Cleanup Tool
- Selvstendig EXE: Nimay-OMP-Bridge-Cleanup.exe
- SHA-256: B219B7E76E5C5EA3C544CDF22E73BCACCD4BE6EB30427578791BDA14AD79F5B1
- Publisert under GitHub Release v2.9.73

## New Installer – Nimay-OMP-Bridge-Setup.exe (3.3.0)
- Selvstendig Windows-installer bygget fra bunnen av
- SHA-256: A4624E143CAF54783336D80247F2607DB9F85A23FFDC730080F13CD79DEBD701
- Installasjonsmappe: %LOCALAPPDATA%\Nimay\OMPBridge
- Autostart: Windows-tjeneste (SYSTEM, ingen passord)
- GitHub Release: v3.3.0


## Modellvelger + mest brukte modeller (OmpChat 2.9.72) - 2026-11-05

### Filtrering (OMP, permanent)
- ~/.omp/agent/config.yml: lagt til enabledModels (backup: config.yml.bak.modelselector).
  Mønstre: ollama/*, nvidia/nvidia/nemotron-3.5-super-vl-120b-a12b,
  google/*, google-antigravity/*.
- Resultat: kun aktiverte API-modeller + lokale Ollama vises; NVIDIA kun Nemotron.

### Mest brukte modeller (Android, per PC)
- PcProfile: nytt felt modelUsageCounts (Map<String,Int>), serialisert i PcProfileStore.
- ChatRepository: modelUsageProvider, onModelSelected, sortModelsByUsage,
  resortModels(); RepoState.modelUsageCounts. loadModels/sorterer; setModel teller bruk.
- AppViewModel: recordModelUsage() oppdaterer aktiv PC-profil og lagrer.
- ModelSheet: to seksjoner "Mest brukte" (maks 5) og "Andre tilgjengelige modeller",
  hake på aktiv modell, uendret design.

### Bygg
- assembleDebug og assembleRelease: BUILD SUCCESSFUL.
- APK: OmpChat-2.9.72.apk / OmpChat-2.9.72-release.apk (v2.9.72, code 97).


## Versjon 2.10.0 (2026-11-05)
- app/build.gradle: versionCode 98, versionName 2.10.0.
- APK bygget: OmpChat-2.10.0.apk / OmpChat-2.10.0-release.apk.
- GitHub release v2.10.0 opprettet med samlede assets:
  - OmpChat-2.10.0.apk
  - NimayOMP-Setup-3.2.7.exe
  - Nimay-OMP-Bridge-Cleanup.exe (uninstaller)


## Versjon 2.10.1 (2026-11-05)
- Fiks: Koble fra sletter ikke lenger PC-profilen (disconnectPc bevarer profil og paring).
- Sletting kun via søppelbøtte med bekreftelsesdialog:
  "Er du sikker på at du vil slette denne PC-en?" med Avbryt/Slett,
  auto-lukkes etter 10s uten sletting.
- app/build.gradle: versionCode 99, versionName 2.10.1.
- APK: OmpChat-2.10.1.apk.
- GitHub release v2.10.1 med OmpChat-2.10.1.apk + NimayOMP-Setup-3.2.7.exe +
  Nimay-OMP-Bridge-Cleanup.exe.


## Installer 3.2.8 (2026-11-05)
- NimayOMP.iss: DefaultDirName endret til {localappdata}\Nimay\OMPBridge
  (Bridge havner i %LOCALAPPDATA%\Nimay\OMPBridge\bridge\, ikke Program Files).
- prepare-mobile-page.ps1: bridge-ventetid 40x500ms -> 10x500ms (5s) og
  fallback 20x500ms -> 10x500ms. Raskere, begrenset ventetid.
- Ingen SYSTEM-konto: autostart via {userstartup} (wscript.exe + VBS) i
  brukerkontekst; start via explorer.exe beholder brukerrettigheter.
- Testinstallasjon til temp-mappe verifisert: bridge/config.json opprettet,
  omp-bridge.mjs til stede, installasjon exit 0.
- Ny EXE: NimayOMP-Setup-3.2.8.exe (22.01 MB).
- GitHub release v2.10.2: OmpChat-2.10.1.apk + NimayOMP-Setup-3.2.8.exe +
  Nimay-OMP-Bridge-Cleanup.exe.
- Reboot-autostart er IKKE testet (ingen omstart utført).
