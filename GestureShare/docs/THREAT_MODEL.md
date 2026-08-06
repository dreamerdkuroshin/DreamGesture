# Threat Model

## Assets
1. **Screenshots**: May contain sensitive information (messages, photos, financial data)
2. **Device Identity**: Endpoint IDs, device names visible on local network
3. **Session Keys**: Ephemeral encryption keys during active transfer

## Attack Trees

### 1. Unauthorized Screenshot Access
```
1.1 Access screenshot file on disk
    1.1.1 Exploit app sandbox bypass
    1.1.2 Root access to device
1.2 Intercept screenshot in memory
    1.2.1 Memory dump via debugger
    1.2.2 Cold boot attack
1.3 Fake ContentObserver event
    1.3.1 Malicious app with READ_EXTERNAL_STORAGE
```

### 2. Unauthorized Transfer
```
2.1 Trigger gesture without user intent
    2.1.1 Project hand image onto camera
    2.1.2 Device vibration mimicking hand motion
2.2 Spoof target device
    2.2.1 WiFi Direct spoofing
    2.2.2 BLE MAC address spoofing
2.3 Force transfer to wrong device
    2.3.1 Manipulate direction estimation
    2.3.2 Signal strength manipulation
```

### 3. Data Interception in Transit
```
3.1 Eavesdrop on transfer
    3.1.1 WiFi sniffing (WPA3 mitigates)
    3.1.2 BLE packet capture
3.2 Decrypt transferred data
    3.2.1 Break AES-256-GCM (infeasible)
    3.2.2 Extract key from memory
3.3 Modify transferred data
    3.3.1 Bit flip (GCM authentication detects)
    3.3.2 Replay old transfer (unique IV prevents)
```

## Risk Assessment

| Threat | Likelihood | Impact | Risk |
|--------|-----------|--------|------|
| Unauthorized Access | Low | High | Medium |
| Unauthorized Transfer | Very Low | Medium | Low |
| Data Interception | Very Low | High | Low |
| MITM Attack | Very Low | High | Low |
| DoS | Medium | Low | Low |

## Mitigations Summary

1. **Memory-only processing**: Screenshots never written to temp files
2. **ECDH key exchange**: Ephemeral keys provide forward secrecy
3. **AES-256-GCM**: Authenticated encryption detects tampering
4. **>95% confidence**: Multi-frame gesture verification prevents accidental triggers
5. **Cooldown**: Rate limiting prevents rapid re-triggering
6. **No cloud**: All processing stays on-device
7. **Sandboxing**: Standard Android security model
