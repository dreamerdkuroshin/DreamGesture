pub mod webcam;
pub mod landmarker;
pub mod classifier;

use serde::{Deserialize, Serialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct DesktopGesture {
    pub gesture_type: String,
    pub confidence: f32,
    pub hand_side: String,
    pub direction: Direction3D,
    pub timestamp: u64,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Direction3D {
    pub x: f32,
    pub y: f32,
    pub z: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct Landmark {
    pub x: f32,
    pub y: f32,
    pub z: f32,
    pub visibility: f32,
}

#[derive(Debug, Clone)]
pub struct GestureConfig {
    pub min_confidence: f32,
    pub cooldown_ms: u64,
    pub use_gpu: bool,
    pub target_fps: u32,
}

impl Default for GestureConfig {
    fn default() -> Self {
        GestureConfig {
            min_confidence: 0.95,
            cooldown_ms: 500,
            use_gpu: true,
            target_fps: 15,
        }
    }
}
