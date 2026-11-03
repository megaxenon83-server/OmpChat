# OmpChat / Nimay OMP – Prosjektstatus

Sist oppdatert: 2026-11-03 av AI-agent

## Installer-fix (3.2.5)

### Feil funnet og rettet
**Rotårsak:** I `InitializeWizard` (linje ~525) ble `MobilePage` opprettet med `wpInstalling` som `AfterID`:

```pascal
MobilePage := CreateCustomPage(wpInstalling, 'Mobilen', ...);
```

`wpInstalling` er Inno Setup's installasjonsfase-side (vises *under* installasjon, ikke i veiviseren før installering). Den er ikke en gyldig `AfterID` for en egendefinert veiviser-side som opprettes i `InitializeWizard`. Dette forårsaket ugyldig side-referanse → "Could not find page with ID 48557680" umiddelbart etter språkvalg.

**Fix:** Endret `wpInstalling` → `wpReady` (siste veiviser-side før installering, riktig plass for ekstra side). Lagt til `Log`-diagnostikk.

**CreateDownloadPage er KORREKT** (jf. Inno Setup-eksempel `CodeDownloadFiles.iss`: signatur er `Description, DescriptionText, AfterID`; `nil` = `AfterID = 0` = `wpWelcome`).

### Autostart-fix
Autostart-snarlveien i `{userstartup}` pekte tidligere til `Start-OMP-Bridge.cmd` (åpner konsollvindu, bruker `where node` som kan feile i autostart-kontekst). Endret til `wscript.exe` + `start-bridge-hidden.vbs` (skjult launcher som leser `config.json` for port og starter broen uten konsollvindu). Verifisert: broen starter og svarer på `/api/ping`.

### Engelsk språk
Lagt til `Name: "english"; MessagesFile: "compiler:Default.isl"` i `[Languages]`.

### Versjonsbump
AppVersion: 3.2.4 → 3.2.5

## Tester (verifisert 2026-11-03)
| Test | Resultat |
|------|----------|
| Kompilering (ISCC.exe) | BUILD SUCCESSFUL, exit 0 |
| VERYSILENT installasjon til temp-mappe | Fullført, alle filer installert |
| config.json opprettet | OK, token + port 7777 + approvalMode always-ask |
| Skjult bridge-launcher (wscript.exe + VBS) | OK, broen starter og svarer /api/ping (200) |
| Autostart-snarlvei | OK, peker til wscript.exe + start-bridge-hidden.vbs |
| /ompserver-kommando registrert | OK, filer kopiert til ~/.omp/agent/commands/ |
| OMP commands state file | OK, projectRoot + bridge-sti skrevet |

## Publisert fil
- **Fil:** `NimayOMP-Setup-3.2.5.exe`
- **Sted:** prosjektroten + `installer\dist\`
- **Størrelse:** 21.99 MB
- **SHA-256:** `5F7CDAC2C96D0298F01B73AF23AB19DA84ED79E0C0F949555419CE0D6DEECFB0`

## Bygg
- ISCC: `C:\Users\Megaxenon\AppData\Local\Programs\Inno Setup 6\ISCC.exe`
- Byggscript: `installer\build-installer.ps1`
- Kilde: `installer\NimayOMP.iss` (AppVersion 3.2.5)

## Gjenstående
- Manuell GUI-test med norsk/engelsk språkvalg, sideflyt, Tilbake/Neste, komponentvalg, avbryt
- Test installasjon over eksisterende Nimay OMP uten tap av innstillinger
- Publisering til GitHub (bruker bekrefter når klar)