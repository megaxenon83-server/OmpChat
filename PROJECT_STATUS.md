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