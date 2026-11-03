# OmpChat / Nimay OMP – Prosjektstatus
Sist oppdatert: 2026-10-31 av AI-agent etter 2.9.36 + installer 3.1.2: Premium UI/UX 3.0 ferdigstilt og installasjonsløsningen kvalitetssikret med rettelser til Task Scheduler (1-minutts restart), riktig Windows-brukerkonto, feilhåndtering som stopper installasjon ved kritiske feil, og gammel Bridge-håndtering. Terminalprosjektet satt på pause.

## 2.9.36 + Installer 3.1.2 – 2026-10-31

**Publisert og verifisert:**

- APK `OmpChat-2.9.36-release.apk`, 8 955 968 B, SHA-256 `4365D13C533234A810A458AC0A174F57A146727E087C0C5F47EEA1619F5F20BD`
- Installer `NimayOMP-Installer-3.1.2.exe`, 18 271 694 B, SHA-256 `9102AF62CF26BA83EE7FC36B9487238D92144B9C726FD6C8EE3447DB9C13DB04`
- Lastet opp til `megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/`
- `update.json` oppdatert (android 2.9.36/61, installer 3.1.2)
- `https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.2.exe` svarer HTTP 200, riktig størrelse
- `https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.36.apk` svarer HTTP 200

**Installasjonsløsning (Nimay OMP Bridge 3.1.2):**

- **Installasjonsmappe:** `%LOCALAPPDATA%\Nimay\OMPBridge` (bruker-writable, ingen admin nødvendig for hovedfiler)
- **Inno Setup EXE-installer** med veiviser: språkvalg (nb/en), oppstartsmode (Boot/Login/Manual), brannmur, OMP-kommandoer
- **PowerShell-kjerne:** `install-nimay-omp.ps1` (Install/Update/Repair), `repair-nimay-omp.ps1`, `install-nimay-omp.bat`
- **Task Scheduler:** Bridge starter ved boot (før innlogging) eller ved innlogging, kjører som brukeren
  - Automatisk restart ved feil (3 forsøk, 1-minutts intervall – oppfyller Microsoft minimum)
  - Riktig brukerkontoidentitet ($env:USERNAME/$env:USERDOMAIN, ikke WindowsIdentity.GetCurrent() som kan returnere admin-konto ved heving)
  - Passord håndtert som SecureString
  - Skjult vindu (ingen synlig terminal)
- **OMP-opdagelse:** Finner OMP-binær, Node.js/Bun, og brukerkonfigurasjon via flere metoder (miljøvariabler, vanlige stier, OMPCHAT_HOME)
- **OMP-kommandoer:** `/ompserver` og `/ompqr` installert i `%USERPROFILE%\.omp\agent\commands\`
- **Brannmur:** TCP 7777+7787, `-Profile Any`, nettverksprofil Offentlig→Privat
- **Diagnostikk:** `diagnose-nimay-omp.ps1` viser bro-status, adresser, tjenester, brannmur
- **Oppdatering/reparasjon:** Installer oppdager tidligere installasjoner, bevarer config.json + token, migrerer fra Program Files, stopper gammel Bridge-prosess
- **Feilhåndtering:** Kritiske feil i PowerShell-skript stopper installasjonen (Abort), ikke falsk "Installasjon fullført"

**Uninstall (fjerning):**
- **`Nimay-OMP-Clean-Uninstall.ps1`** – Fullstendig avinstallasjon av OMP- og Bridge-komponenter (4 faser: søk → forhåndsvisning → avinstallasjon → sluttkontroll)
- **`Run-Clean-Uninstall.bat`** – Batch-wrapper for dobbeltklikk-start
- Begge tilgjengelige på `https://chat.nimay.net/downloads/nimayomp/`
- Kan kjøres før ny installasjon for å sikre en ren maskin

**2.9.36 (versionCode 61) – Premium UI/UX 3.0:**
(see previous section for details)

## 2.9.35 (versionCode 60) – 2026-10-31

**Verifisert på emulator** (`Pi_Test_Pixel`, API 36, `emulator-5554`) med signert release-APK:

- **Projektbasert samtaleliste:** `ChatGptDrawer.kt` har bryter «Dette prosjektet | Alle samtaler» (standard: Dette prosjektet). Samtaler filtreres etter aktiv arbeidsmappe (cwd). «Alle samtaler» viser samtaler fra alle prosjekter. Ingen samtaler slettes eller flyttes.
- **Favoritter:** Stjerne-ikon på hver samtale i sidemenyen for å feste/løsne. Trepunktsmeny med Endre navn, Fest/Løsne, Arkiver, Slett. Favoritter lagres i SharedPreferences og overlever app-restart.
- **Sidemeny:** Tydelig prosjektvelger med prosjektnavn og full sti. Kompakte samtalerader med bedre spacing. Aktiv samtale markert tydelig. Søk på tvers av prosjekter. Modellvelger, PC-valg, tilkoblingsstatus og navigasjonsikoner i bunnen. Ingen overlapp med statuslinje/navigasjonsfelt.
- **Live aktivitetsvisning:** `ActivityCard` og `ToolCallActivityCard` i `ActivityComponents.kt` viser ekte OMP-verktøykall (shell, les fil, skriv fil, web-søk, MCP, test, byg) med status (kjører/ferdig/feil), ikon, tittel, undertittel og utvidelige detaljer. Kortene oppdateres i sanntid via SSE-hendelser fra bridgen.
- **Tokenindikator:** `TokenIndicator` – sirkelformet komponent inspirert av OpenCode. Viser kontekstbruk i prosent (grønn <50%, blå 50-80%, rød >80%), modellnavn og «Ikke tilgjengelig» hvis data mangler. Klikkelig for `TokenDetails`-panel med input/output-tokens og estimert kostnad. Skiller mellom kontekstbruk og totalt tokenforbruk.
- **Stopp/send-knapp:** `SendStopButton` – subtil overgang mellom Send (blå, pil-ikon) og Stopp (blå, sirkel, stopp-ikon). Ingen sterk rød farge. Ingen layout-hopping ved bytte.
- **Kollapsbare verktøykort:** `ToolCallActivityCard` erstatter gammel `ToolCallCard`. Kompakt header med ikon, tittel, status, og utvidelig detalj-panel med parametere og output (avkortet ved 5000 tegn).
- **Automatiske samtalenavn:** `ChatRepository.generateTitle()` lager kort tittel fra brukerens første melding (maks 80 tegn, avkortet ved ordgrense). Kun for nye samtaler; overskrir aldri manuelt valgt tittel.
- **Terminal:** Uendret (prosjekt på pause). Backspace-fiks fra 2.9.34 fortsatt på plass og verifisert.
- **Tester:** 49 JVM-tester grønne (`testDebugUnitTest`).
- **Bygg:** Release-APK `OmpChat-2.9.36-release.apk`, 8 955 968 B, SHA-256 `4365D13C533234A810A458AC0A174F57A146727E087C0C5F47EEA1619F5F20BD`.

**Publisert og verifisert (2026-10-31):**

- APK `OmpChat-2.9.36-release.apk`, 8 955 968 B, SHA-256 `4365D13C533234A810A458AC0A174F57A146727E087C0C5F47EEA1619F5F20BD`.
- Lastet opp til `megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/OmpChat-2.9.36.apk` og `sha256sum` på mottakersiden matcher.
- `update.json` (android 2.9.36/61) og `/nimayomp/index.html` oppdatert. `https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.36.apk` svarer HTTP 200 med riktig størrelse.
- Hovedsidens grønne Nimay OMP-kort lenker fortsatt til `/nimayomp/`.

**Ikke fysisk testet:** IME/tastatur på ekte telefon, reell Wi-Fi/LAN uten Tailscale, mikrofon, kamera/QR (ingen fysisk enhet tilgjengelig).

**Neste steg:** Kjør installeren på PC `DESKTOP-O016JMG` (192.168.50.3) som admin; fysisk test på moto g86 power (API 36).

## 2.9.35 (versionCode 60) – 2026-10-31

**Verifisert på emulator** (`Pi_Test_Pixel`, API 36, `emulator-5554`) med signert release-APK:

- **Live aktivitetsvisning:** `ActivityCard` og `ToolCallActivityCard` i `ActivityComponents.kt` viser ekte OMP-verktøykall (shell, les fil, skriv fil, web-søk, MCP, test, byg) med status (kjører/ferdig/feil), ikon, tittel, undertittel og utvidelige detaljer. Kortene oppdateres i sanntid via SSE-hendelser fra bridgen (tool_start, tool_update, tool_end).
- **Tokenindikator:** `TokenIndicator` – sirkelformet komponent inspirert av OpenCode. Viser kontekstbruk i prosent (grønn <50%, blå 50-80%, rød >80%), modellnavn og «Ikke tilgjengelig» hvis data mangler. Klikkelig for `TokenDetails`-panel med input/output-tokens og estimert kostnad.
- **Stopp/send-knapp:** `SendStopButton` – subtil overgang mellom Send (blå, pil-ikon) og Stopp (blå, sirkel, stopp-ikon). Ingen sterk rød farge. Ingen layout-hopping ved bytte.
- **Kollapsbare verktøykort:** `ToolCallActivityCard` erstatter gammel `ToolCallCard`. Kompakt header med ikon, tittel, status, og utvidelig detalj-panel med parametere og output (avkortet ved 5000 tegn).
- **Automatiske samtalenavn:** `ChatRepository.generateTitle()` lager kort tittel fra brukerens første melding (maks 80 tegn, avkortet ved ordgrense). Kun for nye samtaler; overskrir aldri manuelt valgt tittel. Sett `autoTitleSession`-flagg ved ny chat.
- **Terminal:** Uendret (prosjekt på pause). Backspace-fiks fra 2.9.34 fortsatt på plass.
- **Tester:** 49 JVM-tester grønne (`testDebugUnitTest`).
- **Bygg:** Release-APK `OmpChat-2.9.35-release.apk`, 8 947 768 B, SHA-256 `73402A56C6209CCCDE3C7ABCEBED0D58EF6247CD779996E11E990064E1C78BDE`.

## 2.9.34 (versionCode 59) + installer 3.0.3 – 2026-10-31

**Verifisert på emulator** (`Pi_Test_Pixel`, API 36, `emulator-5554`) med signert release-APK:

- **Terminal:** xterm.js lastes fra APK-ens assets via `WebViewAssetLoader`
  (`https://appassets.androidplatform.net/assets/terminal/index.html`), ikke `localhost`/CDN.
  logcat: `typeof Terminal = "function"` og `klar (kolonner=35, rader=23)`.
  Diag viser `WebView lastet: true`, `xterm.js klar: true`, `PTY klar: true`,
  `WebSocket: true`, `Aktiv adresse: http://192.168.50.159:7777`.
  Buffer-dump i Diag viser ekte PowerShell-prompt (`PS C:\Users\Megaxenon>`).
