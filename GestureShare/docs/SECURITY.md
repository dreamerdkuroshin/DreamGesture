# Security Audit & Threat Model

## Security Architecture

### Encryption Strategy

```
┌─────────────┐     ECDH Key Exchange      ┌─────────────┐
│   Sender     │◄──────────────────────────►│   Receiver    │
│              │                            │               │
│  AES-256-GCM │  ═══ Encrypted Tunnel ═══  │  AES-256-GCM │
│  Ephemeral   │     Chunked Transfer       │  Ephemeral   │
│  Session Key │                            │  Session Key │
└─────────────┘                            └─────────────┘
```

### Key Management

1. **Key Generation**: P-384 ECDH key pairs generated per-session using Android Keystore
2. **Key Exchange**: Ephemeral ECDH public keys exchanged via insecure channel (secure due to DH)
3. **Key Derivation**: Shared secret derived via ECDH, used as AES-256 key
4. **Key Lifecycle**: Keys destroyed after transfer completion
5. **No Persistence**: Keys never written to disk in plaintext

### Transfer Security

- **AES-256-GCM**: Authenticated encryption prevents tampering
- **Per-transfer IV**: Random 12-byte IV generated per encryption operation
- **SHA-256 Verification**: Hash of original data verified post-decryption
- **Chunked Transfer**: Each chunk individually encrypted and verified

## Threat Model

### STRIDE Analysis

| Threat | Risk | Mitigation |
|--------|------|------------|
| Spoofing | Medium | ECDH key exchange authenticates both parties |
| Tampering | Low | AES-GCM provides authenticated encryption |
| Repudiation | N/A | No logging requirement is by design |
| Information Disclosure | Medium | All data encrypted in transit, no cloud storage |
| Denial of Service | Medium | Cooldown mechanism, rate limiting |
| Elevation of Privilege | Low | Standard Android permission model |

### Attack Vectors

#### 1. Man-in-the-Middle
**Risk**: Attacker intercepts key exchange
**Mitigation**: ECDH provides forward secrecy; ephemeral keys discarded after session
**Residual Risk**: First-connection MITM (mitigated by proximity requirement)

#### 2. Replay Attack
**Risk**: Attacker replays previous transfer
**Mitigation**: Unique IV per transfer, ephemeral session keys
**Residual Risk**: Negligible

#### 3. Unauthorized Discovery
**Risk**: Attacker discovers nearby devices
**Mitigation**: mDNS/UDP broadcast limited to local network; no identifying info broadcast
**Residual Risk**: On shared WiFi, device presence detectable

#### 4. Screenshot Interception
**Risk**: Attacker reads screenshot before transfer
**Mitigation**: Foreground service isolates screenshot processing; no temp files written
**Residual Risk**: Rooted devices may access memory

#### 5. Gesture Spoofing
**Risk**: Accidental or malicious gesture triggers transfer
**Mitigation**: >95% confidence threshold, cooldown, direction estimation, sequence support
**Residual Risk**: Very low with multi-frame confidence requirement

### Data Lifecycle

```
Screenshot Created → Detected by ContentObserver → Loaded in Memory →
Gesture Trigger → Encrypted in Memory → Transferred → Deleted from Memory
```

No intermediate files are written to disk.

### Compliance

- **GDPR**: No personal data leaves the device; no tracking
- **No Analytics**: No usage data collected
- **No Network Calls**: All communication is peer-to-peer only
- **No Account**: No user accounts or authentication required

## Security Checklist

- [x] AES-256-GCM for all data in transit
- [x] ECDH P-384 for key exchange
- [x] Android Keystore for key storage
- [x] No persistent storage of sensitive data
- [x] No cloud communication
- [x] Foreground service with minimal permissions
- [x] ProGuard/R8 obfuscation for release builds
- [x] Certificate pinning for any future cloud features
- [x] Biometric auth option for high-security mode
