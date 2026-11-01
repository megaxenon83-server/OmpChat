# OmpChat 2.0

Mobilklient for den ekte Oh My Pi (OMP)-agenten på PC-en. Appen kjører ikke
modeller selv og snakker ikke direkte med Ollama — den snakker med en liten
bridge på PC-en som driver `omp --mode rpc` i prosjektmappene dine og strømmer
svarene til telefonen.

- Prosjekt: `C:\Users\Megaxenon\Documents\Opencode\OmpChat\`
- Package: `com.omp.chat` · versjon 2.0 (versionCode 2)
- Plattform: Android 7.0+ (minSdk 24), Kotlin 2.0, Compose Material 3

## Slik henger det sammen

```
Telefon (Compose)  ──HTTP + SSE──▶  bridge/omp-bridge.mjs  ──NDJSON──▶  omp --mode rpc
        │                                   │                              │
        └─ kryptert token i Keystore        └─ config.json                 └─ prosjektmappe
```

- `bridge/omp-bridge.mjs` starter én OMP-prosess per prosjektmappe (maks 4,
  inaktive avvikles), og eksponerer REST + SSE med bearer-token.
- Appen eier ingen samtaler: historikk, titler og verktøykall kommer fra OMP.
  Appen lagrer bare innstillinger og tilgangstokenet (AES/GCM, Android Keystore).
- Godkjenninger fra OMP (`extension_ui_request`) sendes til appen som ark med
  «Godkjenn» / «Godkjenn skriving for denne økten» / «Avslå».

## Kom i gang

1. **Start bridgen på PC-en**

   ```powershell
   cd C:\Users\Megaxenon\Documents\Opencode\OmpChat\bridge
   node omp-bridge.mjs
   ```

   Første kjøring lager `bridge/config.json` med port, token, prosjektrotmapper
   og godkjenningsmodus. Loggen skriver parringsverter uten token.

   Enklere: installer de globale OMP-kommandoene én gang (se under), og bruk
   `/ompserver` i OMP — den starter bridgen løsrevet i bakgrunnen.

2. **Lag QR-kode (valgfritt)**

   ```powershell
   cd ..\tools
   python omp_qr.py                 # bruker token/port fra bridge/config.json
   python omp_qr.py --bridge 100.96.46.66:7777
   ```

   Payload: `omp2://<host>:7777?token=<token>`. `omp://` og `ompchat://`
   godtas også, samt vanlige `http(s)://`-adresser med `?token=`.

3. **Parr telefonen** – skann QR-koden i appen, eller bruk deep link:

   ```powershell
   adb shell am start -a android.intent.action.VIEW -d "omp2://10.0.2.2:7777?token=<token>" -n com.omp.chat/.ui.MainActivity
   ```

4. **Velg prosjektmappe** i appen, start en ny samtale eller åpne en
   eksisterende. Samme økt kan fortsettes både på PC og mobil.

## Globale OMP-kommandoer

OMP 18.12.6 laster TypeScript-kommandoer fra
`%USERPROFILE%\.omp\agent\commands\<navn>\index.ts`. Kildene ligger i
`omp-commands/` og installeres med:

```powershell
powershell -ExecutionPolicy Bypass -File tools\install_omp_commands.ps1
```

Start OMP på nytt etterpå. Kommandoene virker i alle prosjektmapper:

| Kommando | Gjør |
|---|---|
| `/ompserver` | Starter bridgen løsrevet (overlever at OMP lukkes), eller viser status. Starter aldri duplikater. |
| `/ompserver status` | Viser status uten å starte. |
| `/ompqr` | Viser parings-QR i OMP-terminalen (Tailscale-IP først), eller åpner PNG i headless modus. |
| `/ompqr clear` / `/ompqr open` | Skjuler widgeten / åpner PNG-en. |

Detaljer, krav og verifisering: [`omp-commands/README.md`](omp-commands/README.md).
Tokenet skrives aldri i klartekst; QR-en inneholder det, så PNG-en er gitignored.

## Dette kan appen