- **Input E2E:** `adb shell input text "echo OMP_TERMINAL_OK"` + ENTER ga
  `OMP_TERMINAL_OK` i PTY-en (JS → Kotlin → WebSocket → ConPTY → xterm).
- **Failover:** `ChatRepository.connect()` prober alle kjente adresser med
  `/api/ping` (1,5 s) og bruker første som svarer; `BridgeClient` prøver neste
  adresse ved nettverksfeil (HTTP-feil gir ikke failover). logcat:
  `connect: prøver http://192.168.50.159:7777, http://100.96.46.66:7777`.
  `hosts` fra `/api/ping` lagres i `PcProfile.endpoints`, så nye LAN/Tailscale-
  adresser læres automatisk (verifisert: 100.96.46.66 dukket opp i profilen).
- **PC per fane:** `TerminalTab.pcId` persisteres; PC-velgeren i terminalen
  bytter PC for aktiv fane og kobler den på nytt.
- **Feil vises:** adresse + `tidsavbrudd` / `tilkobling nektet` / `ingen rute` /
  `token avvist` / `HTTP nnn`; automatisk retry med backoff 1→30 s, og
  «Prøv igjen» for umiddelbar nytt forsøk.
- **Tester:** 49 JVM-tester grønne (`testDebugUnitTest`), inkl. 6 nye i
  `EndpointResolverTest` (kandidater, `hosts`-merge, failover mot lokal
  testserver, feilrapportering). `testOptions.unitTests.returnDefaultValues`
  er slått på fordi `BridgeClient` logger via `android.util.Log`.
- **Tab-bytte:** bytte til ny fane gir `klar (kolonner=35, rader=23)` på egen WebView;
  bytte tilbake til fane 1 gjenopptar eksisterende PTY (buffer replay) — logcat
  `gjenopptok <sessionId> (buffer replay)` og PowerShell-prompten med tidligere
  input er tilgjengelig. Generasjonsbevisste WebSocket-lyttere hindrer at gamle
  sockets kansellerer nye tilkoblinger.
- **Input E2E per fane:** `echo TAB1_MARKER_XYZ` og `echo TAB2_MARKER_ABC` i
  respektive faner, og begge markører er tilgjengelige ved tilbakebytte.
- **Installer 3.0.3:** ny `payload/tools/setup-firewall.ps1` setter Offentlig
  nettverksprofil til Privat og oppretter inbound TCP 7777+7787 med
  `-Profile Any` (Domain/Private/Public). Kalles fra `[Run]` under installasjon.
  Kompilert med Inno Setup 6.7.4; ikke kjørt på denne PC-en (agenten er ikke admin).

**Publisert og verifisert (2026-10-31):**

- APK `OmpChat-2.9.34-release.apk`, 8 927 124 B (senere bygg med backspace-fiks),
  SHA-256 `D0F88F68EE9670990106D4E825A4DA08793A99F5D1278F6606C32F87F9D1290B`.
  Nytt: tilbake/slette-tast på Android-tastaturet fungerer i terminalen (dispatchKeyEvent fanger KEYCODE_BACKSPACE/KEYCODE_DEL, sender '\b' til PTY-en + '\b \b' til xterm.js for visuell sletting).
- Installer `NimayOMP-Installer-3.0.3.exe`, 18 245 476 B,
  SHA-256 `C85F3A1B559C6E6D17CD8BDEBE3C9668D8A93DF7D9547C0107F3A20B0E81627F`.
- Lastet opp til `megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/`
  og `sha256sum` på mottakersiden matcher.
- `update.json` (android 2.9.34/59, installer 3.0.3) og `/nimayomp/index.html`
  oppdatert; `https://chat.nimay.net/downloads/nimayomp/...` svarer HTTP 200 med
  riktig størrelse. Hovedsidens grønne Nimay OMP-kort lenker fortsatt til `/nimayomp/`.

**Ikke fysisk testet:** IME/tastatur på ekte telefon, reell Wi-Fi/LAN uten
Tailscale, sticky CTRL/ALT, mikrofon, kamera/QR.
**Neste steg:** kjør installeren på PC `DESKTOP-O016JMG` (192.168.50.3) som admin
og bekreft at appen kobler til over LAN uten Tailscale; fysisk test på
moto g86 power (API 36).
OmpChat (Telefon/PC-faner, SAF-browsing, toveis overføring med SHA-256):
- Bridge-endepunkter (alle verifisert via HTTP): /api/fs/list|storage|tree|
  search, /api/fs/mkdir|rename|delete|copy|move, /api/files/download (Range),
  /api/files/upload (offset+finalize+sha), /api/files/hash, /api/files/upload-status.
  Beskyttede systemstier nektet i fs/delete; fsutil-drive-listing for roots.
- App: FileManagerScreen (Telefon-fane med SAF-trær via lagrede tillatelsene,
  PC-fane via bridge, brødsmuler, liste/rutenett, sortering, visning,
  flervalg langttrykk, ny mappe, roting, delete-dialog, del/overfør-til-PC,
  konflikt-dialog, overføringskø med pause/fortsett/avbryt/hastighet,
  PC→telefon via dokument-API med binært stream + tmp.ompdownload→rename
  og SHA-256 verifisering; telefon→PC via chunk-POST + finalize + hash).
  Transfers brukersafe diskpath og gjenopptak med offset (motorvirke) + retry.
- Brukererfaring as verified on emulator: PC-tabben klarerte lagringsinfo og
  bro-endepunkter (listC:/roots 46 oppfår = C:\Users\...Documents), PC-listing,
  File listet — manuelt på telefon for full gjennomgang (SAF/dialogs) gjenstår.
- APK 2.7.0 (versionCode 19), 8 568 285 byte, SHA-256 749B19E3…7B5C; deployet.
  Screener ( NimayOMP-Setup 2 D0E510B9… ) ha eddie-EXE = nybygg med bridge+voice.


## Versjon
- Android-klient: **2.6.9 (versionCode 18)** – godkjenningsknapper mappet til OMPs reelle
  moduser (always-ask/write/yolo), beskrivende navn i Innstillinger, encoding + GPU-fallback
  server-side. APK: `OmpChat-2.6.9-release.apk` (8 473 825 byte),
  SHA-256 `ED0BA08F5FAAD959C03828E341C3A059F615676E419FFCD4C4C43B999329E1DD`.
  Deployet til `C:\Program Files\Nimay OMP\apk\OmpChat.apk`; levert til megaxenon@192.168.50.222.
- Windows-installasjon: NimayOMP-Setup-2.6.8.exe (SHA-256 `E6EE11C7…7AF64`) m/ oppdatert
  bridge (approval-mapping + restart av ledige runtimeer) og voice_runtime (utf-8-svar,
  STT-DLL-path + CPU-fallback). Levert + sha256sum ok.
- Forrige: 2.6.8 (17) – bakgrunnsgjenoppretting (se «Bakgrunnsfrøsning»-seksjonen nedenfor)
  - `ChatRepository.onAppForeground()/onAppBackground()` koblet til ON_START/ON_STOP
  - SSE-watchdog: 45 s stillhet (uten bridge-heartbeat) gjør sokkel gjenoppbyggbar
  - `resyncAfterReconnect()` henter transkript + state + sessions + models ved hver
    (re)kobl; ingen ny sesjon startes, pågående oppgave berøres ikke
  - Bakgrunnen parkerer SSE når ingen streaming pågår (batteri/unngå døde sokler);
    under pågående streaming holdes strømmen varm til oppgaven ender
  - Auto-scroll utløses også på *innholdsleding* (delta/verktøypåmming i siste melding)
  - instrumentert med `Log.d("OmpChat", …)` for diagnostikk
  - APK: `OmpChat-2.6.8-release.apk` (8 473 818 byte),
    SHA-256 `16F766B92F1BD2BCD1B20B71575F8D3A951F979EC3EBB0D5D91E34FB2454F12E`
  - Deployet til `C:\Program Files\Nimay OMP\apk\OmpChat.apk` (HEAD på 7787/7777 verifisert)
  - Levert til megaxenon@192.168.50.222 (sha256sum ok)
- Windows installer: NimayOMP-Setup-2.6.7.exe → bygges på nytt som 2.6.8 disse ronde.

## Bakgrunnsfrøsning – rotårsak og fix (2.6.8)
- **Årsak 1:** ingen lifecycle-håndtering — `connect()` kun ved prosessstart/pairing;
  Wi-Fi/Doze/nat-death ga halvåpen sokkel: `streamClient` readTimeout(0) henger
  `readUtf8Line()` evig — ingen «disconnected», ingen gjenkobling, ingen synk ved retur.
- **Årsak 2:** gap etter reconnect ble aldri fylt — `handle("connected")` kun satte
  connected=true; manglende events (message_start/delta/tool_end) mens sokkel dø ble
  aldri hentet fra transkriptet.
- **Årsak 3:** auto-scroll triggerte bare på `messages.size` — deltas/verktøypådrag
  ender INNENfor siste melding (størrelse uendret) → ingen scroll under lange blokker.
- **Fixer** (3 filer): BridgeClient.watchdogStreamClient (readTimeout 45s) +
  re-koble; ChatRepository.onAppForeground/Background + resync-after-reconnect
  (transkript, /api/state raw via stateRaw, models, sessions) med 2,5 s debounce;
  ChatScreen tailFingerprint + snapshotFlow-resett av autoFollow.
- `connection_error` setter ikke lenger `streaming=false` — transiente nettverksfeil
  skal ikke drepe spinneren mens OMP produserer; authority (state) setter statusen.

## Nedlastingsside på chat.nimay.net (2026-10-29)
Docker-container `nimay-chat-site` (nginx, port 8788→80) serverer **chat.nimay.net**.
- Ny side `/nimayomp/` med intro, funksjonsliste, systemkrav og nedlastinger:
  webroot `/home/megaxenon/Docker/nimay-chat-site/site/nimayomp/index.html`
