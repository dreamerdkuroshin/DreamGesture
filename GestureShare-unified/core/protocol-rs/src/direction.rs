use crate::gesture::{Direction3D, HandLandmark};

pub struct DirectionEstimator;

impl DirectionEstimator {
    pub fn estimate_from_landmarks(landmarks: &[HandLandmark]) -> Direction3D {
        if landmarks.len() < 21 {
            return Direction3D::ZERO;
        }

        let wrist = &landmarks[0];
        let middle_tip = &landmarks[12];
        let index_tip = &landmarks[8];
        let ring_tip = &landmarks[16];

        let palm_direction = Direction3D {
            x: middle_tip.x - wrist.x,
            y: middle_tip.y - wrist.y,
            z: middle_tip.z - wrist.z,
        }
        .normalized();

        let pointing_direction = Direction3D {
            x: index_tip.x - wrist.x,
            y: index_tip.y - wrist.y,
            z: index_tip.z - wrist.z,
        }
        .normalized();

        Direction3D {
            x: (palm_direction.x + pointing_direction.x) / 2.0,
            y: (palm_direction.y + pointing_direction.y) / 2.0,
            z: (palm_direction.z + pointing_direction.z) / 2.0,
        }
        .normalized()
    }

    pub fn estimate_target_device(
        gesture_direction: &Direction3D,
        candidates: &[(String, DevicePosition)],
    ) -> Option<String> {
        if candidates.is_empty() {
            return None;
        }
        if candidates.len() == 1 {
            return Some(candidates[0].0.clone());
        }

        candidates
            .iter()
            .min_by(|a, b| {
                let angle_a = gesture_direction.angle_to(&a.1.direction);
                let angle_b = gesture_direction.angle_to(&b.1.direction);
                angle_a.partial_cmp(&angle_b).unwrap_or(std::cmp::Ordering::Equal)
            })
            .map(|(id, _)| id.clone())
    }
}

pub struct DevicePosition {
    pub device_id: String,
    pub direction: Direction3D,
    pub distance: f32,
    pub signal_strength: i32,
}

impl DevicePosition {
    pub fn from_rssi(device_id: String, rssi: i32, estimated_direction: Direction3D) -> Self {
        let distance = rssi_to_distance(rssi);
        DevicePosition {
            device_id,
            direction: estimated_direction,
            distance,
            signal_strength: rssi,
        }
    }
}

fn rssi_to_distance(rssi: i32) -> f32 {
    let tx_power: f32 = -59.0;
    if rssi == 0 {
        return -1.0;
    }
    let ratio = rssi as f32 / tx_power as f32;
    if ratio < 1.0 {
        ratio * ratio * ratio
    } else {
        0.89976 * ratio.powi(2) + 0.111 * ratio
    }
}
