# Security Architecture

## Threat Model

### Assets Protected
1. **Transferred files**: Screenshots, documents, media
2. **Session keys**: Ephemeral encryption keys
3. **Device identity**: Endpoint IDs, device names

### Attack Surface

```
┌─────────────────────────────────────────────────────────┐
│                    Attack Surface                         │
├─────────────────────────────────────────────────────────┤
│ 1. Network: UDP broadcast, QUIC transfer                  │
│ 2. Key Exchange: ECDH public key transmission             │
│ 3. Storage: Temporary file buffers                        │
│ 4. Memory: Decrypted content in RAM                       │
│ 5. Discovery: Device presence broadcasting                │
└─────────────────────────────────────────────────────────┘
```

## Encryption

### Key Exchange (ECDH P-384)
- Ephemeral key pairs generated per session
- Keys never leave the device unencrypted
- Forward secrecy: past sessions cannot be decrypted
- HKDF-SHA256 derives AES key + IV seed from shared secret

### Data Encryption (AES-256-GCM)
- 256-bit key derived from ECDH shared secret
- 96-bit random nonce per chunk
- 128-bit authentication tag
- Additional Authenticated Data: chunk index

### Integrity (SHA-256)
- Per-chunk hash of plaintext
- Full-file hash in TransferComplete message
- Receiver verifies before saving

## Protocol Security

### Replay Protection
- Unique session ID per transfer
- Monotonically increasing sequence IDs
- Timestamp validation (5-minute window)
- Random nonce per chunk

### Man-in-the-Middle
- ECDH provides authentication via shared secret
- No trusted third party needed
- First-connection MITM mitigated by proximity requirement

### Denial of Service
- Rate limiting on discovery broadcasts
- Cooldown between gesture triggers
- Session timeout after 5 minutes idle
- Max transfer size: 500MB

## Platform Security

### Android
- Hardware-backed keystore for key storage
- Foreground service with minimal permissions
- No INTERNET permission for cloud access
- Sandboxed file access

### Desktop
- OS keychain integration (planned)
- Memory protection via mlock (planned)
- No elevated privileges required
- Sandboxed file dialogs

## Security Checklist

- [x] End-to-end encryption (AES-256-GCM)
- [x] Perfect forward secrecy (ECDH ephemeral keys)
- [x] Integrity verification (SHA-256)
- [x] Replay protection (nonces, sequence IDs)
- [x] No cloud communication
- [x] No persistent key storage
- [x] No logging of sensitive data
- [x] Minimal permission set
- [x] Certificate pinning (for future updates)
- [x] Memory zeroing after use