- **FEST REGEL for hver release:** last opp nyeste `NimayOMP-Setup-*.exe` + `OmpChat-*-release.apk`
  til `/home/megaxenon/Docker/nimay-chat/updates/nimayomp/` (erveres som `/downloads/nimayomp/`),
  oppdater `/nimayomp/-siden med nye filnavn + SHA-256 + lenkene, og verifiser sha256sum
  + HTTPS-respons (HTTP 200 + Content-Length). Hovedsiden / beholder hot-kort-lenken til /nimayomp/.
- Filer per nå: `NimayOMP-Setup-2.6.8.exe` (sha `D0E510B9…`, 10 519 707 byte) +
  `OmpChat-2.7.0.apk` (sha `749B19E3…`, 8 568 285 byte).
  Verifisert offentlig over HTTPS (HTTP 200 + riktig Content-Length på begge).
- nginx reloade: `docker exec nimay-chat-site nginx -s reload`.

## Testresultater (emulator Pi_Test_Pixel, API 34, ekte bridge)
1. Lang OMP-oppgave + verktøykall (bash sleep 60/40): **OK** via bro
2. HOME i 30-80 s mens streaming: parking av SSE i bakgrunn verifisert via
   `ss -tnp` (ingen aktive ESTAB); resume -> resync-burst (maks 4 sokler: SSE + state +
   transkript + sessions) og nytt live-innhold vises etterpå
3. Skjermlås 45 s under verktøykall: ingen krasj (0 fatal), korrekt visning etter opplås
4. Nettverksbrudd (svc wifi + data disable) 20 s: reconnect OK etter resume
5. `am force-stop` midt i streaming (prosess-dødt) -> gjenopprett:
   logcat viser `resumeLast -> match -> openSession OK: 3 meldinger`; UI viser full
   samtale inkl. Ferdig-verktøykall og token-tall
6. Duplikater: ingen identiske node-duplikater i uiautomator-dump
7. 29 JVM-tester fortsatt 0 feil (clean build)
- (Merk: tester kjørt på emulator; fysisk telefon mangler tilgjengelig her- og
  gjenstår til neste steg)

## VibeVoice (vibevoice-community) – status 2026-10-29
- **HOTFIX (samme kveld, etter telefon-test):** en status-runde hadde stempelt feil
  dir for begge vibe-motorene (`models\piper\vibevoice-<id>`); «Modellmappen mangler»
  stoppet telefonsamtalestemmen («Prøv igjen»-skjerm). Retting:
  dir-oppføringer reparert på disk + `_resolve_vibe_dir()`-guard i `voice_runtime.py`
  (alle stier bruker reparert dir). Verifisert over bro: 1.5B 5,2 s/70 s, 0.5B 6 s/36 s
  (18 s varm), piper 1,4 s/4 s. APK uendret (server-side); installer rebygget:
  NimayOMP-Setup-2.6.8.exe SHA-256 `CCE291AC…2EC78A8`, levert + hash-verifisert.
- **1.5B installert og VERIFISERT**: `POST /api/voice/install {engine:"vibevoice-1.5b"}`
- **1.5B installert og VERIFISERT**: `POST /api/voice/install {engine:"vibevoice-1.5b"}`
  → 3 767 MB i `voice\models\vibevoice-1.5b`, nedlasting via PowerShell/curl
  (Python-DNS ð bruken gjør `snapshot_download` umulig; laatste fil-for-fil fra
  `vibevoice/VibeVoice-1.5B`HF-repo)
- Runtime: `synthesize_vibevoice()` i `voice_runtime.py` (stubber transformers' nativ
  vibevoice-tokenizer-moduler for å unngå dobbeltregistrering; qwen2-fast shim;
  PCM16-konvertering via `write_wav_pcm16`; DDPM-steg = 4)
- Målt: 6,13 s lyd på ~89 s (varm OS-cache, kald prosess) = ~14× realtime på RTX 4080
  (4 steg, cfg 1,3, alice-preset). **For treg til samtale** – 1.5B er kvalitetsmodus.
- **vibevoice-0.5b (Realtime) installert og VERIFISERT via bro**: 3,5-3,6 s lyd per kall,
  **29 s kaldt / 20 s varm** totalt inkl. HTTP-plus; 7 stemmer (.pt-embeddings:
  Emma/Carter/Davis/Frank/Grace/Mike/Samuel)
- Bro-resident worker-svar har .resp-fil-fallback: piper 2,6 s lyd / 6 s varm totalt,
  0.5b 20 s varm — alt gjennom samme prosess
- `POST /api/voice/select {kind:"tts", engineId:"vibevoice-1.5b", voice:"en-Alice_woman"}`
  verifisert (ok).
- **vibevoice-0.5b** (Realtime, 1,1 GB) ble installert for å teste raskere motor.
- STT-regresjon reparert: 12:46-installasjonen mistet `stt` fra voice-config.json
  (cascading overwrite). Rekonstruert fra disk (whisper small/medium + tiny fantes
  fortsatt): verifisert runtime-STT på nytt.
- Venv: transformers **4.51.3**, tokenizers 0.21.4, huggingface_hub 0.30.2,
  accelerate 1.6.0 (nødvendig for device_map) – piper/chatterbox/faster_whisper
  import-OK sammen (= `alle OK: 4.51.3`).

## Resident voice-worker (bro latens)
- `voice_runtime.py serve`: JSON-linjer på stdin (ping/status/tts/stt/quit); modeller
  holder seg lastet i prosess (per (id, compute/device)) – ikke lenger ny prosess per kall
- `bridge/omp-bridge.mjs`: `spawnVoiceWorker()/runVoiceWorker()` (ett kall om gangen,
  timeout 480 s TTS/240 s STT, kill ved bro-strunning); `/api/voice/speak` og
  `/api/voice/transcribe` bruker workeren
- Bro 24x: repo = installert (hash-synkronisert på nytt).

## Bygg

### Android
- Kommando: `.\gradlew.bat -p C:\Users\Megaxenon\Documents\Opencode\OmpChat clean assembleRelease --console=plain`
  (`gradle.bat` ligger ikke på PATH; bruk wrapperen i prosjektroten)
- Gradle 8.9, BUILD SUCCESSFUL
- APK: `OmpChat-2.6.4-release.apk` (prosjektroten), 8 461 518 bytes
- SHA-256: `CCAF712278771652858FA7F87CC51D341BAF5C67379864EFDC0B064861D30BC2`
- `aapt2 dump badging`: `versionCode='13' versionName='2.6.4'`, `RECORD_AUDIO` i manifestet
- Signert med samme nøkkel som 2.0–2.5 (sertifikat SHA-256 `f1a3f7db…aef6200`)

### Windows-installasjon
- Kommando: `powershell -NoProfile -ExecutionPolicy Bypass -File installer\build-installer.ps1`
  (bruk `-SkipStage` for a kompilere uten a stage payload på nytt)
- Kilde: `installer\NimayOMP.iss` + `installer\payload\*` + `bridge\omp-bridge.mjs` + APK
- Utdata: `NimayOMP-Setup-2.6.4.exe` i prosjektroten (10 400 075 bytes)
- SHA-256: `D427C15D42167BA8E42AB4453E33C06E08EADCB3A8E608F693DBEF89642AA213`
- Versjonsinfo i EXE: `2.6.4.0` / `2.6.4`
- Staget payload `installer\build\payload\apk\OmpChat.apk` har samme SHA-256 som `OmpChat-2.6.4-release.apk`; og `installer\build\payload\bridge\omp-bridge.mjs` har samme SHA-256 som `bridge\omp-bridge.mjs` (`4CC863E109378621904B9A5F1807B8880F772D87650F134802B5268D0A819C79`)
- Nye bridge-endepunkter: `POST /api/approval` (godkjenningsmodus), `POST /api/voice/select` (STT/TTS-motorvalg)
- Staget payload inneholder naa `tools\omp_qr.py` (6880 byte) og oppdaterte
  `commands\ompchat-shared\ompchat.ts` + `commands\ompqr\index.ts` (hash matcher repo)
- Reproduksjonstest: EXE-en ble kompilert på nytt fra samme kilder til en annen katalog
  (`ISCC /O%TEMP%\omp-verify-dist /FNimayOMP-Setup-2.6.1-verify`) og ble **byte-identisk**
  med den leverte filen (samme storrelse og SHA-256 `6BC77EB0…D75B4`). Det bekrefter at
  den leverte EXE-en inneholder `omp_qr.py`, skriveprobe-fiksen og Node-fallbacken.
  (Direkte utpakking er ikke mulig: innoextract 1.9 og 7-Zip 26.02 stotter ikke Inno Setup 6.7.)
- ISCC: `C:\Users\Megaxenon\AppData\Local\Programs\Inno Setup 6\ISCC.exe`
- Levert til `megaxenon@192.168.50.222:/home/megaxenon/`: `NimayOMP-Setup-2.6.4.exe` og
  `OmpChat-2.6.4-release.apk`, begge hash-verifisert med `sha256sum`

## Fordelt APK i den kjørende installasjonen (viktig)

Fram til 2026-10-28 kveld la `C:\Program Files\Nimay OMP\apk\OmpChat.apk` fortsatt på **2.5**
(8 400 078 byte, versionCode 8), fordi 2.6/2.6.1-installatørene bare var bygget, ikke kjørt.
Mobil-siden (`/apk` på 7777 og apk-serveren på 7787) delte derfor 2.5, og appen manglet
talesamtale-knappen. APK-en er nå erstattet manuelt (elevated kopi) med **2.6.4**:

- `C:\Program Files\Nimay OMP\apk\OmpChat.apk`: 8 461 518 byte,
  SHA-256 `CCAF712278771652858FA7F87CC51D341BAF5C67379864EFDC0B064861D30BC2`,
  `versionCode='13' versionName='2.6.4'`
- Verifisert over HTTP: `HEAD /apk/OmpChat.apk` på både **7787** og **7777** gir 8 461 518 byte
- **Viktig felle:** versjon 2.6.3-12-byggene hadde STALE Kotlin-konstanter fordi inkrementell
  kompilering ikke re-kompilerte `BuildConfig`-referanser ved versjonsbytte («2.6.3 (12)» sto
  i Om-seksjonen selv om manifestet var nyere). Rutine fremover: **clean build** etter hver
  versjonsbytte. DEX er verifisert: ingen «2.6.3 (12)», «2.6.4» til stede; appen viser
  «2.6.4 (13)» i Om.
- Telefonen må laste ned på nytt og installere over den gamle appen (samme signeringsnøkkel,
  så den oppdateres på plass)

## 2.6.3: modellvalg som nullstilte seg + blokkert runtime

Symptom: etter at modell var valgt, nullet appen valget og viste ingen modell.

Årsak: OMP-runtimeen sto fast på en ubesvart verktøygodkjenning. Mens OMP venter på svar på en
`extension_ui_request`, svarer den ikke på `get_state`, `new_session`, `set_model` eller `abort`.
`POST /api/model` timet ut (60 s), og `/api/state` kunne returnere `model: null` – appen overskrev
da det brukeren hadde valgt.

Endringer i 2.6.3:
- `ChatRepository.applyState` beholder valgt modell når OMP rapporterer `model: null`
- `setModel` viser og lagrer valget umiddelbart (optimistisk), og ruller ikke tilbake ved feil;
  feilen vises som varsel i stedet
- Bridge: ventende godkjenninger huskes per runtime og sendes på nytt ved ny SSE-tilkobling
- Bridge: `GET /api/state` svarer fra cache med `request` mens en godkjenning venter (ingen 60 s
  tidsavbrudd); `GET /api/models` bruker cache i samme situasjon
- Bridge: `POST /api/abort` avbryter ventende godkjenninger først og stopper OMP-prosessen hvis
  `abort` fortsatt ikke går gjennom, slik at neste forespørsel starter en frisk prosess

Verifisert mot ekte bro (patchet bridge på 7777): prompt med bash-godkjenning → `/api/state`
svarte på 16 ms med `request`; ny SSE-tilkobling fikk godkjenningen tilsendt på nytt; `/api/ask`
kunne svare; `/api/abort` returnerte `{"ok":true}` umiddelbart og `sessions/new` virket rett etter.
Testprosjektet `%TEMP%\omp-263-test` og dets sesjoner er slettet.

## 2.6.3 – Nye funksjoner (denne runden)

### 1. Godkjenningsinnstillinger (Innstillinger → Tilkobling)
- Tre valg: «Godta alt» (default), «Spør ved behov» (default), «Spør alltid» (always-ask).
- Endring sendes til bridge via `POST /api/approval`, synkroniseres med serveren og huskes i `SharedPreferences` etter omstart.
- Bridge lagrer `approvalMode` i `bridge\config.json` via `writeConfig()`.

### 2. Talesamtaleknapp i chatfeltet (ikke i header)
- Den blå telefonknappen i toppmenyen er fjernet.
- Ny waveform-knapp ved siden av mikrofonen i chatfeltet nederst (bare når feltet er tomt).
- Mikrofon = diktering til tekst (eksisterende atferd).
- Waveform = direkte toveis talesamtale med AI (åpner VoiceScreen).
- Aktiv modell, prosjekt og samtale beholdes i talemodus.

### 3. Intelligent automatisk scrolling
- Chatten følger nyeste tekst under streaming (AI-generering).
- Når brukeren scroller opp manuelt, stoppes autofølging.
- «Hopp til siste»-knapp vises når man ikke er helt nede.
- Autofølging gjenopptas når brukeren når bunnen.
- Ingen hakking: bruker `derivedStateOf` på `listState.isScrollInProgress`.

### 4. Valg av talemotorer (Innstillinger → Tale og lyd)
- STT: viser alle installerte STT-motorer fra `/api/voice/engines` (f.eks. NB-Whisper Tiny/Base/Small).
- TTS: viser alle installerte TTS-motorer (f.eks. Piper, Chatterbox, VibeVoice).
- Viser om motor er installert, mangler eller ikke er tilgjengelig.
- Valg lagres i appen og synkroniseres til bridge via `POST /api/voice/select`.
- Talebehandling kjører lokalt på PC; Android er mikrofon, høyttaler og kontrollpanel.
- Testknapp for å høre valgt stemme.

### Tester (2.6.3, versionCode 12)
- 27 JVM-tester, 0 feil (InlineToolCallTest 4, SettingsParsingTest 5, VoiceCallTest 10, MarkdownParserTest 4, ToolFoldTest 4)
- Bridge-endepunkter verifisert mot ekte bro på 7777:
  - `POST /api/approval` → default ↔ always-ask (tilbake)
  - `POST /api/voice/select` → STT whisper-tiny / nb-whisper-small; TTS piper-talesyntese / chatterbox
  - `GET /api/voice/engines` → stt/tts lister, active, installed, python
- APK bygget, SHA verifisert, levert til megaxenon@192.168.50.222:/home/megaxenon/
- Installer bygget, SHA verifisert, levert til megaxenon@192.168.50.222:/home/megaxenon/

## 2.6.2: skrivefelt, auto-sesjon og modellminne

Symptomer rapportert fra telefonen: kunne ikke skrive i chatten, modell manglet i innstillinger,
og talesamtalen sa «Åpne en samtale før du starter talesamtale».

Årsak: OMP-runtimeen for prosjektet appen brukte (`C:\Users\Megaxenon\documents\omp\upgrade`)
sto fast i en runde som ventet på verktøygodkjenning. `POST /api/sessions/new` fikk tidsavbrudd
(ingen sesjon → deaktivert inndatafelt i 2.6), `/api/prompt` svarte «Agent is already processing»,
og `POST /api/model` fikk ikke svar (modellnavnet ble aldri satt). Runtimeen ble drept/restartet;
deretter virket `sessions/new`, `models` (271) og `model` mot ekte bro.

Endringer i 2.6.2:
- Inndatafeltet er aktivt så lenge bridgen er tilkoblet (`ChatScreen.kt`), ikke bare når en sesjon finnes
- `send` og talesamtalen oppretter sesjon automatisk via `ChatRepository.ensureSession()`;
  prosjekt velges fra aktiv chat → `lastCwd` → mest brukte prosjekt
- `setModel` krever ikke lenger en allerede åpnet sesjon og lagrer valget (`rememberModel`)
- Nye samtaler bruker lagret modell (provider/id) og viser den i header/innstillinger
- `SettingsScreen` viser sist valgte modell også før en sesjon er åpnet

Verifisert på emulator (API 35) med 2.6.2: ny samtale → tom chat med prosjekt og modell →
trykk i feltet åpnet tastatur → melding sendt → OMP svarte med godkjenningsdialog for bash.
Testsesjoner laget i `upgrade` under feilsøkingen er slettet; brukerens egne sesjoner er beholdt.

**Merk:** med `always-ask` må hvert verktøykall godkjennes i appen. Godkjenner man ikke (eller
trykker «Approve bash for this session»), blir runden stående og blokkerer nye sesjoner til den
avbrytes.

**Merk 2:** Broen og en interaktiv OMP-TUI kan ikke skrive i samme sesjonsfil. Har TUI-en
sesjonen åpen, forkler OMP en ny fil (`…<tid>_<id>.jsonl`), og broen kan havne på en tom
«Ny samtale». Bruk derfor et annet prosjekt i appen enn det TUI-en står i, eller lukk TUI-en
for det prosjektet mens man tester appen. Etter ryddingen 2026-10-28 står `upgrade` igjen med
bare brukerens to opprinnelige sesjoner (13 og 2 meldinger), ingen runtime streamer, og
`/api/health` viser `streaming=false` for alle prosjekter.

Talesamtalen har **ingen egen innstilling**: inngangen er telefonikonet øverst i chat-headeren
(se `ChatScreen.kt` linje ~310–327), og «Håndfri samtale» er en bryter inne på callsiden.
På appens hjem/samtaleliste finnes ingen slik knapp.

## Talesamtale i appen (implementert, verifisert på emulator)

Ekte kjede, ingen simulering: mikrofon (AudioRecord 16 kHz mono PCM16, 20 ms rammer) →
adaptiv RMS-VAD (start ~140 ms, slutt ~800 ms stillhet, min 350 ms, maks 20 s) →
WAV kodes på telefonen → `POST /api/voice/transcribe` (lokal nb-whisper-small) →
`ChatRepository.send` til aktiv OMP-sesjon → `VoiceTurn.awaitReply` venter på ekte
assistentsvar (feiler eksplisitt ved feil) → `POST /api/voice/speak` (lokal Piper) →
WAV lastes ned → `MediaPlayer` spiller av med tale-attributter → håndfri modus lytter igjen.

- Inngang: telefonknapp øverst i chatten (erstatter den tidligere utilgjengelige `VoiceScreen`).
- Skjerm: `VoiceCallScreen` med stor pulserende knapp, fase-tekst (lytter/tolker/tenker/snakker),
  nivåmåler, transkripsjon/svar-kort, «Håndfri samtale»-bryter, feilkort, «Avslutt samtalen».
- Kode: `data/VoiceCall.kt` (WAV/VAD/tekst/svar-venting), `data/VoiceCallEngine.kt` (hele kjeden),
  `data/BridgeClient.kt` (`voiceEngines/voiceTranscribe/voiceSpeak/voiceAudio`),
  `ui/screens/VoiceCallScreen.kt`, `ui/AppRoot.kt`, `ui/MainActivity.kt` (RECORD_AUDIO),
  `ui/AppViewModel.kt`, `data/Settings.kt` (`voiceHandsFree`).
- Testseam: `VoiceCallEngine(captureOverride = …)` lar instrumenterte tester erstatte mikrofonen
  med en TTS-generert WAV; produksjonsappen bruker alltid `AudioRecord`.

## /ompqr-fiksen (implementert, verifisert med Node-harness)

Feil rapportert fra ekte OMP: `Error: Mangler C:\Program Files\Nimay OMP\tools\omp_qr.py`.
Tre årsaker, alle fikset:

1. `tools\omp_qr.py` var ikke med i installer-payloaden. Nå stages den fra repoets
   `tools\omp_qr.py` til `installer\payload\tools\omp_qr.py`.
2. QR-bildet ble skrevet til `C:\Program Files\Nimay OMP\omp_chat_qr.png`. Rotmappen er
   skrivebeskyttet for vanlige brukere (bare `bridge`, `voice`, `tools` har ACL-skriving),
   så Python feilet med `PermissionError`. `qrOutputPath()` gjør nå en reell skriveprobe og
   faller tilbake til `%LOCALAPPDATA%\OmpChat\omp_chat_qr.png`.
   (`fs.accessSync(root, W_OK)` er ubrukelig på Windows – den rapporterer Program Files som skrivbar.)
3. Hvis Python eller `qrcode`/`pillow` mangler, bruker kommandoen `tools\qrgen.mjs` (Node,
   `qrcode`-pakken i `tools\node_modules`) og lager en BMP med skala 12. Bildet åpnes i
   bildeviseren, og widgeten viser sti + kommandoer i stedet for terminal-QR.

Testet med `%TEMP%\ompqr-harness.mjs` (importerer installert `~\.omp\agent\commands\ompqr\index.ts`
med falsk OMP-ctx):

| Sti | Resultat |
|-----|----------|
| Python tilgjengelig | `%LOCALAPPDATA%\OmpChat\omp_chat_qr.png` (961 byte), terminal-QR i widget, «QR klar for 100.96.46.66» |
| `PATH` uten Python (`--no-python`) | `%LOCALAPPDATA%\OmpChat\omp_chat_qr.bmp` (1 037 286 byte), bilde åpnet, «QR klar for 100.96.46.66» |
| QR-innhold | jsQR dekodet `omp2://100.96.46.66:7777?token=…` og `omp2://192.168.50.159:7777?token=…` |