- Samtaleliste med søk, prosjektfilter, dag/går/tidligere, arkiv og sletting.
- Streaming-svar med Markdown, tabeller, kodeblokker med syntaksfarge og kopi.
- Stopp, regenerer, rediger og send på nytt (ekte OMP `branch` + `prompt`).
- Verktøykort med argumenter/resultat, godkjenning eller avslag, terminal-ark
  som bare kjører kommandoer du selv har skrevet.
- Modellbytte midt i samtalen, oppgavefremdrift, arbeidsstatus.
- Vedlegg: filer, bilder, kamera, flere samtidig.
- Innstillinger: tema (mørkt standard), dynamiske farger, tekststørrelse,
  haptikk, varsler, resonnering, tale-språk, språk (norsk/engelsk), diagnostikk
  med bridge-logg.
- Krasjskjerm: etter en uventet feil viser neste oppstart stakksporet med
  «Kopier feilmelding» / «Fortsett», slik at en telefonfeil kan rapporteres uten adb.

## Bygg

```powershell
cd C:\Users\Megaxenon\Documents\Opencode\OmpChat
# tester + debug
cmd /c "C:\Users\Megaxenon\.gradle\wrapper\dists\gradle-8.9-bin\90cnw93cvbtalezasaz0blq0a\gradle-8.9\bin\gradle.bat testDebugUnitTest assembleDebug --console=plain"
# signert release
cmd /c "C:\Users\Megaxenon\.gradle\wrapper\dists\gradle-8.9-bin\90cnw93cvbtalezasaz0blq0a\gradle-8.9\bin\gradle.bat assembleRelease --console=plain"
```

Ferdig APK: `app/build/outputs/apk/release/app-release.apk`, kopiert til
`OmpChat-2.0-release.apk` i prosjektroten. Signering bruker
`app/ompchat-release.jks` + `keystore.properties` (gitignored, samme nøkkel som
1.0 slik at release kan oppgraderes direkte).

`OmpChat-2.0-release.apk` (2026-10-27) SHA-256:
`FFDE3887DD400B00109C324D4A5810F7C57848A5CA1EB773DF60708A06361AEB`.

Merk: launcher-ikonet er raster-PNG og splash-skjermen bruker et bitmap-ikon
(`values-v31/themes.xml`). Det unngår en SystemUI-krasj i
`SplashscreenIconDrawableFactory` på Android 14/15 ved førstegangsinstallasjon.
Ikonene genereres med `python tools/make_icons.py`.

## Sikkerhet

- Alt går over LAN/Tailscale med bearer-token; ingen offentlige porter åpnes.
- Standard `approvalMode` er `always-ask`. Vil du la OMP skrive filer uten å
  spørre, sett `"approvalMode": "write"` i `bridge/config.json`.
- `allowBash: true` gir terminal-arket; kommandoer krever eksplisitt handling.
- Tokenet ligger kryptert på telefonen og skrives aldri i klartekst i logger.

## Filer

- `app/src/main/java/com/omp/chat/data/` – `BridgeClient`, `ChatRepository`,
  `Models`, `Settings`, `SecureStore`.
- `app/src/main/java/com/omp/chat/ui/` – `AppRoot`, `AppViewModel`, skjermer,
  komponenter (Markdown, meldingsliste, ark), tema og `Strings` (nb/en).
- `app/src/test/java/com/omp/chat/` – JVM-tester (URL-parsing, Markdown,
  verktøykort).
- `bridge/omp-bridge.mjs` – HTTP/SSE-bro mot OMPs RPC.
- `tools/omp_qr.py` – QR-generator for parring (`--terminal` = kompakt QR uten token).
- `tools/install_omp_commands.ps1` – installerer de globale OMP-kommandoene.
- `tools/make_icons.py` – genererer launcher- og splash-ikoner (PNG).
- `omp-commands/` – `/ompserver`, `/ompqr` og felles hjelpemodul.
- `app/src/main/java/com/omp/chat/util/CrashReporter.kt` + `ui/screens/CrashScreen.kt`
  – lagrer og viser forrige krasj.

Se `..\PROJECT_STATUS.md` for hva som er verifisert, og hva som gjenstår på
fysisk telefon.
