#!/bin/bash
set -euo pipefail

MODELS_DIR="$(cd "$(dirname "$0")/../app/src/main/assets/models" && pwd)"
mkdir -p "$MODELS_DIR"

echo "Downloading MediaPipe hand landmarker model..."
curl -L -o "$MODELS_DIR/hand_landmarker.task" \
  "https://storage.googleapis.com/mediapipe-models/hand_landmarker/hand_landmarker/float16/1/hand_landmarker.task"

echo "Downloading gesture classifier model..."
if [ ! -f "$MODELS_DIR/gesture_classifier.tflite" ]; then
  echo "WARNING: gesture_classifier.tflite not found in repository."
  echo "Please train your custom gesture classifier and place it at:"
  echo "  $MODELS_DIR/gesture_classifier.tflite"
  echo ""
  echo "Alternatively, use the rule-based classifier in GestureRecognizer.kt"
fi

echo "Done. Models are in $MODELS_DIR"
ls -la "$MODELS_DIR"
