use crate::Landmark;

pub struct GestureClassifier {
    min_confidence: f32,
}

impl GestureClassifier {
    pub fn new(min_confidence: f32) -> Self {
        GestureClassifier { min_confidence }
    }

    pub fn classify(&self, landmarks: &[Landmark]) -> Option<(String, f32)> {
        if landmarks.len() != 21 {
            return None;
        }

        let gesture = super::classify_from_landmarks(landmarks)?;
        if gesture.1 >= self.min_confidence {
            Some(gesture)
        } else {
            None
        }
    }
}

pub fn classify_from_landmarks(landmarks: &[Landmark]) -> Option<(String, f32)> {
    use std::collections::HashSet;

    let wrist = &landmarks[0];
    let thumb_tip = &landmarks[4];
    let index_tip = &landmarks[8];
    let middle_tip = &landmarks[12];
    let ring_tip = &landmarks[16];
    let pinky_tip = &landmarks[20];
    let index_mcp = &landmarks[5];

    let thumb_open = distance(wrist, thumb_tip) > distance(wrist, index_mcp) * 0.6;
    let index_open = is_finger_extended(landmarks, 8, 5);
    let middle_open = is_finger_extended(landmarks, 12, 9);
    let ring_open = is_finger_extended(landmarks, 16, 13);
    let pinky_open = is_finger_extended(landmarks, 20, 17);

    let extended_count = [index_open, middle_open, ring_open, pinky_open]
        .iter()
        .filter(|&&x| x)
        .count();

    let gesture = match (thumb_open, extended_count) {
        (false, 0) => "grab",
        (true, 4) => "open_hand",
        (false, 4) => "palm",
        (false, 1) if index_open => "point",
        (false, 2) if index_open && middle_open => "peace_sign",
        (true, 0) => {
            if thumb_tip.y < wrist.y {
                "thumb_up"
            } else {
                "thumb_down"
            }
        }
        (true, 3..=4) => "throw",
        (false, 0) => "fist",
        _ => "open_hand",
    };

    let confidence = compute_confidence(landmarks);
    Some((gesture.to_string(), confidence))
}

fn is_finger_extended(landmarks: &[Landmark], tip_idx: usize, mcp_idx: usize) -> bool {
    let wrist = &landmarks[0];
    let tip_dist = distance(wrist, &landmarks[tip_idx]);
    let mcp_dist = distance(wrist, &landmarks[mcp_idx]);
    tip_dist > mcp_dist * 1.2
}

fn distance(a: &Landmark, b: &Landmark) -> f32 {
    let dx = a.x - b.x;
    let dy = a.y - b.y;
    let dz = a.z - b.z;
    (dx * dx + dy * dy + dz * dz).sqrt()
}

fn compute_confidence(landmarks: &[Landmark]) -> f32 {
    let avg_vis: f32 = landmarks.iter().map(|l| l.visibility).sum::<f32>() / landmarks.len() as f32;
    let spatial_var: f32 = {
        let wrist = &landmarks[0];
        landmarks
            .iter()
            .map(|l| {
                let dx = l.x - wrist.x;
                let dy = l.y - wrist.y;
                dx * dx + dy * dy
            })
            .sum::<f32>()
            / landmarks.len() as f32
    };
    let spatial_conf = 1.0 - (spatial_var / 2.0).clamp(0.0, 0.3);
    (avg_vis * 0.7 + spatial_conf * 0.3).clamp(0.0, 1.0)
}
