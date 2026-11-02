## 2.9.49 — 2026-11-02

**Resource monitor: discreet icon button**

### Root Cause Fix: SecureStore.put() ArrayIndexOutOfBoundsException
In SecureStore.put(), the System.arraycopy for encrypted data used cipher.iv.size (12) as the source offset instead of 0. This caused ArrayIndexOutOfBoundsException on every token save, silently clearing the token and making connection impossible.

Fixed: Changed source offset from cipher.iv.size to 0.

### New Feature: PC Resource Monitor (discreet icon button)
- Bridge: New GET /api/resources endpoint returns CPU%, RAM%, GPU% (NVIDIA/AMD/Intel via WMI/PowerShell/nvidia-smi)
- Android: Discreet icon button (speedometer/gauge icon, no text) in chat header next to model name
- Tap: Opens popup with CPU, RAM, GPU usage and fargekoding (gronn <60%, oransje 60-85%, rod >85%)
- Polling: Every 30 seconds
- Opt-in: Toggle in Settings (PC-ressursmonitor)
- No tokens needed for this feature

### Changed files:
- app/src/main/java/com/omp/chat/data/SecureStore.kt — fixed System.arraycopy source offset
- app/src/main/java/com/omp/chat/data/BridgeClient.kt — added resources() method
- app/src/main/java/com/omp/chat/data/ChatRepository.kt — added loadResources(), startResourcesPolling(), stopResourcesPolling(), resources field in RepoState
- app/src/main/java/com/omp/chat/data/Models.kt — added PcResources data class
- app/src/main/java/com/omp/chat/ui/AppRoot.kt — added resourceMonitorEnabled parameter
- app/src/main/java/com/omp/chat/ui/screens/ChatScreen.kt — added ResourceMonitorChip to header
- app/src/main/java/com/omp/chat/ui/components/ResourceMonitor.kt — new discreet icon button + popup

### Bridge changes:
- omp-bridge.mjs — added /api/resources endpoint with CPU/RAM/GPU monitoring

### APK:
- OmpChat-2.9.49.apk (13,466,098 B, SHA-256: 48B25A13B837FF029629FDCB5790C7B029972E5E7338C9B450C91F80B4376500)

### To publish:
1. Copy APK to server (SSH not available from this machine — manual upload needed)
2. Update update.json on server
3. Update index.html on server
4. Restart nginx