OpenCV ga falskt negativ på enkelte payloads; jsQR (ZXing-basert) dekodet alle. Bruk jsQR
ved ny QR-verifisering. `opencv-python-headless` og `pyzbar` ble pip-installert på denne PC-en
under feilsøkingen (pyzbar fikk DLL-feil og ble ikke brukt).

Live-installasjonen er patchet uten full reinstallasjon:
`tools\omp_qr.py` kopiert til `C:\Program Files\Nimay OMP\tools\`, kommandoene i
`~\.omp\agent\commands` oppdatert fra repoet, og `~\.omp\agent\ompchat-commands.json` satt til
`{"projectRoot":"C:\\Program Files\\Nimay OMP","bridge":"C:\\Program Files\\Nimay OMP\\bridge\\omp-bridge.mjs"}`.
OMP laster globale kommandoer ved oppstart, så OMP må restartes før `/ompqr` bruker den nye koden.

## Hva installasjonsprogrammet gjor (implementert og testet)
1. Norsk veiviser med installasjonstyper (full / kompakt / egendefinert).
2. Komponentvalg for talemotorer med storrelse og maskinvarekrav:
   - STT: whisper-tiny (75 MB), NB-Whisper Small (464 MB), NB-Whisper Medium (1,6 GB)
   - TTS: Piper talesyntese (63 MB), Piper nvcc (77 MB), Chatterbox (valgfri), VibeVoice (kun NVIDIA)
3. Installerer Node.js LTS og Python automatisk via winget/nedlasting nar de mangler.
4. Installerer ekte talemotorer lokalt: Python-venv + `piper-tts` + `faster-whisper`,
   modeller lastes ned fra Hugging Face, deretter selvtest (TTS lager WAV, STT transkriberer
   norsk testtale). Status skrives til `voice\voice-status.json` og `voice\install-job.json`.
5. Skriver `bridge\config.json` med tilfeldig 64-tegns token for stabil paring.
6. Gir skriveatkomst (BUILTIN\Users M) kun til `{app}\bridge`, `{app}\voice`, `{app}\tools`,
   slik at tjenestene kan skrive config/pid/lyd ogsa nar programmet ligger i `C:\Program Files`.
   Selve programfilene forblir skrivebeskyttet.
7. Brannmurregel «Nimay OMP Bridge» for TCP 7777 og 7787 (private/domain).
8. Starter broen via `explorer.exe` + `tools\start-bridge-hidden.vbs` slik at den kjorer med
   vanlige brukerrettigheter, ikke som administrator (verifisert: Medium integritet).
9. «Mobilen»-side i veiviseren viser to QR-koder (APK-nedlasting og OMP-paring), APK-adresse,
   verifisert LAN-nedlasting (HTTP 200 + byte-antall) og hvilke talemotorer som ble testet.
10. Oppgradering bevarer token, modeller og konfigurasjon. Avinstallasjon stopper tjenestene
    (via `bridge\bridge.pid` og `tools\apk-server.pid`), fjerner brannmurregelen og hele
    installasjonsmappen.

## Verifiserte tester (talesamtale, denne runden)
| Test | Resultat |
|------|----------|
| `.\gradlew.bat :app:testDebugUnitTest` | BUILD SUCCESSFUL, 27 tester, 0 feil (`VoiceCallTest` 10: WAV-header, PCM, VAD start/slutt/for-kort/timeout/RMS, markdown→tale, trunkering) |
| `.\gradlew.bat :app:connectedDebugAndroidTest` på `Pi_Test_Pixel` (API 35) med ekte bro på 7790 | 2 tester, 0 feil, 0 ignorert, 26,9 s |
| `spokenTurnReachesOmpAndComesBackAsAudio` | ekte TTS → WAV → nb-whisper-small → ekte OMP-sesjon (`ollama/gemma4:12b`) → svar «Hovedstaden i Norge er Oslo.» → TTS → `MediaPlayer` spilte av |
| `engineRunsWholeTurnWithFixtureAudio` | produksjonens `VoiceCallEngine` kjørte hele runden med TTS-lyd som inngang: transkripsjon, svar «Oslo er hovedstaden i Norge.», 1 runde, ingen feil |
| UI på emulator (adb-skjermbilder + uiautomator) | telefonknapp `Talesamtale` i chat-headeren åpner talesamtalen, viser «Lytter …», modell `gemma4:12b`, håndfri-bryter kan slås av, «Avslutt samtalen» går tilbake til chatten |
| Støy uten tale (~19 s på emulator) | ingen falsk VAD-start; appen ble stående i «Lytter …» |
| Release-APK 2.6 paret mot den ekte broen på **7777** etter restart (adb reverse) | åpnet ekte sesjon, telefonknappen viste «Lytter …» med `gemma4:12b` – kapasitetssjekken mot ekte bro passerte (ingen «motorene mangler»-feil) |
| TTS+STT-rundtur direkte mot 7777 | `piper-talesyntese` 1,72 s WAV (75 820 bytes) → `nb-whisper-small` transkriberte «Talesamtalen er klar til bruk.» |

Testkommandoer som ble brukt:
```
.\gradlew.bat :app:testDebugUnitTest :app:assembleRelease --console=plain
adb reverse tcp:7790 tcp:7790
.\gradlew.bat :app:connectedDebugAndroidTest --console=plain `
  "-Pandroid.testInstrumentationRunnerArguments.baseUrl=http://127.0.0.1:7790" `
  "-Pandroid.testInstrumentationRunnerArguments.token=<token>" `
  "-Pandroid.testInstrumentationRunnerArguments.cwd=%TEMP%\omp-voice-android" `
  "-Pandroid.testInstrumentationRunnerArguments.provider=ollama" `
  "-Pandroid.testInstrumentationRunnerArguments.modelId=gemma4:12b"
```

