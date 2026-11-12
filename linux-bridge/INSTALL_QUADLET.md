# Installasjonsguide: Bazzite / Silverblue / uBlue / uCore (Quadlet)

## Hva er Quadlet?
Quadlet er systemd-integrasjon for Podman-containere. Det lar deg kjøre containere som systemd-tjenester uten rot-skrift til filsystemet – perfekt for immutable OS som Bazzite, Silverblue, uBlue, uCore, Fedora Silverblue.

---

## Forutsetninger

- Bazzite / Fedora Silverblue / uBlue / uCore / uCore
- Podman + systemd (standard på alle ovennevnte)
- Bruker med sudo-tilgang

---

## Installasjon (som root)

```bash
# 1. Kopier Quadlet-fil til systemd
sudo cp nimay-omp-bridge.container /etc/containers/systemd/

# 2. Oppdater brukernavn i .container-filen (erstatt <BRUKER> med ditt brukernavn)
sudo sed -i 's/<BRUKER>/din_brukernavn/g' /etc/containers/systemd/nimay-omp-bridge.container

# 3. Last systemd-konfigurasjon
systemctl daemon-reload

# 4. Aktiver og start tjenesten
systemctl enable --now nimay-omp-bridge
```

---

## Verifiser installasjon

```bash
# Sjekk status
systemctl is-active nimay-omp-bridge
# → active

systemctl is-enabled nimay-omp-bridge
# → enabled

# Sjekk port
ss -tln | grep 7802
# → LISTEN 0.0.0.0:7802

# Test API
TOKEN=$(cat /opt/nimay-omp-bridge/token)
curl -H "Authorization: Bearer $TOKEN" http://127.0.0.1:7802/api/ping
# → {"ok":true,"name":"omp-bridge","version":"2.9.24","port":7802,...}
```

---

## Konfigurasjon

Token genereres automatisk på første oppstart. Finn token:
```bash
cat /opt/nimay-omp-bridge/token
```

### Endre konfigurasjon
Rediger `/opt/nimay-omp-bridge/config.json` (krever restart):
```bash
sudo systemctl restart nimay-omp-bridge
```

### Loggføring
```bash
journalctl -u nimay-omp-bridge -f
```

---

## Oppdatering
```bash
# Hent ny container-image
podman pull ghcr.io/nimay/omp-bridge:latest

# Eller bygg lokalt
podman build -t nimay-omp-bridge:latest -f Containerfile .

# Restart for å bruke nytt image
systemctl restart nimay-omp-bridge
```

---

## Fjerning
```bash
systemctl stop nimay-omp-bridge
systemctl disable nimay-omp-bridge
sudo rm /etc/containers/systemd/nimay-omp-bridge.container
systemctl daemon-reload
```

---

## Funksjoner

| Funksjon | Støttet |
|----------|---------|
| Autostart ved boot | ✅ |
| Ikke-root bruker | ✅ (kjøres som din bruker) |
| Tilgang til `~/.omp` | ✅ (volume mount) |
| `omp` i PATH | ✅ |
| Auto-oppdatering | ✅ `podman auto-update` |
| Logg i journalctl | ✅ `journalctl -u nimay-omp-bridge -f` |

---

## Feilsøking

| Problem | Løsning |
|---------|---------|
| Container starter ikke | `journalctl -u nimay-omp-bridge -f` |
| Port 7802 allerede i bruk | `ss -tlnp | grep 7802` |
| Ingen tilgang til `~/.omp` | Sjekk at `User=<din_bruker>` i `.container` |
| Ingen tilgang til `omp` | Sjekk at `PATH` i `.container` inneholder brukerens bin-mapper |

---

## Avansert: Bygg eget image

```bash
# Bygg lokalt
podman build -t nimay-omp-bridge:latest -f Containerfile .

# Oppdater .container-fil til å bruke lokalt image:
# Image=nimay-omp-bridge:latest

# Eller publiser til GHCR:
podman push localhost/nimay-omp-bridge:latest ghcr.io/nimay/omp-bridge:latest
# Oppdater .container: Image=ghcr.io/nimay/omp-bridge:latest
```

---

## Sikkerhet

- Kjører som din bruker (ikke root)
- Volume mounts med `:Z` (SELinux-konforme)
- Token i `/opt/nimay-omp-bridge/token` (0600)
- Kun port 7802 eksponert (host network)