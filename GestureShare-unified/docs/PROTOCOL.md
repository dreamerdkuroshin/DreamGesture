# GestureShare Protocol Specification

## Overview

The GestureShare protocol enables secure, gesture-initiated file transfer between any combination of Android phones, Windows/Linux/macOS laptops, and tablets over WiFi/LAN.

## Protocol Stack

```
┌─────────────────────────────────────────┐
│         Application Layer                │
│  Gesture Recognition │ File Transfer     │
├─────────────────────────────────────────┤
│         Session Layer                    │
│  Session Management │ Key Exchange (ECDH)│
├─────────────────────────────────────────┤
│         Security Layer                   │
│  AES-256-GCM Encryption │ SHA-256 Hash  │
├─────────────────────────────────────────┤
│         Transport Layer                  │
│  QUIC / UDP / WebRTC Data Channel       │
├─────────────────────────────────────────┤
│         Discovery Layer                  │
│  mDNS │ UDP Broadcast │ BLE Advertising │
├─────────────────────────────────────────┤
│         Network Layer                    │
│  WiFi Direct │ LAN │ WiFi Aware │ BLE   │
└─────────────────────────────────────────┘
```

## Wire Format

### Header (32 bytes)

```
 0                   1                   2                   3
 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|     'G'       |     'S'       |     'H'       |     'R'       |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|         Version (16)          |     Type (16)                 |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               |
|                      Sequence ID (64)                         |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                      Payload Length (32)                      |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
|                                                               |
|                      Timestamp (64)                           |
|                                                               |
+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
```

### Message Types

| ID | Name | Direction | Description |
|----|------|-----------|-------------|
| 0x00 | DiscoveryBroadcast | Any → Any | Announce presence |
| 0x01 | DiscoveryResponse | Any → Any | Respond to discovery |
| 0x02 | SessionRequest | Initiator → Target | Request session |
| 0x03 | SessionAccept | Target → Initiator | Accept session |
| 0x04 | SessionReject | Target → Initiator | Reject session |
| 0x05 | KeyExchange | Bidirectional | ECDH public keys |
| 0x06 | TransferOffer | Sender → Receiver | File metadata |
| 0x07 | TransferAccept | Receiver → Sender | Accept transfer |
| 0x08 | TransferReject | Receiver → Sender | Reject transfer |
| 0x09 | DataChunk | Sender → Receiver | Encrypted chunk |
| 0x0A | ChunkAck | Receiver → Sender | Chunk receipt |
| 0x0B | TransferComplete | Sender → Receiver | Final verification |
| 0x0C | TransferCancel | Bidirectional | Cancel transfer |
| 0x0D | Ping | Any → Any | Keepalive |
| 0x0E | Pong | Any → Any | Keepalive response |
| 0x0F | Error | Bidirectional | Error notification |

## Session Lifecycle

```
Device A (Sender)                     Device B (Receiver)
     │                                       │
     │──DiscoveryBroadcast──────────────────▶│
     │◀──DiscoveryResponse───────────────────│
     │                                       │
     │──SessionRequest──────────────────────▶│
     │◀──SessionAccept───────────────────────│
     │                                       │
     │──KeyExchange(A_pub)──────────────────▶│
     │◀──KeyExchange(B_pub)──────────────────│
     │                                       │
     │  [Both derive shared AES-256 key]     │
     │                                       │
     │──TransferOffer(metadata)─────────────▶│
     │◀──TransferAccept──────────────────────│
     │                                       │
     │──DataChunk(0, encrypted)─────────────▶│
     │◀──ChunkAck(0, OK)─────────────────────│
     │──DataChunk(1, encrypted)─────────────▶│
     │◀──ChunkAck(1, OK)─────────────────────│
     │              ...                      │
     │──DataChunk(N-1, encrypted)───────────▶│
     │◀──ChunkAck(N-1, OK)───────────────────│
     │                                       │
     │──TransferComplete(hash, duration)────▶│
     │                                       │
     │  [Session keys destroyed]             │
```

## Encryption

### Key Exchange
1. Both parties generate P-384 ECDH ephemeral key pairs
2. Public keys exchanged via KeyExchange message
3. Shared secret computed via ECDH
4. AES-256 key + IV seed derived via HKDF-SHA256

### Data Encryption
- Algorithm: AES-256-GCM
- Key: 32 bytes (derived from ECDH shared secret)
- Nonce: 12 bytes (random per chunk)
- Tag: 16 bytes (GCM authentication tag)
- Additional Authenticated Data: chunk_index (4 bytes, big-endian)

### Integrity
- Each chunk: SHA-256 hash of plaintext stored in chunk header
- Full file: SHA-256 hash of original data in TransferComplete
- Verification: Receiver recomputes hash and compares

## Discovery

### UDP Broadcast
- Port: 57771
- Address: 255.255.255.255 (broadcast) or 239.255.42.99 (multicast)
- Interval: 3 seconds
- Payload: JSON-encoded DiscoveryPayload

### mDNS
- Service: `_gestureshare._tcp.local`
- TXT records: device_id, device_type, capabilities

### BLE Advertising
- Service UUID: 0xFEGS (gesture share)
- Manufacturer data: device_id (8 bytes) + capabilities (2 bytes)

## Transfer

### Chunking
- Chunk size: 64 KB
- Max file size: 500 MB
- Compression: Zlib (fast level) before encryption
- Parallel chunks: Up to 4 concurrent (configurable)

### Retry
- Max retries per chunk: 3
- Retry delay: 500ms × attempt_number
- Timeout: 30 seconds per chunk

### Resume
- Chunk ACK bitmap tracks received chunks
- On reconnect: sender resends unacked chunks
- Session persists for 5 minutes after last activity

## Platform-Specific Notes

### Android
- CameraX for camera access
- MediaPipe Hands for landmark detection
- Nearby Connections API for WiFi Direct
- BLE advertising for discovery
- Background service for screenshot monitoring

### Windows
- MediaFoundation for webcam
- DXGI Desktop Duplication for screen capture
- WinRT BLE APIs
- Native mDNS via Win32

### macOS
- AVFoundation for webcam
- CGDisplay for screen capture
- CoreBluetooth for BLE
- Bonjour for mDNS

### Linux
- V4L2 for webcam
- X11/Wayland for screen capture
- BlueZ for BLE
- Avahi for mDNS

## Error Handling

| Code | Name | Description |
|------|------|-------------|
| 0x0001 | VERSION_MISMATCH | Protocol version incompatible |
| 0x0002 | SESSION_EXPIRED | Session timed out |
| 0x0003 | KEY_EXCHANGE_FAILED | ECDH computation failed |
| 0x0004 | TRANSFER_REJECTED | Receiver declined transfer |
| 0x0005 | INTEGRITY_FAILED | Hash verification failed |
| 0x0006 | CHUNK_TIMEOUT | Chunk not acknowledged |
| 0x0007 | DEVICE_BUSY | Target device in another session |
| 0x0008 | FILE_TOO_LARGE | Exceeds 500MB limit |
| 0x0009 | PERMISSION_DENIED | OS permission not granted |
| 0x000A | NETWORK_ERROR | Connection lost |

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2024-01 | Initial protocol |