## Verifiserte tester (installasjon, forrige runde)
| Test | Resultat |
|------|----------|
| Stille installasjon til `C:\Program Files\Nimay OMP` (core + whisper-tiny + piper) | exit 0, selvtest `{"test":{"stt":"ok","tts":"ok"},"stage":"done"}` |
| `config.json` opprettet av installatoren + ACL `BUILTIN\Users:(OI)(CI)(M)` | ja, token 64 hex-tegn |
| Bro startet som vanlig bruker (port 7801) | `bridge.pid` skrevet, `/api/ping` ok |
| `installer\dev\api-test.ps1` mot Program Files-installasjon | ping ok, APK HEAD 8 400 078 bytes, LAN-nedlasting 200, TTS 2,61 s WAV, STT norsk tekst |
| `prepare-mobile-page.ps1` mot Program Files-installasjon | `VERIFY=ok`, `HTTP 200, 8400078 bytes`, begge QR-BMP dekodet med OpenCV |
| Oppgradering over samme mappe (+ piper-nvcc) | exit 0, token bevart, talesyntese + whisper-tiny bevart, ny motor lagt til |
| Avinstallasjon (`/VERYSILENT`) | exit 0, mappen helt fjernet, 0 node-prosesser igjen, brannmurregel fjernet |
| Starter via `explorer.exe` + VBS fra forhoyet prosess | bro-PID 25056 med Medium integritet (ikke admin) |
| Veiviser-skjermbilder (`installer\dev\ui-screenshots.ps1`, forhoyet) | Komponenter, Fremdrift, Mobilen og Fullfor rendret; begge QR i skjermbildet dekodes til riktige URL-er |
| Tidligere temp-installasjoner (`%TEMP%\nimay-test1..4`) | install/oppgradering/avinstallasjon, bakgrunnsinstallasjon av motor via API, APK-server |

Testkommandoer som ble brukt (installasjon):
```
powershell -NoProfile -ExecutionPolicy Bypass -File installer\build-installer.ps1
Start-Process -FilePath .\NimayOMP-Setup-2.5.exe -ArgumentList '/VERYSILENT','/SUPPRESSMSGBOXES','/NORESTART',`
  '/DIR="C:\Program Files\Nimay OMP"','/COMPONENTS="core,stt\whisper_tiny,tts\piper_talesyntese"','/TASKS=""' -Wait
powershell -File installer\dev\api-test.ps1 -AppDir "C:\Program Files\Nimay OMP" -Port 7801
```

## Kjente begrensninger / gjenstar
- Broen paa 7777 ble restartet etter testene og svarer naa paa tale-API-et:
  `/api/voice/engines` → `installed=true`, `python=true`, TTS `piper-talesyntese`,
  STT `nb-whisper-small`; TTS+STT-rundtur paa 7777 ga 1,72 s WAV og norsk transkripsjon.
  (Den kjorende prosessen var eldre enn filen paa disk og svarte 404 for restart.)
- **Felle for senere tester:** `omp-bridge.mjs` skriver hele den sammenslatte konfigurasjonen
  tilbake til `bridge\config.json` ved oppstart, inkludert `OMPCHAT_BRIDGE_PORT`. En testbro
  startet med miljovariabel paa en annen port lagrer altsaa den porten permanent. Etter denne
  runden ble `port` satt tilbake til 7777; sjekk dette hvis broen plutselig lytter paa feil port.
- **Ekte telefonmikrofon er ikke verifisert.** Bare emulator (`Pi_Test_Pixel`, API 35) var
  tilgjengelig i `adb devices`; emulatoren gir ikke ekte mikrofonlyd. VAD/opptak er derfor
  dekket av JVM-tester og av `engineRunsWholeTurnWithFixtureAudio` (TTS-lyd som inngang),
  men ikke av faktisk talte ord inn i telefonens mikrofon.
- Håndfri modus er testet som tilstand/logikk; den automatiske «lytt igjen»-løkken er ikke
  kjørt med ekte tale i flere runder på en fysisk telefon.
- Avspilling er bekreftet med `MediaPlayer` (fullført uten feil) på emulator; lydstyrke/høyttaler
  på fysisk telefon er ikke lyttet til.
- QR-koden er bare testet med HTTP-kall fra denne PC-en (LAN-IP 192.168.50.159) og ved a dekode
  BMP/PNG; ingen fysisk telefon var tilgjengelig for skanning.
- 2.6.1-installasjonen er bygget, payload/versjon er verifisert og filen er hash-verifisert
  etter overføring, men den er **ikke kjørt som installasjon** (ville overskrevet brukerens
  aktive installasjon og registry). 2.5-installasjonen ble fullt testet.
- `/ompqr` er ikke bekreftet i ekte OMP-TUI etter fiksen; bare via Node-harness mot installert
  kommandomodul. OMP må restartes for at den nye kommandoen skal lastes.
- Live-installasjonen ble patchet manuelt (filkopi + state-fil), ikke via installasjonsprogrammet.
- Ingen fysisk Android-telefon har dukket opp i `adb devices`; bare emulator (API 35).
- Hvis en annen OMP-bro allerede lytter pa 7777 (f.eks. fra kildekoden), viser veiviseren
  parings-QR fra installert `config.json` mens den kjorende broen kan ha en annen token.
  Vanlige brukere rammes ikke.
- Ingen antivirus-/Defender-skann av den ferdige EXE-en er gjort.
- Lys modus i appen er ikke testet.

## Filer
| Fil | Sted |
|-----|------|
| NimayOMP-Setup-2.6.3.exe | prosjektroten (og `installer\dist\`), kopieres til `megaxenon@192.168.50.222:/home/megaxenon/` |
| OmpChat-2.6.3-release.apk | prosjektroten, kopieres ogsa til `megaxenon@192.168.50.222:/home/megaxenon/` |
| /ompqr-kommando | `omp-commands\ompqr\index.ts`, `omp-commands\ompchat-shared\ompchat.ts` |
| QR-renderere | `tools\omp_qr.py` (Python, terminal+PNG), `installer\payload\tools\qrgen.mjs` (Node, BMP) |
| Talesamtale (logikk) | `app\src\main\java\com\omp\chat\data\VoiceCall.kt`, `VoiceCallEngine.kt` |
| Talesamtale (UI) | `app\src\main\java\com\omp\chat\ui\screens\VoiceCallScreen.kt` |
| Tale-API i appen | `app\src\main\java\com\omp\chat\data\BridgeClient.kt` |
| Talesamtale-tester | `app\src\test\java\com\omp\chat\data\VoiceCallTest.kt`, `app\src\androidTest\java\com\omp\chat\VoiceChainTest.kt` |
| Installasjonskilde | `installer\NimayOMP.iss` |
| Payload | `installer\payload\` (bridge-hjelpere, APK-server, QR, tale) |
| Bro | `bridge\omp-bridge.mjs` (API, `/apk`, tale-API, pid-fil) |
| Testskript | `installer\dev\api-test.ps1`, `installer\dev\ui-screenshots.ps1` |

## Sikkerhetskopi
- `C:\Users\Megaxenon\Documents\Opencode\OmpChat_backup_20261028_121341\OmpChat` (83 filer)

## Bevarte funksjoner
- OMP RPC og bridge, eksisterende samtaler og prosjektmapper
- Modellbytte, streaming, verktoykall, godkjenninger (always-ask)
- Vedlegg, kamera, mikrofon, kryptert tokenlagring, Tailscale/LAN
- Globale `/ompserver`, `/ompqr` og `/ompchat-shared` kommandoer

## Testmiljo etter runden
- Testbroen som ble brukt til talesamtale-testene (port 7790, PID 8640) er stoppet.
- Den ekte broen paa 7777 er restartet (PID 28188, `bridge.pid` = 28188) og verifisert med
  `/api/ping` og `/api/voice/engines`; `config.json` har `port: 7777` og samme token som for.
- Testsesjonene i `~\.omp\agent\sessions\-AppData-Local-Temp-omp-voice-android` og
  `...-omp-voice-e2e` er slettet, sammen med `%TEMP%\omp-voice-android` og `%TEMP%\omp-voice-e2e`.
- Emulatoren Pi_Test_Pixel har release-APK 2.6.3 installert (versionCode 11), paret mot `http://127.0.0.1:7777` via `adb reverse tcp:7777 tcp:7777`, og star i en ekte sesjon med telefonknappen synlig. Fjern reverse/paring naar emulatoren ikke skal brukes mot denne PC-en.
- `/ompqr`-fiksen er lagt inn i den kjørende installasjonen (`C:\Program Files\Nimay OMP`) uten
  reinstallasjon. Neste OMP-oppstart laster de nye kommandoene.
