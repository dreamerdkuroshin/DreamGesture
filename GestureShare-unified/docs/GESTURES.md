# Gesture Reference

## Supported Gestures

### Primary Gestures

| Gesture | Name | Action | How to Perform |
|---------|------|--------|----------------|
| 🤾 | Throw | Send file | Open hand, push forward toward target |
| ✊ | Grab | Receive file | Close hand, pull toward self |
| 👈 | Swipe Left | Cancel | Hand moves left across view |
| 👉 | Swipe Right | Send | Hand moves right across view |
| 👌 | Pinch | Copy | Thumb and index form circle |
| 🖐️ | Open Palm | Paste | All fingers extended, palm forward |
| 👋 | Wave | Connect | Open hand, wave side to side |
| ✊ | Fist | Pause | All fingers closed |
| 👍 | Thumb Up | Accept | Thumb extended upward |
| 👎 | Thumb Down | Reject | Thumb extended downward |

### Secondary Gestures

| Gesture | Name | Action | How to Perform |
|---------|------|--------|----------------|
| ☝️ | Point | Select target | Index finger extended |
| ✌️ | Peace Sign | Multi-select | Index + middle extended |
| 🤘 | Circle | Settings | Thumb + index form circle |
| 🖖 | Two Fingers | Next device | Switch between discovered devices |

### Gesture Sequences

| Sequence | Action | Description |
|----------|--------|-------------|
| Palm → Throw | Quick send | Open palm then throw |
| Wave → Point | Select & connect | Wave to discover, point to select |
| Pinch → Throw | Copy & send | Pinch to copy, throw to send |
| Grab ← Throw | Receive | Grab from sender's throw |

## Gesture Recognition Pipeline

```
Camera Frame
     │
     ▼
┌──────────────────┐
│ MediaPipe Hands   │  GPU/CPU
│ 21 Landmarks     │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Kalman Filter     │  Smooth landmarks
│ Optical Flow      │  Track motion
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Rule Classifier   │  Fast path
│ TFLite Model      │  Deep path (>95%)
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Gesture Engine    │  Cooldown, sequences
│ Confidence >95%   │  False positive reject
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Direction Est.    │  Target device
│ Device Selection  │  Spatial matching
└────────┬─────────┘
         │
         ▼
    Action Triggered
```

## Custom Gesture Training

Users can train custom gestures:

1. Open Settings → Gestures → Add Custom
2. Perform gesture 5 times for training samples
3. Assign action to gesture
4. Model retrains on-device (Federated Learning)

## Platform-Specific Gesture Handling

### Android (Front Camera)
- CameraX captures at 15-30 FPS
- MediaPipe Hands processes frames
- Gesture recognized within 20ms
- Direction estimated from palm normal

### Desktop (Webcam)
- Platform-specific webcam access:
  - Windows: MediaFoundation
  - macOS: AVFoundation
  - Linux: V4L2
- Same MediaPipe pipeline
- Same classification model

## Confidence Calibration

Gesture confidence is computed from:

1. **Landmark visibility** (40% weight): How visible are the landmarks
2. **Spatial confidence** (30% weight): Hand pose plausibility
3. **Temporal consistency** (20% weight): Smoothness across frames
4. **Classification score** (10% weight): Model output probability

Minimum trigger threshold: **95% confidence**

## Gesture Timing

| Parameter | Value | Description |
|-----------|-------|-------------|
| Min hold time | 200ms | Gesture must be held |
| Cooldown | 500ms | Between consecutive triggers |
| Sequence timeout | 2s | For multi-gesture sequences |
| Idle FPS | 5 | Camera rate when no hand |
| Active FPS | 15 | Camera rate with hand |
| Max FPS | 30 | During gesture execution |

## Tips for Best Recognition

1. **Good lighting**: Ensure your hand is well-lit
2. **Clear background**: Avoid cluttered backgrounds
3. **Steady hand**: Hold gesture steady for 200ms+
4. **Full hand visible**: Keep entire hand in frame
5. **Facing camera**: Palm should face the camera
6. **Distance**: 30-80cm from camera for best results