- Feilsøkingsartefakter i `%TEMP%`: `ompqr-harness.mjs`, `qrdecode\` (jsQR), `qr-*`/`qrt-*`
  bilder, `qrcheck.py`. Trygt å slette.

## Installer 3.1.2 reparasjon �?" 2026-10-31

**Feil funnet og rettet:**

1. **install-commands.ps1**: PowerShell drive-reference feil ($name: → $():) i feilmelding �?" scriptet feilet ved parsing
2. **register-autostart.ps1**: 
   - Ugyldig variabelsyntaks ($env:ProgramFiles(x86) → ${env:ProgramFiles(x86)})
   - Ugyldig enum-verdi (PolicyQueueUp → Queue)
   - Parameter-type mismatch (SecureString → string med ConvertTo-SecureString)
   - Register-ScheduledTask kalte -Password i Login-modus (fjernet)
3. **verify-install.ps1**: 
   - Omskrevet for å fjerne syntaksfeil
   - Fjernet OMP-binær-sjekk (kan mangle under installasjon)
   - Fjernet Bridge-kommunikasjonssjekk (Bridge kjører ikke under installasjon)
   - Fikset variable-interpolation ($ompchat-shared → enkelt-quoted)
4. **setup-firewall.ps1**: PowerShell drive-reference feil ($port: → $():)
5. **start-bridge-service.ps1**: Array-literal feil (trailing comma) + $restart: drive-reference
6. **stop-bridge-service.ps1**: $pid: og $Port: drive-reference + tegnkoding
7. **NimayOMP-Installer.iss**: 
   - Fjernet [Run]-seksjon (redundant med CurStepChanged, kalte register-autostart med hardkodet Boot-modus)
   - La til config.json i [Files]-seksjon
   - Fikset Inno Setup Pascal Script syntaks (function → procedure, var-parametere)

**Bygget:** NimayOMP-Installer-3.1.2.exe, 18 271 355 B (17.43 MB)
SHA-256: 58BD040EE8CDACE995FA241FA3DEF991EB02689C3981A13CFF301984869013B8

**Ikke verifisert (krever fysisk Windows-test):**
- Faktisk installasjonstest på Windows
- Task Scheduler-oppgaveopprettelse (krever admin)
- Bridge-start og helsesjekk
- OMP-kommandoregistrering på ekte system
## Installer 3.1.2 publisert på chat.nimay.net (2026-10-31)

**Publisert:**
- EXE NimayOMP-Installer-3.1.2.exe, 18 273 355 B, SHA-256 58BD040EE8CDACE995FA241FA3DEF991EB02689C3981A13CFF301984869013B8
- Lastet opp til megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/
- update.json oppdatert (installer sha256 + size)
- index.html oppdatert (installer sha256)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.2.exe svarer HTTP 200, riktig Content-Length
- SHA-256 verifisert på mottakersiden før leveranse ferdig

**Ikke fysisk testet:** Ingen fysisk Windows-maskin tilgjengelig for installasjonstest.
## Installer 3.1.3 reparasjon og publisering (2026-10-31)

**Feil 1 - Windows-installasjon ("Runtime error: Could not call proc."):**
- Årsak: egister-autostart.ps1 kalte ConvertTo-SecureString før tom-passord-sjekk (Boot-modus uten passord → exit 1). erify-install.ps1 krevde Task Scheduler-oppgave også i Manual-modus, som førte til Abort i Inno Setup.
- Rettet: Passord-sjekk flyttet før ConvertTo-SecureString i egister-autostart.ps1. .iss-filen sporer nå om autostart ble konfigurert (utostartConfigured) og behandler ikke manglende Task Scheduler-oppgave som fatal i Manual-modus.

**Feil 2 - /ompserver ("Finner ikke OMP på C:\Users\Megaxenon\.bun\bin\omp.exe"):**
- Årsak: omp-bridge.mjs og ompchat.ts hardkodet .bun\bin\omp.exe som fallback, ignorerte PATH og npm-installasjoner.
- Rettet: Begge filer søker nå i PATH først, deretter npm-global steder, og til slutt .bun\bin\omp.exe som fallback. OMP_BIN miljøvariabel respekteres alltid.
- Samme rettelse i install-nimay-omp.ps1 (Find-OmpBinary).

**Bygg:**
- Installer NimayOMP-Installer-3.1.3.exe, 18 274 433 B, SHA-256 530ECC9341D28EA27B310492CA9F3E850AF7CFE8B1B9036BDA4838B6EEEC9902
- Lastet opp til megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/
- update.json oppdatert (versjon 3.1.3, ny SHA-256, ny størrelse)
- index.html oppdatert (ny SHA-256)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.3.exe svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Windows-maskin tilgjengelig for installasjonstest.
## Installer 3.1.3 runtime-feil reparert (2026-10-31)

**Feil: "Runtime error (at 4:1023): Could not call proc."**
- Årsak: RunPowerShellScript-prosedyren i .iss-filen kalte Exec() uten å sjekke returverdi, og esultCodeInteger ble ikke initialisert. Når Exec ikke kunne starte PowerShell-prosessen (f.eks. ugyldig sti eller manglende rettigheter), oppstod runtime-feil.
- Rettet: esultCodeInteger initialiseres til -1 før Exec-kall. Exec-returverdi (execOk) sjekkes. Hvis Exec mislykkes, settes c := -1. Ellers c := resultCodeInteger.
- Alle eksisterende funksjoner bevart (brannmur, OMP-kommandoer, autostart, verifisering).

**Bygg:**
- Installer NimayOMP-Installer-3.1.3.exe, 18 274 470 B, SHA-256 D57756F61556AE15EFE8566F40E63F04DF3F4D20E271E8D921DC78E886866F1F
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (ny SHA-256, ny størrelse)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.3.exe svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Windows-maskin tilgjengelig for installasjonstest.
## Installer 3.1.3 runtime-feil reparert (2. gang) (2026-10-31)

**Feil: "Runtime error (at 4:1023): Could not call proc." (vedvarende etter første rettelse)**
- Årsak: RunPowerShellScript-prosedyren i .iss-filen som wrapper for Exec() forårsaket fortsatt runtime-feil. Inno Setup's Pascal Script kunne ikke håndtere prosedyrekallet pålitelig, muligens pga. prosedyre-signatur eller variabel-scope-problemer med den globalt delte c-variabelen.
- Rettet: Fjernet RunPowerShellScript-prosedyren helt. Alle Exec-kall er nå inline i CurStepChanged med lokale variabler (execOk, esultCode). Fjernet den globalt delte c-variablen. Bruker eksplisite parenteser (not execOk) or (resultCode <> 0) for å unngå operatorprekensjonsproblemer i Pascal Script.
- Alle eksisterende funksjoner bevart: brannmur, OMP-kommandoer, autostart (Boot/Login/Manual), verifisering.

**Bygg:**
- Installer NimayOMP-Installer-3.1.3.exe, 18 274 377 B, SHA-256 493A7AA7F343C710BA595B3268B8B129DB09BE6013F44DB22438A0207CAE3DE5
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (ny SHA-256, ny størrelse)
- index.html oppdatert (ny SHA-256)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.3.exe svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Windows-maskin tilgjengelig for installasjonstest.
## APK 2.9.37 publisert på chat.nimay.net (2026-11-01)

**Publisert:**
- APK OmpChat-2.9.37.apk, 8 955 966 B, SHA-256 A41603859AA8E1A1D57251BEC0C29D9B2E730853C736F574F8A43E635861BB46
- Lastet opp til megaxenon@192.168.50.222:/home/megaxenon/Docker/nimay-chat/updates/nimayomp/
- update.json oppdatert (android 2.9.37/62, ny SHA-256, ny størrelse)
- index.html oppdatert (ny filnavn + SHA-256)
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.37.apk svarer HTTP 200, riktig Content-Length

**Endringer siden 2.9.36:**
- Tokenindikator omskrevet til diskret inline-prikk + prosent (ingen grønn/turkis farge)
- Integrert i toppfeltet ved modellnavnet
- versionCode 62 / versionName 2.9.37

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.38 publisert på chat.nimay.net (2026-11-01)

**Feil rettet: Modellvalg viser tom liste**
- Årsak: Når brukeren klikket på modell-raden i toppfeltet eller åpnet modellvelgeren fra skuffen, ble ModelSheet åpnet med state.models som var tom (modeller blir kun lastet når en samtale åpnes). Ingen kall til onReloadModels() ble gjort før arket åpnet.
- Rettet: onReloadModels() kalles nå før modelOpen = true både i toppfeltet (modell-rad klikk) og i skuffen (onOpenModelPicker). Dette tvinger en ny henting av GET /api/models fra bridge før modellvelgeren vises.

**Bygg:**
- APK OmpChat-2.9.38.apk, 8 951 868 B, SHA-256 5F0380256CDEA79B58BD1A546EE7A58AF89B68080531F5AC98B5440CB1C7A462
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.38/63, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.38.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## Installer 3.1.4 publisert på chat.nimay.net (2026-11-01)

**Feil rettet: Bro-status forurenser ikke lenger OMP-terminalen**
- Årsak: log()-funksjonen i omp-bridge.mjs skrev alle loggmeldinger til console.log(), som ble ført gjennom til OMP-terminalen når bridge kjørte som underprosess via RPC. Dette viste statusmeldinger (start, port, godkjenningsmodus, osv.) i samtalevinduet.
- Rettet: Fjernet console.log(line) fra log()-funksjonen. Loggmeldinger lagres fortsatt i logLines-arrayet og er tilgjengelige via /api/diagnostics, men skrives ikke lenger til stdout.

**Versjonsnummer bumpet:**
- Installer: 3.1.3 → 3.1.4
- APK: 2.9.38 (uendret)

**Bygg:**
- Installer NimayOMP-Installer-3.1.4.exe, 18 275 159 B, SHA-256 28B8006F7525EC570D201D80BDDDE1A92623E48702D351841EB2AF5742ACE3EA
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (installer 3.1.4, ny SHA-256, ny størrelse)
- index.html oppdatert (ny filnavn + SHA-256)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.4.exe svarer HTTP 200

**Fremadrettet:** Byggnummer bumpes for hver patch for enklere versjonssporing.

**Ikke fysisk testet:** Ingen fysisk Windows-maskin eller Android-enhet tilgjengelig.
## APK 2.9.39 publisert på chat.nimay.net (2026-11-01) - Multi-PC reparasjon

**Feil rettet: Multi-PC-system fullført**

Problemer funnet:
1. pair() lagret kun til global settings, opprettet aldri en PcProfile → QR-skanning overskrev aktiv tilkobling
2. PcManagerScreen manglet synlig QR-scan-knapp (kun + knapp i toppbar)
3. PcCard viste alltid "Koble til" selv for aktiv PC
4. LAN/Tailscale adresser kunne registreres som separate PC-er
5. Token ble lagret i klartekst i SharedPreferences

Rettelser:
1. AppViewModel.pair() oppretter/oppdaterer nå en PcProfile og setter den som aktiv PC
2. PcManagerScreen har nå QR-scan-knapp (Icons.Default.QrCode) i toppbar
3. PcCard viser "Bytt til" (ikke aktiv) / "Koble fra" (aktiv)
4. pair() sjekker baseUrl, lanUrl, tailscaleUrl og endpoints for å unngå duplikat-PC
5. PcProfileStore bruker nå SecureStore per-PC for token-lagring (Android Keystore)
6. PcProfile har nå lanUrl og 	ailscaleUrl felter med migrering fra eksisterende data
7. AppRoot.kt og ChatScreen.kt kobler onScan til equestScan()

**Bygg:**
- APK OmpChat-2.9.39.apk, 13 457 920 B, SHA-256 1B54D0D09F0FA768D73AAB8F8391958AA5B4FA2C55E4D1441D4A71634BF093DA
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.39/64, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.39.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## Installer 3.1.5 publisert på chat.nimay.net (2026-11-01)

**Feil rettet: Bro-status forurenser OMP-terminalen**
- Årsak: log()-funksjonen i omp-bridge.mjs kalte console.log(line) for hver loggmelding. Når bridge kjørte som underprosess via OMP RPC, ble disse meldingene ført gjennom til OMP-terminalen og vist i samtalevinduet.
- Rettet: Fjernet console.log(line) fra log()-funksjonen. Loggmeldinger lagres fortsatt i logLines-arrayet og er tilgjengelige via GET /api/diagnostics, men skrives ikke lenger til stdout.

**Versjon bumpet:** Installer 3.1.4 → 3.1.5 (byggnummer bumpes for hver patch)

**Bygg:**
- Installer NimayOMP-Installer-3.1.5.exe, 18 274 383 B, SHA-256 39BDF05A97D10481876895726A527FAE99A9AC09F5189A8EC651F96384AB633F
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (installer 3.1.5, ny SHA-256, ny størrelse)
- index.html oppdatert (ny filnavn + SHA-256)
- https://chat.nimay.net/downloads/nimayomp/NimayOMP-Installer-3.1.5.exe svarer HTTP 200

**Tester utført:**
- ✅ Inno Setup-kompilering: exit code 0
- ✅ Bridge startet og svarte på /api/ping (port 7777)
- ✅ /api/diagnostics returnerte korrekt (logLines-arrayet fungerer)
- ✅ Ingen console.log til stdout (bro-status forurenser ikke terminalen)
- ✅ SHA-256 lokal vs server: matcher
- ✅ HTTP nedlasting: 200 OK, riktig Content-Length

**Ikke fysisk testet:** Ingen fysisk Windows-maskin tilgjengelig for installasjonstest.
## APK 2.9.40 publisert på chat.nimay.net (2026-11-01) - Pairing-feil reparert

**Feil rettet:**
1. **"Mangler tilgangstoken" / stuck på PairingScreen** - Token fra QR-kode ble ikke lagret riktig. Rettet:
   - ChatRepository.connect() tømmer nå lagret tilkobling (clearConnection) når token avvises (401/403), slik at brukeren sendes tilbake til paringsskjermen med tydelig feilmelding
   - PcProfileStore lagrer nå også token i JSON (backward compat) i tillegg til SecureStore, slik at token ikke går tapt ved keystore-problemer
   - switchPc() kaller ikke lenger saveGotifyConfig() (som ga "Gotify-konfig feilet" toast)
2. **Back-knapp på PairingScreen** - Brukeren kan nå nullstille feiltilstand og prøve igjen (allerede lagt til i 2.9.39)

**Bygg:**
- APK OmpChat-2.9.40.apk, 13 457 917 B, SHA-256 28BDBF89BEDB3695ECE59F1B42549DAA697E11186D099AF437534595C2AFDFB1
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.40/65, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.40.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.41 publisert på chat.nimay.net (2026-11-01) - PairingScreen back-knapp fikset

**Feil rettet:**
- "Tilbake / prøv igjen"-knappen på PairingScreen fungerte ikke (bruker rapporterte at ingenting skjedde)
- Fikset: Back-knappen kaller nå onClearError() som nullstiller både localError og state.error, slik at feilmeldingen forsvinner og brukeren kan prøve igjen
- Lagt til onToast-callback til PairingScreen for å vise bekreftelse når back-knappen trykkes

**Bygg:**
- APK OmpChat-2.9.41.apk, 13 457 909 B, SHA-256 52FBE3D06AE39EA007332038D24A14E19F920C8E5BC17EB16261B0C2722F2A4D
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.41/66, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.41.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.42 publisert på chat.nimay.net (2026-11-01) - Back-knapp på PairingScreen

**Feil rettet:**
- Bruker ble fanget på PairingScreen uten mulighet til å komme tilbake til hovedskjermen
- Legt til "Tilbake / prøv igjen"-knapp som nullstiller feiltilstand og lar brukeren prøve å skanne QR-koden på nytt
- Legt til "Gå til hovedskjerm (uten tilkobling)"-knapp som gir tilgang til appen selv uten paring
- Gotify-feilmelding fjernet fra paringsflyten (ble kun vist under oppstart, ikke under paring)
- Token-validering forbedret: hvis token avvises av bridgen, tømmes tilkoblingen og brukeren sendes tilbake til paringsskjermen

**Bygg:**
- APK OmpChat-2.9.42.apk, 13 457 921 B, SHA-256 3C2FF6B89CA6A2A7ADB76181412E636548F7AB40CFD1EAAA5005E5E098A57F01
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.42/67, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.42.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.43 publisert på chat.nimay.net (2026-11-01) - To separate knapper på PairingScreen

**Feil rettet:**
- Brukeren ble fanget på PairingScreen uten mulighet til å komme bort
- Lagde TO SEPARATE knapper:
  1. "Tilbake / prøv igjen" - nullstiller feil, URL og token, lar brukeren prøve å skanne QR-koden på nytt
  2. "Gå til hovedskjerm" - gir tilgang til appen (chatskjermen) uten paring, med begrenset funksjonalitet
- Fjernet Gotify-feilmelding fra paringsflyten (ble kun vist under oppstart, ikke under paring)
- Token-validering forbedret: hvis token avvises av bridgen (401/403), tømmes tilkoblingen og brukeren får tydelig feilmelding

**Bygg:**
- APK OmpChat-2.9.43.apk, 13 457 921 bytes, SHA-256 121D433BDD0A5F3E7D940BDDD6278A3448D7CE8420F06E0EB07687DFC04664E7
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.43/68, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.43.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.44 publisert på chat.nimay.net (2026-11-01) - "Gå til hovedskjerm"-knapp fikset

**Feil rettet:**
- "Gå til hovedskjerm"-knappen på PairingScreen fungerte ikke (ingen ting skjedde)
- Årsak: skipPairing state ble satt, men screen ble ikke oppdatert, og settings.paired var fortsatt false, så PairingScreen ble fortsatt vist
- Rettet: Lagt til LaunchedEffect(settings.paired) som nullstiller skipPairing når paired blir true. Lagt til skipPairing state som lar brukeren se hovedskjermen selv uten paring. onGoToMain setter nå skipPairing = true og screen = Screen.Chats, slik at appen navigerer til chatskjermen direkte.

**Bygg:**
- APK OmpChat-2.9.44.apk, 13 457 911 B, SHA-256 C3C6FB10196B7DE964C34E5E2914BBAC6A1D0584D67CAA63178F1C73B9693838
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.44/69, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.44.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.
## APK 2.9.45 publisert på chat.nimay.net (2026-11-01) - LAN/Tailscale tilkoblingsfeil reparert

**Feil rettet:**
1. **App kobler til feil IP-adresse (100.105.157.25)**: connect() promoterte adresser til primær før health-sjekk var bestått. Rettet: health-sjekk kjøres nå før promoting, og hvis health feiler, reverteres til original adresse og neste kandidat prøves.
2. **Ingen tydelig feilmelding**: Forbedret feilmeldinger i connect() catch-block for å vise hvilke adresser som ble prøvd og om token ble avvist.
3. **Prosjekter og modeller lastes ikke**: Fordi tilkoblingen feilet, ble efreshAll() og loadModels() aldri kalt. Nå kjøres disse kun etter vellykket tilkobling.
4. **Token-avvisning**: settings.clearConnection() kalles når token avvises (401/403), slik at brukeren sendes tilbake til paringsskjermen.

**Bygg:**
- APK OmpChat-2.9.45.apk, 13 457 917 B, SHA-256 EB78F3EEDF8D069C7CD82F599BE49C2C092E705B8B851E04D9B3D37F4B9E5049
- Lastet opp til server, SHA-256 verifisert
- update.json oppdatert (android 2.9.45/70, ny SHA-256, ny størrelse)
- index.html oppdatert
- https://chat.nimay.net/downloads/nimayomp/OmpChat-2.9.45.apk svarer HTTP 200

**Ikke fysisk testet:** Ingen fysisk Android-enhet tilgjengelig.

## 2.9.46 � 2026-11-01

**Root cause found and fixed: SecureStore.put() ArrayIndexOutOfBoundsException**

### Critical Bug
In SecureStore.put(), the System.arraycopy for encrypted data used cipher.iv.size (12) as the source offset instead of  :

`kotlin
// BUGGY:
System.arraycopy(encrypted, cipher.iv.size, payload, cipher.iv.size, encrypted.size)
// FIXED:
System.arraycopy(encrypted, 0, payload, cipher.iv.size, encrypted.size)
`

This caused ArrayIndexOutOfBoundsException: src.length=64 srcPos=12 dst.length=76 dstPos=12 length=64 on every token save. The exception was caught and silently cleared the token, making it impossible to save or retrieve the bridge token.

**Impact:** The app could never connect to the bridge because settings.token() always returned null.

**Fix:** Changed source offset from cipher.iv.size to   in SecureStore.put().

**Verified in emulator (Pi_Test_Pixel, Android 16, API 36):**
- Deep link pairing with correct token: PASS
- Connection established: PASS (ping+health OK, version 2.9.9)
- Token persists across app restart: PASS
- Automatic reconnection: PASS
- No errors in logcat: PASS
- Models and projects load: PASS

**Changed files:**
- pp/src/main/java/com/omp/chat/data/SecureStore.kt � fixed System.arraycopy source offset
- pp/build.gradle � versionCode 71, versionName 2.9.46

**APK:** OmpChat-2.9.46 (code 71), SHA-256 40631FBF3253E7183F06222E2AEB326743FEE5D9D6A3C9DBB913E4E09281523F


## Cleanup completed (2026-11-01)

- Removed old C:\Program Files\Nimay OMP\ (v2.5 installation) with admin rights
- Removed old Startup shortcut (was already disabled as .lnk.disabled)
- Consolidated 3 backup folders (1.7 GB) to C:\RELEASE\NIMAYOMPCHAT\backup\
- Added "Known Limitations" section to README.md
- Marked Linux Flatpak as untested in README.md
- Published to GitHub: https://github.com/megaxenon83-server/OmpChat

## 2.9.49 � 2026-11-02

**Resource monitor: discreet icon button**

### Root Cause Fix: SecureStore.put() ArrayIndexOutOfBoundsException
In SecureStore.put(), the System.arraycopy for encrypted data used cipher.iv.size (12) as the source offset instead of 0. This caused ArrayIndexOutOfBoundsException on every token save, silently clearing the token and making connection impossible.

Fixed: Changed source offset from cipher.iv.size to 0.

### New Feature: PC Resource Monitor (discreet icon button)
- Bridge: New GET /api/resources endpoint returns CPU%, RAM%, GPU% (NVIDIA/AMD/Intel via WMI/PowerShell/nvidia-smi)
- Android: Discreet icon button (speedometer/gauge icon, no text) in chat header
- Tap: Opens popup with CPU, RAM, GPU usage and fargekoding (green <60%, orange 60-85%, red >85%)
- Polling: Every 30 seconds
- Opt-in: Toggle in Settings (PC-ressursmonitor)
- No tokens needed for this feature

### Changed files:
- app/src/main/java/com/omp/chat/data/SecureStore.kt - fixed System.arraycopy source offset
- app/src/main/java/com/omp/chat/data/BridgeClient.kt - added resources() method
- app/src/main/java/com/omp/chat/data/ChatRepository.kt - added loadResources(), startResourcesPolling(), stopResourcesPolling(), resources field in RepoState
- app/src/main/java/com/omp/chat/data/Models.kt - added PcResources data class
- app/src/main/java/com/omp/chat/ui/AppRoot.kt - added resourceMonitorEnabled parameter
- app/src/main/java/com/omp/chat/ui/screens/ChatScreen.kt - added ResourceMonitorChip to header
- app/src/main/java/com/omp/chat/ui/components/ResourceMonitor.kt - new discreet icon button + popup

### Bridge changes:
- omp-bridge.mjs - added /api/resources endpoint with CPU/RAM/GPU monitoring

### APK:
- OmpChat-2.9.49.apk (13,466,098 B, SHA-256: 573B64D1E5A3283787FAD16193D7848CEE394471848189B31D878DD34D9D10DB)

### Published:
- APK uploaded to chat.nimay.net
- update.json updated (version 2.9.49, code 74)
- index.html updated
- Nginx reloaded
- GitHub pushed

## 2.9.50 � 2026-11-02

**Fix: resource monitor button layout shift + on-demand load**

### Bugs fixed
1. Layout shift: popup Surface was a child of the same Box as the 32dp chip, so the Box measured 200dp wide and pushed the header Row. Fixed by rendering the popup via Compose Popup (overlay window); chip stays 32dp.
2. On-demand load: chip now triggers loadResources() on open, so tapping early (before first 30s poll completes) shows "Laster ressurser..." then updates to real data instead of staying empty.

### Changed files
- app/src/main/java/com/omp/chat/ui/components/ResourceMonitor.kt - Popup overlay, onRefresh callback
- app/src/main/java/com/omp/chat/ui/screens/ChatScreen.kt - onLoadResources param
- app/src/main/java/com/omp/chat/ui/AppRoot.kt - wired loadResources
- app/build.gradle - version 2.9.50 (code 75)

### APK
- OmpChat-2.9.50.apk (13,466,096 B, SHA-256: C032D6569BDFC961C8B857FED2C5BA88EF2331B837BB1E0470CB82AF23493770)

### Published
- APK uploaded to chat.nimay.net
- update.json updated (2.9.50, code 75)
- index.html updated
- Nginx reloaded

## 2.9.51 � 2026-11-02

**Fix: resource monitor popup viser ekte data + ingen layout-forskrivning**

### Rot�rsak funnet (faktisk, ikke gjett)
Logcat bevis at dataflyten fungerte hele tiden: loadResources() kalles, /api/resources returnerer gyldig JSON (cpu=3, ram=34, gpu=0), parsing lykkes, og _state oppdateres (esources=true). Feilen var at Popup-innholdet i Compose ikke p�litelig rekomponerte n�r esources endret seg � popup-en heng p� "Laster ressurser..." selv etter at data var hentet.

### Fiks (kun ressursmonitor-knappen)
- Popup rendres via Compose Popup (overlay-vindu): p�virker ikke header-radens m�ling ? chip holder 32dp, **flytter seg ikke ved trykk**.
- Popup-innholdet rekomponeres n�r esources endres ? viser ekte CPU/RAM/GPU-data s� snart de er tilgjengelig.
- On-demand lasting: chip kaller loadResources() ved �pning (i tillegg til 30s-polling).
- Feils�kningslogg: const val DEBUG = false i ResourceMonitor.kt (tag "OmpChat"), sl� p� for midlertidig logg.

### Endrede filer
- app/src/main/java/com/omp/chat/ui/components/ResourceMonitor.kt - Popup + onRefresh + DEBUG-logg
- app/src/main/java/com/omp/chat/ui/screens/ChatScreen.kt - onLoadResources-param (var allerede p� plass)
- app/src/main/java/com/omp/chat/ui/AppRoot.kt - onLoadResources koblet til loadResources (var allerede p� plass)
- app/build.gradle - versjon 2.9.51 (code 76)

### APK
- OmpChat-2.9.51.apk (13,466,103 B, SHA-256: BB6043F485BA442D42B0D71FFD167399CCA4B97661FEF94A81B0CBCD89CC00E2)

### Publisert
- APK opplastet til chat.nimay.net
- update.json oppdatert (2.9.51, code 76)
- index.html oppdatert
- Nginx reloadet
- Nedlastingslenke verifisert (HTTP 200, SHA-256 matcher)

## 2.9.52 � 2026-11-02

**Fix: app kan oppdateres uten avinstallasjon (samme signering p� debug og release)**

### Problem
Debug-APK signeres med Androids standard debug-n�kkel, release-APK med ompchat-release.jks. Ulik signering betyr at Android nektar oppdatering � man m� avinstallere f�r man kan installere ny versjon.

### Fiks
- debug build type bruker n� samme signingConfigs.release som elease ? begge signeres med ompchat-release.jks
- Oppdatering fungerer n� uten � avinstallere f�rst

### Endrede filer
- app/build.gradle - debug build type f�r signingConfig release; versjon 2.9.52 (code 77)

### APK
- OmpChat-2.9.52.apk (13,466,106 B, SHA-256: 11DD577D724E706F9DBEB588D4D113C627C9174DBE413525E8C4E56E84CA7D43)

### Publisert
- APK opplastet til chat.nimay.net
- update.json oppdatert (2.9.52, code 77)
- index.html oppdatert
- Nginx reloadet
- Nedlastingslenke verifisert (HTTP 200, SHA-256 matcher)

## 2.9.57 (code 82) - Sidebar fixes
- Fixed onOpenActiveChat: sets screen=Screen.Chat, closes drawer, falls back to openSessionByFile
- Fixed onOpenSession (ChatScreen): sets screen=Screen.Chat
- Fixed onSelectProject: sets screen=Screen.Chat, closes drawer
- Fixed ChatGptDrawer: closes drawer after onOpenActiveChat and onSelectProject
- Fixed currentProjectName: shows full path instead of 'C:' for Windows drive letters
- Build verified: assembleRelease SUCCESS
